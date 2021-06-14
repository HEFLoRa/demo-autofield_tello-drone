package de.tum.digitalagriculture.tello.commanders;

import de.tum.digitalagriculture.tello.controllers.PrintController;
import de.tum.digitalagriculture.tello.streams.StreamWriter;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * A commander that takes commands from the CLI. On "end" finishes the iteration.
 */
public class CliCommander implements Commander, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(CliCommander.class);
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private Commands.Command nextCommand = null;

    @SneakyThrows
    public static void main(String[] args) {
        // Load OpenCV -> needs to be done bc. its a non-JVM library
        Loader.load(opencv_java.class);

        final String ip;
        if (args.length == 1) {
            ip = args[0];
        } else {
            ip = "192.168.10.1"; // The ip when connected to the drone directly
        }
        var executor = new ScheduledThreadPoolExecutor(8);
        var streamHandler = new StreamWriter("/tmp/capture0.avi");
//        @Cleanup var controller = new FlightController<>(ip, executor, streamHandler, FlightController.ConnectionOption.KEEP_ALIVE);
        var controller = new PrintController(500, TimeUnit.MILLISECONDS);
        @Cleanup var commander = new CliCommander();
        commander.forEachRemaining(controller);
    }

    /**
     * Close the input reader
     * @throws IOException failed to close reader
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Read from STDIN. If "end" is input, return null, to end the iteration.
     * @return the next command to be executed
     */
    @SneakyThrows
    private Commands.Command getNextCommand() {
        if (nextCommand != null) {
            return nextCommand;
        }
        System.out.println("Please enter your command:");
        var input = reader.readLine();
        if (input.equals("end")) {
            logger.info("Finished!");
            return null;
        }
        Commands.Command command;
        try {
            command = Commands.Command.parse(input);
        } catch (IllegalArgumentException iae) {
            logger.warn(String.format("Erroneous command: %s", input));
            command = getNextCommand();
        }
        nextCommand = command;
        return nextCommand;
    }

    @SneakyThrows
    @Override
    public boolean hasNext() {
        return getNextCommand() != null;
    }

    @Override
    public Commands.Command next() {
        var command = getNextCommand();
        if (command == null) {
            throw new IllegalStateException("Iteration finished!");
        }
        nextCommand = null;
        return command;
    }
}
