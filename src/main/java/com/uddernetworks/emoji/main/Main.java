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
    private AudioPlayer audioPlayer;

    private DefaultVideoParser parser;
    private ProcessableVideo video;
    private HashMap<Long, VideoPlayer> playing = new HashMap<>();
    private int nextGif = 0;

    @Override
    public void onReady(ReadyEvent event) {
        LOGGER.info("Bot Ready");

        try {
            this.fFmpegManager = new FFmpegManager();
            this.videoGifProcessor = new VideoGifProcessor(this.fFmpegManager);
            this.audioPlayer = new AudioPlayer(this.fFmpegManager);
        } catch (IOException e) {
            LOGGER.error("There was an error initializing some stuff!", e);
            System.exit(0);
        }

        playing.put(591484659913981972L, new VideoPlayer(new Video(null, "Rick roll", "test123"), event.getJDA().getTextChannelById(591484659913981972L)));

        LOGGER.info("Pre-processing video...");
        parser = new DefaultVideoParser();

        video = parser.getVideo(new File("videos\\video.mp4"));
        parser.preprocessVideo(video, 0);

        LOGGER.info("Starting movie...");
        CompletableFuture.runAsync(this::runUpdate);
    }

    private void runUpdate() {
        long start = System.currentTimeMillis();


        // Wait for next time to update
        try {
            Thread.sleep(15 * 1000 /* 15 seconds */ - (System.currentTimeMillis() - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        CompletableFuture.runAsync(this::runUpdate);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

    }
}
