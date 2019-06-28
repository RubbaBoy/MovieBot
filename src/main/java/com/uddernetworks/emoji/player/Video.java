package com.uddernetworks.emoji.player;

import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.gif.VideoGifProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Video {

    private static Logger LOGGER = LoggerFactory.getLogger(Video.class);

    private String title;
    private String description;
    private File videoFile;
    private int duration = 0;
    private FFmpegManager fFmpegManager;
    private VideoGifProcessor videoGifProcessor;

    public Video(FFmpegManager fFmpegManager, VideoGifProcessor videoGifProcessor, File videoFile, String title, String description) {
        this.title = title;
        this.description = description;
        this.videoFile = videoFile;
        this.fFmpegManager = fFmpegManager;
        this.videoGifProcessor = videoGifProcessor;

        try {
            this.duration = Double.valueOf(fFmpegManager.getProperty("duration", this.videoFile)).intValue();
        } catch (IOException e) {
            LOGGER.error("There was an error while calculating the duration of the video " + videoFile.getAbsolutePath(), e);
        }
    }

    public FFmpegManager getfFmpegManager() {
        return fFmpegManager;
    }

    public VideoGifProcessor getVideoGifProcessor() {
        return videoGifProcessor;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the length of the video, in seconds.
     *
     * @return The seconds of the video
     */
    public int getLength() {
        return this.duration;
    }

    public CompletableFuture<File> convertToGif(int offset, int duration) {
        return this.videoGifProcessor.convertVideoToGif(this.videoFile, offset, duration);
    }
}
