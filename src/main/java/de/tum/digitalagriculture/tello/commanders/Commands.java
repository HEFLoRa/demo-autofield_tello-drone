package de.tum.digitalagriculture.tello.commanders;

import lombok.Getter;
import lombok.NonNull;

public class Commands {

    /**
     * Abstract class from which all commands, that the Tello drone can execute, should be derived.
     * <p>
     *     See <a href=file:///tmp/mozilla_edward0/Tello%20SDK%202.0%20User%20Guide.pdf>the tello sdk</a>
     * </p>
     */
    public abstract static class Command {
        /**
         * @return the string version of the command
         */
        @Getter
        private final String command;

        private Command(@NonNull String command) {
            this.command = command;
        }

        /**
         * Parse a string to the corresponding command
         *
         * @param input string to parse
         * @return the parsed Command
         * @throws IllegalArgumentException on wrong input
         */
        public static Command parse(@NonNull String input) throws IllegalArgumentException {
            var command = input.trim();
            if (command.isEmpty()) {
                throw new IllegalArgumentException("Passed empty string");
            }
            var split = command.split(" ");
            return switch (split[0]) {
                case "command" -> new Init();
                case "takeoff" -> new TakeOff();
                case "land" -> new Land();
                case "streamon" -> new StreamOn();
                case "streamoff" -> new StreamOff();
                case "emergency" -> new Emergency();
                case "stop" -> new Stop();
                case "speed?" -> new ReadSpeed();
                case "battery?" -> new ReadBattery();
                case "time?" -> new ReadTime();
                case "wifi?" -> new ReadWifi();
                case "up" -> {
                    if (split.length != 2) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new Up(Integer.parseInt(split[1]));
                }
                case "down" -> {
                    if (split.length != 2) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new Down(Integer.parseInt(split[1]));
                }
                case "left" -> {
                    if (split.length != 2) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new Left(Integer.parseInt(split[1]));
                }
                case "right" -> {
                    if (split.length != 2) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new Right(Integer.parseInt(split[1]));
                }
                case "forward" -> {
                    if (split.length != 2) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new Forward(Integer.parseInt(split[1]));
                }
                case "back" -> {
                    if (split.length != 2) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new Back(Integer.parseInt(split[1]));
                }
                case "cw" -> {
                    if (split.length != 2) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new ClockWise(Integer.parseInt(split[1]));
                }
                case "ccw" -> {
                    if (split.length != 2) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new CounterClockWise(Integer.parseInt(split[1]));
                }
                case "speed" -> {
                    if (split.length != 2) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new Speed(Integer.parseInt(split[1]));
                }
                case "flip" -> {
                    if (split.length != 2) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new Flip(split[1].charAt(0));
                }
                case "go" -> {
                    if (split.length != 5) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new Go(
                            Integer.parseInt(split[1]),
                            Integer.parseInt(split[2]),
                            Integer.parseInt(split[3]),
                            Integer.parseInt(split[4])
                    );
                }
                case "curve" -> {
                    if (split.length != 8) {
                        throw new IllegalArgumentException(String.format("The number of passed parameters (%d) does not match the expected amount (1)", split.length - 1));
                    }
                    yield new Curve(
                            Integer.parseInt(split[1]),
                            Integer.parseInt(split[2]),
                            Integer.parseInt(split[3]),
                            Integer.parseInt(split[4]),
                            Integer.parseInt(split[5]),
                            Integer.parseInt(split[6]),
                            Integer.parseInt(split[7])
                    );
                }
                default -> throw new IllegalArgumentException(String.format("Unknown command: %s", command));
            };
        }

        @Override
        public String toString() {
            return getCommand();
        }
    }

    /**
     * Initialize the Tello drone to accept commands. Has to be executed before any other command is send to the drone
     */
    public static final class Init extends Command {
        public Init() {
            super("command");
        }
    }

    /**
     * Take off with the drone
     */
    public static final class TakeOff extends Command {
        public TakeOff() {
            super("takeoff");
        }
    }

    /**
     * Land at the current position
     */
    public static final class Land extends Command {
        public Land() {
            super("land");
        }
    }

    /**
     * Start a video stream. Stream can be read from UDP port 11111
     */
    public static final class StreamOn extends Command {
        public StreamOn() {
            super("streamon");
        }
    }

    /**
     * End a running stream
     */
    public static final class StreamOff extends Command {
        public StreamOff() {
            super("streamoff");
        }
    }

    /**
     * Emergency stop. Immediately shuts down motors
     */
    public static final class Emergency extends Command {
        public Emergency() {
            super("emergency");
        }
    }

    /**
     * Stop the drone in flight
     */
    public static final class Stop extends Command {
        public Stop() {
            super("stop");
        }
    }

    /**
     * Read the current speed of the drone
     */
    public static final class ReadSpeed extends Command {
        public ReadSpeed() {
            super("speed?");
        }
    }

    /**
     * Read battery voltage
     */
    public static final class ReadBattery extends Command {
        public ReadBattery() {
            super("battery?");
        }
    }

