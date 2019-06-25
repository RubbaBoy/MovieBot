package com.uddernetworks.emoji.emoji;

import com.uddernetworks.emoji.utils.IntPair;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface ProcessableVideo {

    /**
     * Gets frames of the video.
     *
     * @param skip The amount of frames to skip, 0 starting at the beginning of the video
     * @param frames The amount of frames to fetch
     * @return A list of frames with the same size as `frames`
     */
    List<BufferedImage> getNextXFrames(int skip, int frames);

    /**
     * Gets the video  file.
     *
     * @return The file of the video
     */
    File getFile();

    /**
     * Gets the width in pixels the video is.
     *
     * @return The width of the video in pixels
     */
    int getWidth();

    /**
     * Gets the height in pixels the video is.
     *
     * @return The height of the video in pixels
     */
    int getHeight();

    /**
     * Gets the FPS of the video. With some video formats, this may be an average, as some formats don't have
     * frames due to compression or something like that (I'm trying to remember the stuff I saw from StackOverflow
     * a few hours ago, if you want a better reason look it up yourself)
     *
     * @return The FPS the video is
     */
    double getFPS();
}
