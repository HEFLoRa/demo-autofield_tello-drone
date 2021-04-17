package de.tum.digitalagriculture.commanders;

import lombok.Getter;
import lombok.NonNull;

public class Commands {

    public abstract static class Command {
        @Getter
        private final String command;

        private Command(@NonNull String command) {
            this.command = command;
        }

        public static Command parse(@NonNull String input) {
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

    public static final class Init extends Command {
        public Init() {
            super("command");
        }
    }

    public static final class TakeOff extends Command {
        public TakeOff() {
            super("takeoff");
        }
    }

    public static final class Land extends Command {
        public Land() {
            super("land");
        }
    }

    public static final class StreamOn extends Command {
        public StreamOn() {
            super("streamon");
        }
    }

    public static final class StreamOff extends Command {
        public StreamOff() {
            super("streamoff");
        }
    }

    public static final class Emergency extends Command {
        public Emergency() {
            super("emergency");
        }
    }

    public static final class Stop extends Command {
        public Stop() {
            super("stop");
        }
    }

    public static final class ReadSpeed extends Command {
        public ReadSpeed() {
            super("speed?");
        }
    }

    public static final class ReadBattery extends Command {
        public ReadBattery() {
            super("battery?");
        }
    }

    public static final class ReadTime extends Command {
        public ReadTime() {
            super("time?");
        }
    }

    public static final class ReadWifi extends Command {
        public ReadWifi() {
            super("wifi?");
        }
    }

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

    public static final class Up extends SingleParameterCommand<Integer> {
        public Up(@NonNull Integer x) {
            super("up", x);
        }
    }

    public static final class Down extends SingleParameterCommand<Integer> {
        public Down(@NonNull Integer x) {
            super("down", x);
        }
    }

    public static final class Left extends SingleParameterCommand<Integer> {
        public Left(@NonNull Integer x) {
            super("left", x);
        }
    }

    public static final class Right extends SingleParameterCommand<Integer> {
        public Right(@NonNull Integer x) {
            super("right", x);
        }
    }

    public static final class Forward extends SingleParameterCommand<Integer> {
        public Forward(@NonNull Integer x) {
            super("forward", x);
        }
    }

    public static final class Back extends SingleParameterCommand<Integer> {
        public Back(@NonNull Integer x) {
            super("back", x);
        }
    }

    public static final class ClockWise extends SingleParameterCommand<Integer> {
        public ClockWise(@NonNull Integer x) {
            super("cw", x);
        }
    }

    public static final class CounterClockWise extends SingleParameterCommand<Integer> {
        public CounterClockWise(@NonNull Integer x) {
            super("ccw", x);
        }
    }

    public static final class Speed extends SingleParameterCommand<Integer> {
        public Speed(@NonNull Integer x) {
            super("speed", x);
        }
    }

    public static final class Flip extends SingleParameterCommand<Character> {
        public Flip(@NonNull Character x) {
            super("flip", x);
        }
    }

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

    public static final class Go extends MultiParameterCommand<Integer> {
        public Go(@NonNull Integer x, @NonNull Integer y, @NonNull Integer z, @NonNull Integer speed) {
            super("go", x, y, z, speed);
        }
    }

    public static final class Curve extends MultiParameterCommand<Integer> {
        public Curve(@NonNull Integer x1, @NonNull Integer y1, @NonNull Integer z1, @NonNull Integer x2, @NonNull Integer y2, @NonNull Integer z2, @NonNull Integer speed) {
            super("curve", x1, y1, z1, x2, y2, z2, speed);
        }
    }
}