    /**
     * Read flight time
     */
    public static final class ReadTime extends Command {
        public ReadTime() {
            super("time?");
        }
    }

    /**
     * Read WiFi signal strength
     */
    public static final class ReadWifi extends Command {
        public ReadWifi() {
            super("wifi?");
        }
    }

    /**
     * Abstract class from which commands with on parameter should extend
     * @param <T> the input type of a command
     */
    public static abstract class SingleParameterCommand<T> extends Command {
        @Getter
        private final T x;

        private SingleParameterCommand(@NonNull String command, @NonNull T x) {
            super(command);
            this.x = x;
        }

        @Override
        public String toString() {
            return String.format("%s %s", getCommand(), getX().toString());
        }
    }

    /**
     * Ascend {@code x} centimeters. x in [20, 500]
     */
    public static final class Up extends SingleParameterCommand<Integer> {
        public Up(@NonNull Integer x) {
            super("up", x);
        }
    }

    /**
     * Descend {@code x} centimeters. x in [20, 500]
     */
    public static final class Down extends SingleParameterCommand<Integer> {
        public Down(@NonNull Integer x) {
            super("down", x);
        }
    }

    /**
     * Fly {@code x} centimeters to the left. x in [20, 500]
     */
    public static final class Left extends SingleParameterCommand<Integer> {
        public Left(@NonNull Integer x) {
            super("left", x);
        }
    }

    /**
     * Fly {@code x} centimeters to the right. x in [20, 500]
     */
    public static final class Right extends SingleParameterCommand<Integer> {
        public Right(@NonNull Integer x) {
            super("right", x);
        }
    }

    /**
     * Fly {@code x} centimeters forward. x in [20, 500]
     */
    public static final class Forward extends SingleParameterCommand<Integer> {
        public Forward(@NonNull Integer x) {
            super("forward", x);
        }
    }
    /**
     * Fly {@code x} centimeters backward. x in [20, 500]
     */
    public static final class Back extends SingleParameterCommand<Integer> {
        public Back(@NonNull Integer x) {
            super("back", x);
        }
    }

    /**
     * Rotate {@code x} degrees clockwise. x in [1, 360]
     */
    public static final class ClockWise extends SingleParameterCommand<Integer> {
        public ClockWise(@NonNull Integer x) {
            super("cw", x);
        }
    }

    /**
     * Rotate {@code x} degrees counter-clockwise. x in [1, 360]
     */
    public static final class CounterClockWise extends SingleParameterCommand<Integer> {
        public CounterClockWise(@NonNull Integer x) {
            super("ccw", x);
        }
    }

    /**
     * Set the drones speed to {@code x} cm/s. x in [10, 100]
     */
    public static final class Speed extends SingleParameterCommand<Integer> {
        public Speed(@NonNull Integer x) {
            super("speed", x);
        }
    }

    /**
     * Flip the drone in {@code x} direction. x in {"l", "r", "f", "b"} (left, right, forward, backward)
     */
    public static final class Flip extends SingleParameterCommand<Character> {
        public Flip(@NonNull Character x) {
            super("flip", x);
        }
    }

    /**
     * Commands with more than one parameter should derive from this class
     *
     * @param <T> type of the passed parameters
     */
    public static abstract class MultiParameterCommand<T> extends Command {
        @Getter
        private final T[] params;

        @SafeVarargs
        private MultiParameterCommand(@NonNull String command, @NonNull T... params) {
            super(command);
            this.params = params;
        }

        @Override
        public String toString() {
            StringBuilder output = new StringBuilder(getCommand());
            for (var t : params) {
                output.append(" ");
                output.append(t.toString());
            }
            return output.toString();
        }
    }

    /**
     * Go to ({@code x}, {@code y}, {@code z}) at {@code speed} cm/s. x, y, z in [-500, 500], speed in [10, 100]
     * <p>{@code x}, {@code y}, {@code z} cannot be set to [-20,20] simultaneously</p>
     */
    public static final class Go extends MultiParameterCommand<Integer> {
        public Go(@NonNull Integer x, @NonNull Integer y, @NonNull Integer z, @NonNull Integer speed) {
            super("go", x, y, z, speed);
        }
    }

    /**
     * Fly in a curve to ({@code x1}, {@code y1}, {@code z1}) and ({@code x2}, {@code y2}, {@code z2}) at {@code speed} cm/s. x{1,2}, y{1,2}, z{1,2} in [-500, 500], speed in [10, 100]
     * <p>{@code x}, {@code y}, {@code z} cannot be set to [-20,20] simultaneously</p>
     */
    public static final class Curve extends MultiParameterCommand<Integer> {
        public Curve(@NonNull Integer x1, @NonNull Integer y1, @NonNull Integer z1, @NonNull Integer x2, @NonNull Integer y2, @NonNull Integer z2, @NonNull Integer speed) {
            super("curve", x1, y1, z1, x2, y2, z2, speed);
        }
    }
}
