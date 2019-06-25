package com.uddernetworks.emoji.emoji;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface GifGenerator {
    /**
     * Generates a gif with the given name in the /gifs/ directory.
     *
     * @param name The filename of the gif
     * @param fps The FPS of the original media
     * @param frames The frames to compile the gif out of
     * @return The File of the gif, if successful
     */
    CompletableFuture<Optional<File>> generateGif(String name, double fps, List<BufferedImage> frames);
}
