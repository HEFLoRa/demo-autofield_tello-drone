package de.tum.digitalagriculture.tello.commanders;

import de.tum.digitalagriculture.tello.controllers.Result;

public interface Commander {
    Result execute(Commands.Command command);

    void run();
}
