package de.tum.digitalagriculture.streams;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_videoio.VideoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

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
        return stream != null && stream.isActive.get();
    }

    @Override
    public void stopStream() {
        stream.isActive.set(false);
    }

    @Override
    public void close() throws FFmpegFrameGrabber.Exception {
        if (stream != null) {
            stream.close();
        }
    }

    protected class Stream implements StreamHandler.Stream {
        private final AtomicBoolean isActive;
        private final FFmpegFrameGrabber capture;
        private final VideoWriter writer;
        private final OpenCVFrameConverter.ToMat converter;
        @Getter
        private final Double fps;

        @SneakyThrows
        private Stream(@NonNull String streamUrl) {
            var url = streamUrl + "?overrun_nonfatal=1";
            converter = new OpenCVFrameConverter.ToMat();
            capture = new FFmpegFrameGrabber(url);
            capture.setNumBuffers(1024);
            capture.start();
            fps = capture.getVideoFrameRate();
            var frame = capture.grabImage();
            var img = converter.convert(frame);
            var codec = VideoWriter.fourcc((byte) 'M', (byte) 'P', (byte) 'J', (byte) 'G');
            writer = new VideoWriter(filename, codec, fps, img.size(), true);
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
                writer.write(img);
            }
            capture.stop();
            capture.release();
            writer.release();
        }

        @Override
        public void close() throws FFmpegFrameGrabber.Exception {
            isActive.set(false);
            capture.stop();
            capture.release();
            writer.release();
        }
    }
}
