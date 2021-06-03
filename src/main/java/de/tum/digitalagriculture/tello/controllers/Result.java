package de.tum.digitalagriculture.tello.controllers;

import de.tum.digitalagriculture.tello.commanders.Commands;
import lombok.NonNull;

public record Result(@NonNull ResultEnum result, @NonNull String response) {
    public static Result of(Commands.Command command, String response) {
        return new Result(ResultEnum.OK, response);
    }

    public enum ResultEnum {
        OK, ERROR, READING
    }
}