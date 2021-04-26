package de.tum.digitalagriculture.commanders;

import de.tum.digitalagriculture.controllers.Controller;
import de.tum.digitalagriculture.controllers.FlightController;
import de.tum.digitalagriculture.controllers.Result;
import de.tum.digitalagriculture.streams.StreamWriter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PathCommander implements Commander {
    @Getter
    private final Controller controller;
    @Getter
    @Setter
    private Commands.Command[] commands;

    public PathCommander(@NonNull Controller controller, @NonNull Commands.Command... commands) {
        this.controller = controller;
        this.commands = commands;
    }

    @SneakyThrows
    public static void main(String[] args) {
        Loader.load(opencv_java.class);

        Commands.Command[] commands = new Commands.Command[]{
                new Commands.StreamOn(),
                new Commands.TakeOff(),
                new Commands.Up(50),
                new Commands.Forward(100),
                new Commands.ClockWise(180),
                new Commands.Forward(100),
                new Commands.CounterClockWise(180),
                new Commands.Land(),
                new Commands.StreamOff(),
        };
        var executor = new ScheduledThreadPoolExecutor(8);
        var streamHandler = new StreamWriter("/tmp/flight0.avi");
        var controller = new FlightController<>("192.168.10.1", executor, streamHandler, FlightController.ConnectionOption.TIME_OUT);
        var commander = new PathCommander(controller, commands);
        commander.run();
        streamHandler.close();
        controller.close();
    }

    @Override
    public Result execute(Commands.Command command) {
        return controller.execute(command);
    }

    @Override
    public void run() {
        for (var command : commands) {
            execute(command);
        }
        System.out.println("Finished");
    }
}
