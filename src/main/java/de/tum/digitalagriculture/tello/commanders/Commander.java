package de.tum.digitalagriculture.tello.commanders;

import java.util.Iterator;

/**
 *     Interface that decides which commands should be executed on the Drone.
 *     <p>
 *         Implements the {@link Iterator} interface. To run the commander until it is finished simply run:
 *         <pre>
 *             {@code commander.forEachRemaining(controller);}
 *         </pre>
 *     <p/>
 */
public interface Commander extends Iterator<Commands.Command> {}
