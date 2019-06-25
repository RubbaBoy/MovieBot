package com.uddernetworks.emoji.emoji;

import com.uddernetworks.emoji.utils.IntPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.GCMParameterSpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class VideoParser {

    private static Logger LOGGER = LoggerFactory.getLogger(VideoParser.class);

    private static final double FRAMES_PER_GIF = 100;

    private VideoManager videoManager;
    private GifGenerator gifGenerator;

    public VideoParser() {
        this.videoManager = new VideoManager();
        this.gifGenerator = new GifGenerator();
    }

    public void preprocessVideo(ProcessableVideo video) {
        Optional<BufferedImage> frameOptional;
        var separatedGifs = new HashMap<IntPair, List<BufferedImage>>();
        var frameNum = 0;
        var total = System.currentTimeMillis();

        var start = System.currentTimeMillis();
        var tempList = new ArrayList<>(video.getNextXFrames(25 * 5 /* Skip 5 seconds */, (int) FRAMES_PER_GIF));
        LOGGER.info("Took {}ms to fetch {} frames", System.currentTimeMillis() - start, FRAMES_PER_GIF);

//        while ((frameOptional = video.getNextFrame()).isPresent() && frameNum++ < FRAMES_PER_GIF) {
//            var frame = frameOptional.get();
//            tempList.add(frame);

//            try {
//                ImageIO.write(frame, "png", new File("E:\\DiscordEmojiCreator\\gifs\\frame_" + frameNum + ".png"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

//            var start = System.currentTimeMillis();
//            var optionalSeparation = separateImages(frame);
//            LOGGER.info("Took {}ms to parse one {}x{} frame into {} sections.", System.currentTimeMillis() - start, video.getWidth(), video.getHeight(), optionalSeparation.map(Map::size).orElse(-1));
//
//            if (optionalSeparation.isEmpty()) {
//                LOGGER.error("The frame couldn't be separated!");
//                return;
//            }
//
//            var separation = optionalSeparation.get();
//
//            separation.forEach((coords, section) -> {
//                separatedGifs.computeIfAbsent(coords, i -> new ArrayList<>()).add(section);
//            });

//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        LOGGER.info("Processed {} frames @ {}/second", FRAMES_PER_GIF, FRAMES_PER_GIF / ((double) (System.currentTimeMillis() - total)));

        double delay = 1000D / video.getFPS();

        try {
            this.gifGenerator.generateGif("_____test", video.getFPS(), tempList).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        var futures = new ArrayList<CompletableFuture<Optional<File>>>();

        total = System.currentTimeMillis();

//        final int[] num = {0};
//        separatedGifs.forEach((coords, frames) -> {
//            String str = String.format("%03d", num[0]++);
//
//            futures.add(this.gifGenerator.generateGif(str, 1000D / video.getFPS(), frames));
//        });
//
//        while (futures.stream().anyMatch(Predicate.not(CompletableFuture::isDone))) {
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException ignored) {}
//        }
//
//        LOGGER.info("Finished gif generation after a total of {}ms", System.currentTimeMillis() - total);
    }

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

    public ProcessableVideo getVideo(File file) {
        return new ProcessableVideo(file);
    }

    public VideoManager getVideoManager() {
        return videoManager;
    }

    public GifGenerator getGifGenerator() {
        return gifGenerator;
    }
}
