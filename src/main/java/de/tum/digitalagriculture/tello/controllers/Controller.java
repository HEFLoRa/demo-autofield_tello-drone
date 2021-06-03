package de.tum.digitalagriculture.tello.controllers;

import de.tum.digitalagriculture.tello.commanders.Commands;

import java.util.function.Consumer;

/**
 * {@code Controller} executes the provided command. It extends the {@link Consumer} interface
 */
public interface Controller extends Consumer<Commands.Command> {}
