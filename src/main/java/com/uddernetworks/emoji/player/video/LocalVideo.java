package com.uddernetworks.emoji.player.video;

import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.gif.VideoGifProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class LocalVideo implements Video {

    private static Logger LOGGER = LoggerFactory.getLogger(LocalVideo.class);

    private String title;
    private File videoFile;
    private int duration = 0;
    private FFmpegManager fFmpegManager;
    private VideoGifProcessor videoGifProcessor;

    public LocalVideo(FFmpegManager fFmpegManager, VideoGifProcessor videoGifProcessor, File videoFile) {
        this.videoFile = videoFile;
        this.fFmpegManager = fFmpegManager;
        this.videoGifProcessor = videoGifProcessor;

        try {
            var metadata = fFmpegManager.getMetadata(this.videoFile);
            this.title = metadata.get("TAG:title");
            this.duration = Double.valueOf(metadata.get("duration")).intValue();
        } catch (IOException e) {
            LOGGER.error("There was an error while calculating video meta data " + videoFile.getAbsolutePath(), e);
        }
    }

    @Override
    public FFmpegManager getfFmpegManager() {
        return fFmpegManager;
    }

    @Override
    public VideoGifProcessor getVideoGifProcessor() {
        return videoGifProcessor;
    }

    @Override
    public File getVideoFile() {
        return videoFile;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getLength() {
        return this.duration;
    }

    @Override
    public CompletableFuture<File> convertToGif(int offset, int duration) {
        return this.videoGifProcessor.convertVideoToGif(this.videoFile, offset, duration);
    }
}
