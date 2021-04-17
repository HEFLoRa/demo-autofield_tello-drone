package de.tum.digitalagriculture.controllers;

import java.nio.channels.DatagramChannel;

public interface StreamHandler {
    void startStream(DatagramChannel channel);
    void stopStream();
}
