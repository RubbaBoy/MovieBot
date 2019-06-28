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
