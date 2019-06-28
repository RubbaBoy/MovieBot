package com.uddernetworks.emoji.player.video;

import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.gif.VideoGifProcessor;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface Video {

    FFmpegManager getfFmpegManager();

    VideoGifProcessor getVideoGifProcessor();

    File getVideoFile();

    String getTitle();

    String getDescription();

    /**
     * Returns the length of the video, in seconds.
     *
     * @return The seconds of the video
     */
    int getLength();

    CompletableFuture<File> convertToGif(int offset, int duration);
}
