package de.tum.digitalagriculture.tello.streams;


/**
 * A handler that manages a running stream
 * @param <S> the stream type
 */
@SuppressWarnings("rawtypes")
public interface StreamHandler<S extends StreamHandler.Stream> {
    /**
     * Start a new stream @ {@code streamUrl}
     * @param streamUrl url of the stream
     * @return the opened stream
     */
    S startStream(String streamUrl);

    /**
     * Exists a running stream?
     *
     * @return whether this handler has a currently active stream
     */
    Boolean hasActiveStream();

    /**
     * Stop a running stream
     */
    void stopStream();

    /**
     * The class that captures the drones video stream
     * @param <D> the type of data that the stream returns
     */
    interface Stream<D> extends AutoCloseable {
        /**
         * Capture one image of the stream
         */
        void capture();

        /**
         * Get the data of the stream
         *
         * @return relevant data of this type of stream
         */
        D getData();
    }
}
