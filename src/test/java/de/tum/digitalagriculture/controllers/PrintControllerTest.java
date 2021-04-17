package de.tum.digitalagriculture.controllers;

import de.tum.digitalagriculture.commanders.Commands;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PrintControllerTest {

    @Test
    void execute() {
        var controller = new PrintController(0 , TimeUnit.MILLISECONDS);
        assertEquals(new Result(Result.ResultEnum.OK, "ok"), controller.execute(new Commands.ReadBattery()));
    }
}