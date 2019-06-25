package com.uddernetworks.emoji.main;

import com.uddernetworks.emoji.emoji.DefaultVideoParser;

import java.io.File;

public class Main {

    /*
     * This requires you to have FFmpeg installed (Including FFprobe), and it is autodetected with the Windows command
     * `where ffmpeg.exe` To be used on other OS's, modify Mp4ProcessableVideo.
     */
    public static void main(String[] args) {
        var parser = new DefaultVideoParser();

        var video = parser.getVideo(new File("videos\\video.mp4"));
        parser.preprocessVideo(video);
    }
}
