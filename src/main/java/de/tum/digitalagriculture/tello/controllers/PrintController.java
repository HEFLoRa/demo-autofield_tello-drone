package de.tum.digitalagriculture.tello.controllers;

import de.tum.digitalagriculture.tello.commanders.Commands;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A simple test controller that simply logs all input that is receives
 */
public class PrintController implements Controller {
    /**
     * How long the <em>execution time</em> is
     *
     * @return simulated <em>execution time</em>
     */
    @Getter
    private final long duration;
    /**
     * Time unit of the execution time
     *
     * @return time unit of the execution time
     */
    @Getter
    private final TimeUnit unit;
    private final Logger logger = LoggerFactory.getLogger(PrintController.class);
    /**
     * Is the <em>stream</em> running
     *
     * @return is the <em>stream</em> running
     */
    @Getter
    private boolean streamIsRunning;

    /**
     * Create a new {@link PrintController}
     *
     * @param duration how long the <em>execution time</em> is
     * @param unit time unit of the <em>execution time</em>
     */
    public PrintController(long duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
        accept(new Commands.Init());
    }

    @Override
    @SneakyThrows
    public void accept(@NonNull Commands.Command command) {
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
    }

}
