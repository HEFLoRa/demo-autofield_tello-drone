package de.tum.digitalagriculture.commanders;

import de.tum.digitalagriculture.controllers.Controller;
import de.tum.digitalagriculture.controllers.PrintController;
import de.tum.digitalagriculture.controllers.Result;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

public class PathCommander implements Commander {
    @Getter
    private final Controller controller;
    @Getter
    @Setter
    private Commands.Command[] commands;

    public PathCommander(@NonNull Controller controller, @NonNull Commands.Command... commands) {
        this.controller = controller;
        this.commands = commands;
    }

    @Override
    public Result execute(Commands.Command command) {
        return controller.execute(command);
    }

    @Override
    public void run() {
        for (var command : commands) {
            execute(command);
        }
        System.out.println("Finished");
    }

    public static void main(String[] args) {
        Commands.Command[] commands = new Commands.Command[]{
                new Commands.TakeOff(),
                new Commands.Go(10, 30, 40, 10),
                new Commands.Up(10),
                new Commands.Left(30)
        };
        var controller = new PrintController(500, TimeUnit.MILLISECONDS);
        var commander = new PathCommander(controller, commands);
        commander.run();
    }
}
