package de.tum.digitalagriculture.controllers;

import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class StreamWriter implements StreamHandler, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(StreamWriter.class);
    @Getter
    private final String filename;
    @Getter
    private final float fps;
    private VideoCapture capture;
    private final VideoWriter writer;
    private final AtomicBoolean isActive;

    public StreamWriter(String filename, float fps) {
        this.filename = filename;
        this.fps = fps;
        capture = null;
        isActive = new AtomicBoolean(false);
        writer = new VideoWriter();
    }

    @Override
    @Synchronized
    public void startStream(@NonNull String videoPath) {
        if (capture != null || !isActive.compareAndSet(false, true)) {
            throw new IllegalStateException("There is already a capture active");
        }
        capture = new VideoCapture(videoPath);
        if (!writer.isOpened()) {
            var codec = VideoWriter.fourcc('M', 'J', 'P', 'G');
            var mat = new Mat();
            capture.read(mat);
            writer.open(filename, codec, fps, mat.size(), true);
        }
    }

    @Override
    @Synchronized
    public void capture() {
        if (capture == null || !isActive()) {
            throw new IllegalStateException("Capture not running!");
        }
        var mat = new Mat();
        capture.read(mat);
        if (mat.empty()) {
            logger.debug("No image captured!");
        }
        writer.write(mat);
    }

    @Override
    public boolean isActive() {
        return isActive.get();
    }

    @Override
    @Synchronized
    public void stopStream() {
        if (capture == null || !isActive.compareAndSet(true, false)) {
            throw new IllegalStateException("No capture active!");
        }
        capture.release();
        capture = null;
    }

    @Override
    public void close() throws Exception {
        if (capture != null) {
            capture.release();
        }
        writer.release();
    }
}
