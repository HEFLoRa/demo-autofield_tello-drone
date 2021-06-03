package de.tum.digitalagriculture.tello.controllers;

import de.tum.digitalagriculture.tello.commanders.Commands;
import de.tum.digitalagriculture.tello.streams.StreamHandler;
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


/**
 * Class that sends commands to the Tello drone
 *
 * @param <D> type of stream data
 * @param <S> type of stream
 */
public class FlightController<D, S extends StreamHandler.Stream<D>> implements Controller, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);
    /**
     * Options of the UDP connection
     *
     * @return how connection keep alive is handled
     */
    @Getter
    private final ConnectionOption connectionOption;
    /**
     * Remote address of the drone
     *
     * @return the remote address of the drone
     */
    @Getter
    private final InetSocketAddress remoteAddress;

    /**
     * Adress of the stream locally bound stream
     */
    @Getter
    private final String streamAddress;
    private final DatagramChannel commandChannel;
    private final DatagramChannel statusChannel;
    private final ScheduledExecutorService executor;
    private final StreamHandler<S> streamHandler;
    private S stream;

    /**
     * @param ip IP of the Tello drone. Usually 10.0.0.1
     * @param executor Executor that handles simultaneous execution of stream and commands
     * @param streamHandler Stream handler that controls how the stream is processed
     * @param connectionOption How the connection to the Tello drone should be setup. When KEEP_ALIVE a keep alive command is send every 10s, otherwise the drone will disconnect after 15s
     */
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

    /**
     * create a UDP channel over which communication with the Tello drone is performed
     *
     * @param localPort local port that the UDP stream should be bound to
     * @return a Datagram channel at local port
     */
    @SneakyThrows
    private static DatagramChannel createChannel(Integer localPort) {
        var channel = DatagramChannel.open();
        var localAddress = new InetSocketAddress(localPort);
        channel.bind(localAddress);
        logger.debug("Created channel for port {}", localPort);
        return channel;
    }


    /**
     * Send a command and wait for the result of the execution
     *
     * @param command command to be sent
     * @return the result of the execution
     */
    @Synchronized
    public Result sendAndRecv(Commands.Command command) {
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

    /**
     * Send a commmand to the drone
     *
     * @param command Command to be sent to the drone
     */
    @Override
    public void accept(Commands.Command command) {
        var result = sendAndRecv(command);
        if (command instanceof Commands.StreamOn) {
            startStream(streamAddress);
        }
        if (command instanceof Commands.StreamOff) {
            stopStream();
        }
        logger.info(result.toString());
    }

    /**
     * Start a capture the drone's stream
     *
     * @param streamUrl the url of the stream
     */
    @SneakyThrows
    private void startStream(String streamUrl) {
        stream = streamHandler.startStream(streamUrl);
        logger.debug("Starting stream!");
        executor.submit(stream::capture);
    }

    /**
     * Stop the capture of the drone's stream
     */
    private void stopStream() {
        if (!streamHandler.hasActiveStream()) {
            logger.warn("No stream running!");
            return;
        }
        logger.debug("Stopping stream");
        streamHandler.stopStream();
    }

    /**
     * Return the data of the corresponding stream
     *
     * @return the data of the stream. Could be for example: {@code null}, a string of the filename, an array of captured images
     */
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


    /**
     * Options how the connection to the drone should be handled
     * <ul>
     *     <li>{@code KEEP_ALIVE}: send a command every 10s to keep the connection alive</ul>
     *     <li>{@code TIME_OUT}: time the connection after 15s out</ul>
     * </ul>
     */
    public enum ConnectionOption {
        KEEP_ALIVE, TIME_OUT
    }
}
