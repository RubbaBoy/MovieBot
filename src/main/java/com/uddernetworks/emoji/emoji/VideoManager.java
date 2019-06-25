package com.uddernetworks.emoji.emoji;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class VideoManager {

    private static Logger LOGGER = LoggerFactory.getLogger(VideoManager.class);

    public CompletableFuture<Void> downloadVideo(String url, String name) {
        // TODO: Video downloading?
        return CompletableFuture.runAsync(() -> LOGGER.warn("Video downloading is not yet implemented, assuming {} is already existent.", name));
    }

}
