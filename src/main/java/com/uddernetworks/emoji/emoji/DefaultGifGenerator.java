package com.uddernetworks.emoji.emoji;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DefaultGifGenerator implements GifGenerator {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultGifGenerator.class);

    @Override
    public CompletableFuture<Optional<File>> generateGif(String name, double fps, List<BufferedImage> frames) {
        return CompletableFuture.supplyAsync(() -> {
            var start = System.currentTimeMillis();
            var file = new File("gifs\\" + name + ".gif").getAbsoluteFile();
            file.getParentFile().mkdirs();
            try (ImageOutputStream output = new FileImageOutputStream(file);
                 GifSequenceWriter writer = new GifSequenceWriter(output, frames.get(0).getType(), (int) (1000D / fps), true)) {

                for (BufferedImage frame : frames) {
                    writer.writeToSequence(frame);
                }

                return Optional.of(file);
            } catch (IOException e) {
                LOGGER.error("An error occurred while creating a gif", e);
                return Optional.empty();
            } finally {
                LOGGER.info("Gif generation took {}ms", System.currentTimeMillis() - start);
            }
        });
    }

}
