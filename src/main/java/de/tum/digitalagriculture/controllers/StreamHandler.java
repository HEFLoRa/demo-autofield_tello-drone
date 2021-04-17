package de.tum.digitalagriculture.controllers;


public interface StreamHandler<S extends StreamHandler.Stream> {
    S startStream(String streamUrl);

    Boolean hasActiveStream();

    void stopStream();

    interface Stream extends AutoCloseable {
        Double getFps();

        void capture();
    }
}
