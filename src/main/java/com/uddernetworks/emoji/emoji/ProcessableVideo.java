package com.uddernetworks.emoji.emoji;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProcessableVideo {

    private static Logger LOGGER = LoggerFactory.getLogger(ProcessableVideo.class);

    private File file;
    private int width;
    private int height;

//    private FrameGrab frameGrab;
//    private AbstractMP4DemuxerTrack track;
    private long frameCount;
    private double fps;
    private FFmpegExecutor executor;

    private long currentFrame = 2;

    public ProcessableVideo(File file) {
        this.file = file;

        try {

            this.executor = new FFmpegExecutor(new FFmpeg("C:\\ffmpeg\\bin\\ffmpeg.exe"), new FFprobe("C:\\ffmpeg\\bin\\ffprobe.exe"));

            this.fps = calculateFPS();
            LOGGER.info("FPS calculated to be {}", this.fps);

//            System.exit(0);

        } catch (IOException e) {
            LOGGER.error("An error occurred while processing the video.");
        }
    }

    private int calculateFPS() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("ffprobe -v 0 -of csv=p=0 -select_streams v:0 -show_entries stream=r_frame_rate " + this.file.getAbsoluteFile(), null, null);

        var out = new StringBuilder();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                out.append(line);
            }
        }

        System.out.println("out = " + out);
        var split = out.toString().split("/");
        var first = Double.valueOf(split[0]);
        var second = Double.valueOf(split[1]);
        var fps = (int) (first / second);
        System.out.println("fps = " + fps);
        return fps;
    }

    public List<BufferedImage> getNextXFrames(int skip, int frames) {
        var outDir = new File("E:\\DiscordEmojiCreator\\genned");
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(file.getAbsolutePath())
                .addOutput(outDir.getAbsolutePath() + "\\%04d.jpeg")
                .setVideoFilter("select=gte(n\\, " + skip + ")")
                .setFrames(frames)
                .addExtraArgs("-vsync", "vfr", "-hide_banner")
                .done();
        executor.createJob(builder).run();

        return Arrays.stream(outDir.listFiles()).sorted(Comparator.comparingInt(file -> Integer.parseInt(file.getName().replace(".jpeg", "")))).map((Function<File, Optional<BufferedImage>>) input -> {
            try {
                return Optional.ofNullable(ImageIO.read(input));
            } catch (IOException e) {
                LOGGER.error("Error while reading generated frame", e);
                return Optional.empty();
            }
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    public File getFile() {
        return file;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

//    public AWTFrameGrab getFrameGrab() {
//        return frameGrab;
//    }

//    public AbstractMP4DemuxerTrack getTrack() {
//        return track;
//    }

    public long getFrameCount() {
        return frameCount;
    }

    public double getFPS() {
        return fps;
    }

    public long getCurrentFrame() {
        return currentFrame;
    }
}
