package de.tum.digitalagriculture.streams;


@SuppressWarnings("rawtypes")
public interface StreamHandler<S extends StreamHandler.Stream> {
    S startStream(String streamUrl);

    Boolean hasActiveStream();

    void stopStream();

    interface Stream<D> extends AutoCloseable {
        void capture();

        D getData();
    }
}
