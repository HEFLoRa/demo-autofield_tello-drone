package de.tum.digitalagriculture.streams;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.bytedeco.opencv.global.opencv_highgui.*;

public class StreamDisplay implements StreamHandler<StreamDisplay.Stream> {
    private static final Logger logger = LoggerFactory.getLogger(StreamDisplay.class);

    private Stream stream;

    public StreamDisplay() {
        this.stream = null;
    }

    @Override
    @SneakyThrows
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
        if (stream != null) {
            stream.isActive.set(false);
        }
    }

    public static class Stream implements StreamHandler.Stream<Void> {
        private final AtomicBoolean isActive;
        private final FFmpegFrameGrabber capture;
        @Getter
        private final Double fps;

        @SneakyThrows
        private Stream(String streamUrl) {
            // Use small buffer size to decrease latency
            capture = new FFmpegFrameGrabber(streamUrl + "?fifo_size=0&overrun_nonfatal=1");
            capture.setNumBuffers(0);
            capture.start();
            fps = capture.getFrameRate();
            isActive = new AtomicBoolean(true);
        }

        @Override
        @SneakyThrows
        public void capture() {
            if (!capture.hasVideo()) {
                throw new IllegalStateException("Capture not running!");
            }
            var converter = new OpenCVFrameConverter.ToMat();
            while (capture.hasVideo() && isActive.get()) {
                var frame = capture.grabImage();
                var img = converter.convert(frame);
                imshow("Feed", img);
                waitKey(1000 / fps.intValue());
            }
            capture.stop();
            capture.release();
            destroyAllWindows();
        }

        @Override
        public Void getData() {
            return null;
        }

        @Override
        public void close() throws FrameGrabber.Exception {
            isActive.set(false);
            capture.stop();
            capture.release();
            capture.close();
            destroyAllWindows();
        }
    }

}
