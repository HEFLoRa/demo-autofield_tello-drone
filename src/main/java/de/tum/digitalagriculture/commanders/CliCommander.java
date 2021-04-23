package de.tum.digitalagriculture.commanders;

import de.tum.digitalagriculture.controllers.Controller;
import de.tum.digitalagriculture.controllers.FlightController;
import de.tum.digitalagriculture.controllers.Result;
import de.tum.digitalagriculture.streams.StreamWriter;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class CliCommander implements Commander {
    private static final Logger logger = LoggerFactory.getLogger(CliCommander.class);
    @Getter
    private final Controller controller;

    public CliCommander(Controller controller) {
        this.controller = controller;
    }

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
        var streamHandler = new StreamWriter("/tmp/capture0.webm");
        @Cleanup var controller = new FlightController<>(ip, streamHandler, FlightController.ConnectionOption.KEEP_ALIVE);
//        var controller = new PrintController(500, TimeUnit.MILLISECONDS);
        var commander = new CliCommander(controller);
        commander.run();
        controller.close();
    }

    @Override
    @SneakyThrows
    public void run() {
        @Cleanup var reader = new BufferedReader(new InputStreamReader(System.in));
        var input = "";
        while (!input.equals("end")) {
            System.out.println("Please enter your command:");
            input = reader.readLine();
            try {
                var command = Commands.Command.parse(input);
                var result = execute(command);
                logger.info(result.toString());
            } catch (IllegalArgumentException ill) {
                logger.warn("Invalid input");
            }
        }
        logger.info("Finished");
    }

    @Override
    public Result execute(Commands.Command command) {
        return controller.execute(command);
    }
}
