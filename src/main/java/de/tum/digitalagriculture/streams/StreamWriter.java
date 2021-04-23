package de.tum.digitalagriculture.streams;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_BUFFERSIZE;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS;

public class StreamWriter implements StreamHandler<StreamWriter.Stream>, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(StreamWriter.class);
    @Getter
    @Setter
    private String filename;
    private Stream stream;

    public StreamWriter(String filename) {
        this.filename = filename;
        stream = null;
    }

    @Override
    public Stream startStream(String streamUrl) {
        if (hasActiveStream()) {
            throw new IllegalStateException("startStream cannot be called if a stream is already running!");
        } else if (stream != null) {
            stream.close();
        }
        stream = new Stream(streamUrl);
        return stream;
    }

    @Override
    public Boolean hasActiveStream() {
        return stream != null && stream.isActive.get();
    }

    @Override
    public void stopStream() {
        stream.isActive.set(false);
    }

    @Override
    public void close() {
        if (stream != null) {
            stream.close();
        }
    }

    protected class Stream implements StreamHandler.Stream {
        final AtomicBoolean isActive;
        private final VideoCapture capture;
        private final VideoWriter writer;
        @Getter
        private final Double fps;

        Stream(@NonNull String streamUrl) {
            var url = streamUrl + "?overrun_nonfatal=1&fifi_size=10000000";
            capture = new VideoCapture(url);
            capture.set(CAP_PROP_BUFFERSIZE, 1024);
            var codec = VideoWriter.fourcc('M', 'P', 'J', 'G');
            var img = new Mat();
            capture.read(img);
            fps = capture.get(CAP_PROP_FPS);
            writer = new VideoWriter(filename, codec, fps, img.size(), true);
            isActive = new AtomicBoolean(true);
        }

        @Override
        public void capture() {
            if (!capture.isOpened()) {
                throw new IllegalStateException("Capture not running!");
            }
            var img = new Mat();
            while (isActive.get()) {
                capture.read(img);
                writer.write(img);
            }
        }

        @Override
        public void close() {
            isActive.set(false);
            capture.release();
            writer.release();
        }
    }
}
