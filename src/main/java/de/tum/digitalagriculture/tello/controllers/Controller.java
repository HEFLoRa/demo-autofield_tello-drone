package de.tum.digitalagriculture.tello.controllers;

import de.tum.digitalagriculture.tello.commanders.Commands;
import lombok.NonNull;

public interface Controller {
    Result execute(@NonNull Commands.Command command);
}
