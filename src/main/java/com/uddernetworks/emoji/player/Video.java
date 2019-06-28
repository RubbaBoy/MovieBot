package com.uddernetworks.emoji.player;

import com.uddernetworks.emoji.emoji.DefaultGifGenerator;
import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Video {

    private static Logger LOGGER = LoggerFactory.getLogger(Video.class);

    private String title;
    private String description;
    private File videoFile;
    private int duration = 0;

    public Video(FFmpegManager fFmpegManager, File videoFile, String title, String description) {
        this.title = title;
        this.description = description;
        this.videoFile = videoFile;

        try {
            this.duration = Double.valueOf(fFmpegManager.getProperty("duration", this.videoFile)).intValue();
        } catch (IOException e) {
            LOGGER.error("There was an error while calculating the duration of the video " + videoFile.getAbsolutePath(), e);
        }
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
}
