package com.uddernetworks.emoji.main;

import com.uddernetworks.emoji.emoji.DefaultGifGenerator;
import com.uddernetworks.emoji.emoji.DefaultVideoParser;
import com.uddernetworks.emoji.emoji.ProcessableVideo;
import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.gif.VideoGifProcessor;
import com.uddernetworks.emoji.player.AudioPlayer;
import com.uddernetworks.emoji.player.Video;
import com.uddernetworks.emoji.player.VideoPlayer;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main extends ListenerAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static Video testVideo;

    public static Video getTestVideo() {
        return testVideo;
    }

    /*
     * This requires you to have FFmpeg installed (Including FFprobe), and it is autodetected with the Windows command
     * `where ffmpeg.exe` To be used on other OS's, modify Mp4ProcessableVideo.
     */
    public static void main(String[] args) throws LoginException {
        new JDABuilder(AccountType.BOT)
                // set the token
                .setToken(args[0])

                // set the game for when the bot is loading
                .setStatus(OnlineStatus.ONLINE)
                .setGame(Game.playing("your favorite movie!"))

                // add the listeners
                .addEventListener(new Main())
                .build();
    }

    private FFmpegManager fFmpegManager;
    private VideoGifProcessor videoGifProcessor;
    private HashMap<Long, VideoPlayer> playing = new HashMap<>();

    @Override
    public void onReady(ReadyEvent event) {
        LOGGER.info("Bot Ready");

        try {
            this.fFmpegManager = new FFmpegManager();
            this.videoGifProcessor = new VideoGifProcessor(this.fFmpegManager);
        } catch (IOException e) {
            LOGGER.error("There was an error initializing some stuff!", e);
            e.printStackTrace();
        }

        try {
            var videoFile = new File("videos\\video.mp4");
            Main.testVideo = new Video(this.fFmpegManager, this.videoGifProcessor, videoFile, "Avengers Endgame", "test123");
            var channel = event.getJDA().getTextChannelById(591484659913981972L);
            playing.put(591484659913981972L, new VideoPlayer(Main.testVideo, channel));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

    }
}
