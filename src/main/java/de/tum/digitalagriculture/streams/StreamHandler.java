package de.tum.digitalagriculture.streams;


public interface StreamHandler<S extends StreamHandler.Stream> {
    S startStream(String streamUrl);

    Boolean hasActiveStream();

    void stopStream();

    interface Stream extends AutoCloseable {
        Double getFps();

        void capture();
    }
}
