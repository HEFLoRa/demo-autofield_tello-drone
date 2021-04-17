package de.tum.digitalagriculture.commanders;

import de.tum.digitalagriculture.controllers.Controller;
import de.tum.digitalagriculture.controllers.PrintController;
import de.tum.digitalagriculture.controllers.Result;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;


public class CliCommander implements Commander {
    @Getter private final  Controller controller;
    private final Logger logger = LoggerFactory.getLogger(CliCommander.class);

    public CliCommander(Controller controller) {
        this.controller = controller;
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
                logger.info("Received result: {}", result);
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

    public static void main(String[] args) {
        final String ip;
        if (args.length == 1) {
            ip = args[0];
        } else {
            ip = "192.168.10.1"; // The ip when connected to the drone directly
        }
        var controller = new PrintController(500, TimeUnit.MILLISECONDS);
        var commander = new CliCommander(controller);
        commander.run();
    }
}
