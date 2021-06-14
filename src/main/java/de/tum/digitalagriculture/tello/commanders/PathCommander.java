package de.tum.digitalagriculture.tello.commanders;

import de.tum.digitalagriculture.tello.controllers.PrintController;
import de.tum.digitalagriculture.tello.streams.StreamWriter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executes the commands provided by {@code commands}
 */
public class PathCommander implements Commander {
    /**
     * Commands of the {@link PathCommander}
     *
     * @param commands set the commands
     * @return commands that the {@link PathCommander} executes
     */
    @Getter
    @Setter
    private Commands.Command[] commands;
    private Iterator<Commands.Command> iter;

    /**
     * @param commands Commands to execute
     */
    public PathCommander(@NonNull Commands.Command... commands) {
        this.commands = commands;
        iter = Arrays.stream(commands).iterator();
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
//        var controller = new FlightController<>("192.168.10.1", executor, streamHandler, FlightController.ConnectionOption.TIME_OUT);
        var controller = new PrintController(100, TimeUnit.MILLISECONDS);
        var commander = new PathCommander(commands);
        commander.forEachRemaining(controller);
        streamHandler.close();
//        controller.close();
    }

    /**
     * Restart the iterator
     */
    public void restart() {
        iter = Arrays.stream(commands).iterator();
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public Commands.Command next() {
        return iter.next();
    }
}
