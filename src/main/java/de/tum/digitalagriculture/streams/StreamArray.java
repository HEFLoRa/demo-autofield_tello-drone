package de.tum.digitalagriculture.streams;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamArray implements StreamHandler<StreamArray.Stream> {
    private static final Logger logger = LoggerFactory.getLogger(StreamArray.class);

    private Stream stream;

    public StreamArray() {
        stream = null;
    }

    @SneakyThrows
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
        return null;
    }

    @Override
    public void stopStream() {
        if (stream != null) {
            stream.isActive.set(false);
        }
    }

    public static class Stream implements StreamHandler.Stream<ArrayList<Mat>> {
        private final AtomicBoolean isActive;
        private final FFmpegFrameGrabber capture;
        private final OpenCVFrameConverter.ToMat converter;
        private final ArrayList<Mat> data;

        @SneakyThrows
        private Stream(@NonNull String streamUrl) {
            var url = streamUrl + "?overrun_nonfatal=1";
            converter = new OpenCVFrameConverter.ToMat();
            capture = new FFmpegFrameGrabber(url);
            capture.setNumBuffers(1024);
            capture.start();
            data = new ArrayList<>();
            isActive = new AtomicBoolean(true);

        }

        @SneakyThrows
        @Override
        public void capture() {
            if (!capture.hasVideo()) {
                throw new IllegalStateException("Capture not running!");
            }
            while (capture.hasVideo() && isActive.get()) {
                var frame = capture.grabImage();
                var img = converter.convert(frame);
                data.add(img);
            }
            capture.stop();
            capture.release();
        }

        @Override
        public ArrayList<Mat> getData() {
            return data;
        }

        @Override
        public void close() throws Exception {
            isActive.set(false);
            capture.stop();
            capture.release();
        }
    }
}
