package de.tum.digitalagriculture.streams;

import lombok.Getter;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS;
import static org.opencv.highgui.HighGui.destroyAllWindows;
import static org.opencv.highgui.HighGui.imshow;

public class StreamDisplay implements StreamHandler<StreamDisplay.Stream> {
    private static final Logger logger = LoggerFactory.getLogger(StreamDisplay.class);

    private Stream stream;

    public StreamDisplay() {
        this.stream = null;
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

    protected static class Stream implements StreamHandler.Stream {
        private final AtomicBoolean isActive;
        private final VideoCapture capture;
        @Getter
        private final Double fps;
        private final String url;

        private Stream(String streamUrl) {
            url = streamUrl;
            capture = new VideoCapture(streamUrl);
            fps = capture.get(CAP_PROP_FPS);
            var img = new Mat();
            capture.read(img);
            isActive = new AtomicBoolean(true);
            logger.debug("{}", streamUrl);
            logger.debug("{}", capture.isOpened());
            logger.debug("{}", fps);
        }

        @Override
        public void capture() {
            if (!capture.isOpened()) {
                throw new IllegalStateException("Capture not running!");
            }
            var img = new Mat();
            while (capture.isOpened() && isActive.get()) {
                capture.read(img);
                logger.debug("Capturing");
                imshow("Feed", img);
            }
            capture.release();
            destroyAllWindows();
        }

        @Override
        public void close() {
            isActive.set(false);
            capture.release();
            destroyAllWindows();
        }
    }

}
