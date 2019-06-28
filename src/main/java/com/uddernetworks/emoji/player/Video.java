package com.uddernetworks.emoji.player;

import java.io.File;

public class Video {

    private String title;
    private String description;
    private File videoFile;

    public Video(File videoFile, String title, String description) {
        this.title = title;
        this.description = description;
        this.videoFile = videoFile;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getLength() {
        // TODO
        return 1;
    }
}
