package com.uddernetworks.emoji.emoji;

import com.uddernetworks.emoji.utils.IntPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class DefaultVideoParser implements VideoParser {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultVideoParser.class);

    public static final double FRAMES_PER_GIF = 100;

    private GifGenerator gifGenerator;

    public DefaultVideoParser() {
        this.gifGenerator = new DefaultGifGenerator();
    }

    // TODO: Implementation currently shows the first 100 frames of the input video, after 25 * 5 frames (5 seconds).
    //       When the bot functionality is added, full video will be added, which will go with the emoji distribution
    //       system and stuff.

    @Override
    public void preprocessVideo(ProcessableVideo video) {
        var separatedGifs = new HashMap<IntPair, List<BufferedImage>>();
        var total = System.currentTimeMillis();

        var start = System.currentTimeMillis();
        var frames = video.getNextXFrames(25 * 5 /* Skip 5 seconds */, (int) FRAMES_PER_GIF);
        LOGGER.info("Took {}ms to fetch {} frames", System.currentTimeMillis() - start, FRAMES_PER_GIF);

        frames.forEach(frame -> separateImages(frame).ifPresentOrElse(separation -> {
            separation.forEach((coords, section) -> separatedGifs.computeIfAbsent(coords, i -> new ArrayList<>()).add(section));
        }, () -> LOGGER.error("The frame couldn't be separated!")));

        double duration = System.currentTimeMillis() - total;
        LOGGER.info("Processed {} frames @ {}/second over {}ms", FRAMES_PER_GIF, FRAMES_PER_GIF / duration, duration);

        var futures = new ArrayList<CompletableFuture<Optional<File>>>();

        total = System.currentTimeMillis();

        final int[] num = {0};
        separatedGifs.forEach((coords, sectionedFrames) ->
                futures.add(this.gifGenerator.generateGif(String.format("%03d", num[0]++), video.getFPS(), sectionedFrames)));

        while (futures.stream().anyMatch(Predicate.not(CompletableFuture::isDone))) sleep(500);

        LOGGER.info("Finished gif generation after a total of {}ms", System.currentTimeMillis() - total);
    }

    @Override
    public Optional<Map<IntPair, BufferedImage>> separateImages(BufferedImage input) {
        // This assumes a 16:9 ratio
        if (input.getWidth() / 16D != input.getHeight() / 9D) {
            LOGGER.error("The aspect ratio of the video is not 16:9");
            return Optional.empty();
        }

        var iconSize = (int) (input.getWidth() / 16D);

        var col = input.getWidth() / iconSize;
        var row = input.getHeight() / iconSize;

        var output = new HashMap<IntPair, BufferedImage>();

        for (int yBlock = 0; yBlock < row; yBlock++) {
            for (int xBlock = 0; xBlock < col; xBlock++) {
                BufferedImage subImage = input.getSubimage(xBlock * iconSize, yBlock * iconSize, iconSize, iconSize);
                output.put(new IntPair(xBlock, yBlock), subImage);
            }
        }

        return Optional.of(output);
    }

    @Override
    public ProcessableVideo getVideo(File file) {
        return new Mp4ProcessableVideo(file);
    }

    @Override
    public GifGenerator getGifGenerator() {
        return gifGenerator;
    }

    private static void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ignored) {}
    }

}
