package com.uddernetworks.emoji.ffmpeg;

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
import java.util.stream.Collectors;

public class FFmpegManager {

    private static Logger LOGGER = LoggerFactory.getLogger(FFmpegManager.class);

    private FFmpegExecutor executor;

    public FFmpegManager() throws IOException {
        var ffmpegBin = findFFmpegBin().getAbsolutePath();
        LOGGER.info("Found ffmpeg bin location, {}", ffmpegBin);
        this.executor = new FFmpegExecutor(new FFmpeg(ffmpegBin + "\\ffmpeg.exe"), new FFprobe(ffmpegBin + "\\ffprobe.exe"));
    }

    public void createJob(FFmpegBuilder builder) {
        this.executor.createJob(builder).run();
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

    public String getProperty(String property, File video) throws IOException {
        return runCommand("ffprobe -v 0 -of csv=p=0 -select_streams v:0 -show_entries stream=" + property + " " + video.getAbsolutePath());
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
