package de.tum.digitalagriculture.controllers;

import de.tum.digitalagriculture.commanders.Commands;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.DatagramChannel;
import java.util.concurrent.TimeUnit;

public class PrintController implements Controller {
    @Getter
    private boolean streamIsRunning;
    @Getter
    private final long duration;
    @Getter
    private final TimeUnit unit;

    private final Logger logger = LoggerFactory.getLogger(PrintController.class);

    public PrintController(long duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
        execute(new Commands.Init());
    }

    @Override
    @SneakyThrows
    public Result execute(@NonNull Commands.Command command) {
        if (command instanceof Commands.StreamOn && streamIsRunning) {
            throw new IllegalStateException("Stream already running!");
        } else if (command instanceof Commands.StreamOn) {
            streamIsRunning = true;
            logger.info("Start stream");
        } else if (command instanceof Commands.StreamOff && !streamIsRunning) {
            throw new IllegalStateException("No stream running!");
        } else if (command instanceof Commands.StreamOff) {
            streamIsRunning = false;
            logger.info("Stop stream");
        }
        logger.info("Executing: {}", command);
        Thread.sleep(unit.toMillis(duration));
        return new Result(Result.ResultEnum.OK,  "ok");
    }

}
