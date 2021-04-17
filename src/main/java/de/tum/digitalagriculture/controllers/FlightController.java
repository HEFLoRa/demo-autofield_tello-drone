package de.tum.digitalagriculture.controllers;

import de.tum.digitalagriculture.commanders.Commands;
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class FlightController implements Controller, AutoCloseable {
    @Getter
    private final ConnectionOption connectionOption;
    @Getter
    private final InetSocketAddress remoteAddress;

    private final DatagramChannel commandChannel;
    private final DatagramChannel statusChannel;
    private final DatagramChannel streamChannel;

    private final ScheduledExecutorService executorService;

    private final Logger logger = LoggerFactory.getLogger(FlightController.class);


    public enum ConnectionOption {
        KEEP_ALIVE, TIME_OUT
    }

    public FlightController(String ip, ConnectionOption connectionOption) {
        this.connectionOption = connectionOption;
        remoteAddress = new InetSocketAddress(ip, 8889);

        commandChannel = createChannel(8899);
        statusChannel = createChannel(8890);
        streamChannel = createChannel(11111);

        executorService = createScheduler(connectionOption);
    }

    @SneakyThrows
    private static DatagramChannel createChannel(Integer localPort) {
        var channel = DatagramChannel.open();
        var localAddress = new InetSocketAddress(localPort);
        channel.bind(localAddress);
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
        var result = Result.parse(StandardCharsets.UTF_8.decode(recvBuffer).toString());
        logger.debug("Received result: {}", result);
        return result;
    }

    @Override
    public Result execute(Commands.Command command) {
        return sendAndRecv(command);
    }

    @Override
    public void readStream() {

    }

    @Override
    public void stopStream() {

    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
        commandChannel.close();
        statusChannel.close();
        streamChannel.close();
    }
}
