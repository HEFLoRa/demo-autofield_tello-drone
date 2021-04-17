package de.tum.digitalagriculture.commanders;

import de.tum.digitalagriculture.controllers.Result;

public interface Commander {
    Result execute(Commands.Command command);

    void run();
}
