package com.uddernetworks.emoji.player.video;

import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.gif.VideoGifProcessor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class VideoCreator {

    private static Logger LOGGER = LoggerFactory.getLogger(VideoCreator.class);

    private FFmpegManager fFmpegManager;
    private VideoGifProcessor videoGifProcessor;

    public VideoCreator(FFmpegManager fFmpegManager, VideoGifProcessor videoGifProcessor) {
        this.fFmpegManager = fFmpegManager;
        this.videoGifProcessor = videoGifProcessor;
    }

    public Video createVideo(URL url) throws IOException {
        var file = new File("videos\\" + url.getFile());
        if (!file.exists()) {
            LOGGER.info("Creating {}...", file.getAbsolutePath());
            FileUtils.copyURLToFile(url, file);
        } else {
            LOGGER.info("{} already exists, using local copy...", url);
        }

        return new LocalVideo(this.fFmpegManager, this.videoGifProcessor, file);
    }

    public Video createVideo(File file) throws IOException {
        return new LocalVideo(this.fFmpegManager, this.videoGifProcessor, file);
    }

}
