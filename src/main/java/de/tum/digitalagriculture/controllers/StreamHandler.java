package de.tum.digitalagriculture.controllers;

import java.nio.channels.DatagramChannel;

public interface StreamHandler {
    void startStream(String videoPath);
    void capture();
    float getFps();
    boolean isActive();
    void stopStream();
}
