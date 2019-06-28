package com.uddernetworks.emoji.gif;

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

    private FFmpegExecutor executor;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        LOGGER.info("Making 5 gifs, each 5 seconds long, one after another");

        var video = new File("videos\\video.mp4");
        var processor = new VideoGifProcessor();

        for (int i = 0; i < 5; i++) {
            var start = System.currentTimeMillis();
            var gif = processor.convertVideoToGif(video, i * 5, 5).get();
            LOGGER.info("Created gif {} in {}ms", gif.getName(), System.currentTimeMillis() - start);
        }
    }

    public VideoGifProcessor() throws IOException {
        var ffmpegBin = findFFmpegBin().getAbsolutePath();
        LOGGER.info("Found ffmpeg bin location, {}", ffmpegBin);
        this.executor = new FFmpegExecutor(new FFmpeg(ffmpegBin + "\\ffmpeg.exe"), new FFprobe(ffmpegBin + "\\ffprobe.exe"));
    }

    public CompletableFuture<File> convertVideoToGif(File video, int offset, int duration) {
        return CompletableFuture.supplyAsync(() -> {
            var outDir = new File("gifs");
            outDir.mkdirs();
            var outFile = new File(outDir.getAbsolutePath(), + offset + "_" + duration + ".gif");
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(video.getAbsolutePath())
                    .addOutput(outFile.getAbsolutePath())
                    .addExtraArgs("-r", "10", "-hide_banner", "-vf", "scale=320:-1", "-ss", String.valueOf(offset), "-t", String.valueOf(duration))
                    .done();
            executor.createJob(builder).run();
            return outFile;
        });

    }

    private File findFFmpegBin() throws IOException {
        var foundMpeg = findFileParent("ffmpeg.exe");
        var foundProbe = findFileParent("ffprobe.exe");
        foundMpeg.retainAll(foundProbe);

        if (foundMpeg.isEmpty()) {
            LOGGER.error("Couldn't find ffmpeg and/or ffprobe, do you have it installed?");
            System.exit(0);
        }

        return foundMpeg.get(0);
    }

    private List<File> findFileParent(String file) throws IOException {
        return Arrays.stream(runCommand("where " + file).split("\\r?\\n")).map(File::new).map(File::getParentFile).collect(Collectors.toList());
    }

    private String runCommand(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command, null, null);

        var out = new StringBuilder();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) out.append(line).append('\n');
        }

        return out.toString().trim();
    }

}
