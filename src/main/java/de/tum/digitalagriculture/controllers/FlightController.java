package de.tum.digitalagriculture.controllers;

import de.tum.digitalagriculture.commanders.Commands;
import de.tum.digitalagriculture.streams.StreamHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class FlightController<D, S extends StreamHandler.Stream<D>> implements Controller, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);
    @Getter
    private final ConnectionOption connectionOption;
    @Getter
    private final InetSocketAddress remoteAddress;
    @Getter
    private final String streamAddress;
    private final DatagramChannel commandChannel;
    private final DatagramChannel statusChannel;
    private final ScheduledExecutorService executor;
    private final StreamHandler<S> streamHandler;
    private S stream;

    public FlightController(String ip, ScheduledExecutorService executor, StreamHandler<S> streamHandler, ConnectionOption connectionOption) {
        this.connectionOption = connectionOption;
        remoteAddress = new InetSocketAddress(ip, 8889);
        streamAddress = "udp://0.0.0.0:11111";
        commandChannel = createChannel(8889);
        statusChannel = createChannel(8890);
        this.streamHandler = streamHandler;
        stream = null;

        this.executor = executor;
        if (connectionOption == ConnectionOption.KEEP_ALIVE) {
            executor.scheduleAtFixedRate(() -> sendAndRecv(new Commands.ReadBattery()), 14, 14, TimeUnit.SECONDS);
            logger.debug("Scheduled keep alive!");
        }
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
            startStream(streamAddress);
        }
        if (command instanceof Commands.StreamOff) {
            stopStream();
        }
        return result;
    }

    @SneakyThrows
    private void startStream(String streamUrl) {
        stream = streamHandler.startStream(streamUrl);
        logger.debug("Starting stream!");
        executor.submit(stream::capture);
    }

    private void stopStream() {
        if (!streamHandler.hasActiveStream()) {
            logger.warn("No stream running!");
            return;
        }
        logger.debug("Stopping stream");
        streamHandler.stopStream();
    }

    public D getStreamData() {
        if (stream != null) {
            return stream.getData();
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        if (streamHandler.hasActiveStream()) {
            streamHandler.stopStream();
        }
        executor.shutdown();
        commandChannel.close();
        statusChannel.close();
    }

    public enum ConnectionOption {
        KEEP_ALIVE, TIME_OUT
    }
}
