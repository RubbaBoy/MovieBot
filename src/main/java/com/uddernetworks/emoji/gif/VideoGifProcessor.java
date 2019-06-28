package com.uddernetworks.emoji.gif;

import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// This class was written hastily, and doesn't follow good code-reusing techniques and whatever, and assumes the com.uddernetworks.emoji.emoji package is gone
public class VideoGifProcessor {

    private static Logger LOGGER = LoggerFactory.getLogger(VideoGifProcessor.class);
    private FFmpegManager fFmpegManager;


    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        LOGGER.info("Making 5 gifs, each 5 seconds long, one after another");

//        var video = new File("videos\\video.mp4");
//        var processor = new VideoGifProcessor();
//
//        for (int i = 0; i < 5; i++) {
//            var start = System.currentTimeMillis();
//            var gif = processor.convertVideoToGif(video, i * 5, 5).get();
//            LOGGER.info("Created gif {} in {}ms", gif.getName(), System.currentTimeMillis() - start);
//        }
    }

    public VideoGifProcessor(FFmpegManager fFmpegManager) throws IOException {
        this.fFmpegManager = fFmpegManager;
    }

    public CompletableFuture<File> convertVideoToGif(File video, int offset, int duration) {
        return CompletableFuture.supplyAsync(() -> {
            var outDir = new File("gifs");
            outDir.mkdirs();
            var outFile = new File(outDir.getAbsolutePath(), + offset + "_" + duration + ".gif");
            this.fFmpegManager.createJob(new FFmpegBuilder()
                    .setInput(video.getAbsolutePath())
                    .addOutput(outFile.getAbsolutePath())
                    .addExtraArgs("-r", "10", "-hide_banner", "-vf", "scale=320:-1", "-ss", String.valueOf(offset), "-t", String.valueOf(duration))
                    .done());
            return outFile;
        });

    }

}
