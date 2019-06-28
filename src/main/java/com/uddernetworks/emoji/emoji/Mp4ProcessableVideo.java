package com.uddernetworks.emoji.emoji;

import com.uddernetworks.emoji.utils.IntPair;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Mp4ProcessableVideo implements ProcessableVideo {

    private static Logger LOGGER = LoggerFactory.getLogger(Mp4ProcessableVideo.class);

    private File file;
    private int width;
    private int height;

    private double fps;
    private FFmpegExecutor executor;

    public Mp4ProcessableVideo(File file) {
        this.file = file;

        try {
            var ffmpegBin = findFFmpegBin().getAbsolutePath();
            LOGGER.info("Found ffmpeg bin location, {}", ffmpegBin);
            this.executor = new FFmpegExecutor(new FFmpeg(ffmpegBin + "\\ffmpeg.exe"), new FFprobe(ffmpegBin + "\\ffprobe.exe"));

            this.fps = calculateFPS();
            LOGGER.info("FPS calculated to be {}", this.fps);

            var dimensions = calculateDimensions();
            this.width = dimensions.getKey();
            this.height = dimensions.getValue();

            LOGGER.info("Width and height calculated to be {}x{}", this.width, this.height);
        } catch (IOException e) {
            LOGGER.error("An error occurred while processing the video.");
        }
    }

    @Override
    public List<BufferedImage> getNextXFrames(int skip, int frames) {
        var outDir = new File("genned");
        outDir.mkdirs();
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(file.getAbsolutePath())
                .addOutput(outDir.getAbsolutePath() + "\\%04d.png")
                .setVideoFilter("select=gte(n\\, " + skip + ")")
                .setFrames(frames)
                .addExtraArgs("-vsync", "vfr", "-hide_banner", "-vf", "scale=320:-1")
                .done();
        executor.createJob(builder).run();

        return Arrays.stream(outDir.listFiles()).sorted(Comparator.comparingInt(file -> Integer.parseInt(file.getName().replace(".png", "")))).map((Function<File, Optional<BufferedImage>>) input -> {
            try {
                return Optional.ofNullable(ImageIO.read(input));
            } catch (IOException e) {
                LOGGER.error("Error while reading generated frame", e);
                return Optional.empty();
            }
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    private File findFFmpegBin() throws IOException {
        var foundMpeg = findFileParent("ffmpeg.exe");
        var foundProbe = findFileParent("ffprobe.exe");
        foundMpeg.retainAll(foundProbe);

        if (foundMpeg.isEmpty()) {
            Mp4ProcessableVideo.LOGGER.error("Couldn't find ffmpeg and/or ffprobe, do you have it installed?");
            System.exit(0);
        }

        return foundMpeg.get(0);
    }

    private List<File> findFileParent(String file) throws IOException {
        return Arrays.stream(runCommand("where " + file).split("\\r?\\n")).map(File::new).map(File::getParentFile).collect(Collectors.toList());
    }

    private IntPair calculateDimensions() throws IOException {
        var split = getProperty("width,height").split(",");
        return new IntPair(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private int calculateFPS() throws IOException {
        var split = getProperty("r_frame_rate").split("/");
        var first = Double.valueOf(split[0]);
        var second = Double.valueOf(split[1]);
        return (int) (first / second);
    }

    private String getProperty(String property) throws IOException {
        return runCommand("ffprobe -v 0 -of csv=p=0 -select_streams v:0 -show_entries stream=" + property + " " + this.file.getAbsoluteFile());
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

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public double getFPS() {
        return fps;
    }
}
