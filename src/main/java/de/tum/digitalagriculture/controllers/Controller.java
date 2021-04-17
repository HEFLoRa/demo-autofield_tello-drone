package de.tum.digitalagriculture.controllers;

import de.tum.digitalagriculture.commanders.Commands;
import lombok.NonNull;

public interface Controller {
    Result execute(@NonNull Commands.Command command);
    void readStream();
    void stopStream();
}
