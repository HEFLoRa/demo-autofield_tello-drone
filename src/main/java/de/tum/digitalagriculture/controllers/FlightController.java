package de.tum.digitalagriculture.controllers;

import de.tum.digitalagriculture.commanders.Commands;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Synchronized;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class FlightController implements Controller, AutoCloseable {
    @Getter
    private final ConnectionOption connectionOption;
    @Getter
    private final InetSocketAddress remoteAddress;
    @Getter
    private final InetSocketAddress streamAddress;

    private final DatagramChannel commandChannel;
    private final DatagramChannel statusChannel;

    private final ScheduledExecutorService executorService;
    private final StreamHandler streamHandler;

    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);

    private ScheduledFuture<?> streamTask;


    public enum ConnectionOption {
        KEEP_ALIVE, TIME_OUT
    }

    public FlightController(String ip, StreamHandler streamHandler, ConnectionOption connectionOption) {
        this.connectionOption = connectionOption;
        remoteAddress = new InetSocketAddress(ip, 8889);
        streamAddress = new InetSocketAddress(ip, 11111);

        commandChannel = createChannel(8889);
        statusChannel = createChannel(8890);
        this.streamHandler = streamHandler;
        streamTask = null;

        executorService = createScheduler(connectionOption);

        sendAndRecv(new Commands.Init());
    }

    @SneakyThrows
    private static DatagramChannel createChannel(Integer localPort) {
        var channel = DatagramChannel.open();
        var localAddress = new InetSocketAddress(localPort);
        channel.bind(localAddress);
        logger.debug("Created channel for port {}", localPort);
        return channel;
    }

    private ScheduledExecutorService createScheduler(ConnectionOption connectionOption) {
        var numThreads = 2;
        ScheduledThreadPoolExecutor executor;
        if (connectionOption == ConnectionOption.KEEP_ALIVE) {
            executor = new ScheduledThreadPoolExecutor(numThreads + 1);
            executor.scheduleAtFixedRate(() -> this.sendAndRecv(new Commands.ReadBattery()), 10, 10, TimeUnit.SECONDS);
        } else {
            executor = new ScheduledThreadPoolExecutor(numThreads);
        }
        logger.debug("Created scheduled executor with {}", connectionOption);
        return executor;
    }

    @Synchronized
    private Result sendAndRecv(Commands.Command command) {
        var sendBuffer = ByteBuffer.wrap(command.toString().getBytes(StandardCharsets.UTF_8));
        try {
            logger.debug("Sending command {}", command);
            this.commandChannel.send(sendBuffer, remoteAddress);
        } catch (IOException ioException) {
            logger.warn("Sending command {} failed: {}", command, ioException.getMessage());
            return new Result(Result.ResultEnum.ERROR, ioException.getMessage());
        }
        var recvBuffer = ByteBuffer.allocate(1024);
        try {
            logger.debug("Waiting for answer");
            this.commandChannel.receive(recvBuffer);
        } catch (IOException ioException) {
            logger.warn("Reception failed: {}", ioException.getMessage());
            return new Result(Result.ResultEnum.ERROR, ioException.getMessage());
        }
        recvBuffer.flip();
        var response = StandardCharsets.UTF_8.decode(recvBuffer).toString();
        var result = Result.of(command, response);
        logger.debug("Received result: {}", result);
        return result;
    }

    @Override
    public Result execute(Commands.Command command) {
        var result = sendAndRecv(command);
        if (command instanceof Commands.StreamOn) {
            startStream("udp:/" + remoteAddress.toString());
        }
        if (command instanceof Commands.StreamOff) {
            stopStream();
        }
        return result;
    }

    private void startStream(String videoPath) {
        if (streamTask != null) {
            logger.warn("Stream already running!");
            return;
        }
        streamHandler.startStream(videoPath);
        var freq = Math.round(1000D/streamHandler.getFps());
        streamTask = executorService.scheduleAtFixedRate(streamHandler::capture, freq, freq, TimeUnit.MILLISECONDS);
    }

    private void stopStream() {
        if (streamTask == null || !streamHandler.isActive()) {
            logger.warn("No stream running!");
            return;
        }
        if (!streamTask.cancel(false)) {
            logger.warn("Stream could not be cancelled!");
        }
        streamTask = null;
        streamHandler.stopStream();
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
        commandChannel.close();
        statusChannel.close();
    }
}
