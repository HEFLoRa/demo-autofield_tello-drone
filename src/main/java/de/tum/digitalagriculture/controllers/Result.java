package de.tum.digitalagriculture.controllers;

import lombok.NonNull;

public record Result(@NonNull ResultEnum result, @NonNull String response) {
    public enum ResultEnum {
        OK, ERROR, READING
    }

    public static Result parse(String result) {
        return new Result(ResultEnum.OK, "Bla");
    }
}