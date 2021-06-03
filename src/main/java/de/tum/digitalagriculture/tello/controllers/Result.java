package de.tum.digitalagriculture.tello.controllers;

import de.tum.digitalagriculture.tello.commanders.Commands;
import lombok.NonNull;

/**
 * The result of an execution
 */
public record Result(@NonNull ResultEnum result, @NonNull String response) {

    /**
     * Parse the result to the executed {@code command}
     *
     * @param command the command that was executed
     * @param response the response the drone sent
     * @return the result of the execution
     */
    public static Result of(Commands.Command command, String response) {
        return new Result(ResultEnum.OK, response);
    }

    /**
     * What kind of response was returned? Can be a success, failure or a data reading
     */
    public enum ResultEnum {
        OK, ERROR, READING
    }
}