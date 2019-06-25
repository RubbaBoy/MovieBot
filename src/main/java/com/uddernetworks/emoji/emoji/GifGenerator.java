package com.uddernetworks.emoji.emoji;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GifGenerator {

    private static Logger LOGGER = LoggerFactory.getLogger(GifGenerator.class);

    public CompletableFuture<Optional<File>> generateGif(String name, double fps, List<BufferedImage> frames) {
        return CompletableFuture.supplyAsync(() -> {
            var start = System.currentTimeMillis();
            var file = new File("E:\\DiscordEmojiCreator\\gifs\\" + name + ".gif");
            try (FileOutputStream output = new FileOutputStream(file);
            ){

                AnimatedGifEncoder e = new AnimatedGifEncoder();
                e.start(output);
                e.setDelay((int) (1000D / fps));   // 1 frame per sec

                frames.forEach(e::addFrame);

                e.finish();

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
