package com.uddernetworks.emoji.main;

import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.gif.VideoGifProcessor;
import com.uddernetworks.emoji.player.video.Video;
import com.uddernetworks.emoji.player.video.VideoCreator;
import com.uddernetworks.emoji.player.video.VideoPlayer;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashMap;

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

    private JDA jda;
    private ConfigManager configManager;
    private FFmpegManager fFmpegManager;
    private VideoGifProcessor videoGifProcessor;
    private VideoCreator videoCreator;
    private HashMap<Long, VideoPlayer> playing = new HashMap<>();

    @Override
    public void onReady(ReadyEvent event) {
        this.jda = event.getJDA();
        LOGGER.info("Bot Ready");

        this.configManager = new ConfigManager();

        this.jda.getGuilds().forEach(this.configManager::loadConfig);

        try {
            this.fFmpegManager = new FFmpegManager();
            this.videoGifProcessor = new VideoGifProcessor(this.fFmpegManager);
            this.videoCreator = new VideoCreator(this.fFmpegManager, this.videoGifProcessor);
        } catch (IOException e) {
            LOGGER.error("There was an error initializing some stuff!", e);
            e.printStackTrace();
        }

        new CommandManager(this);

//        try {
//            testVideo = this.videoCreator.createVideo(new URL("https://rubbaboy.me/files/02emyvx-video.mp4"), "Secret Video", "A secret video; should not be played");
////            testVideo = this.videoCreator.createVideo(new URL("https://rubbaboy.me/files/3udhvjt-video_sync.mp4"), "Audio Sync Test", "A video to help determine if the audio and video is synced.");
//            var channel = event.getJDA().getTextChannelById(591484659913981972L);
//            playing.put(591484659913981972L, new VideoPlayer(Main.testVideo, channel));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public FFmpegManager getfFmpegManager() {
        return fFmpegManager;
    }

    public VideoGifProcessor getVideoGifProcessor() {
        return videoGifProcessor;
    }

    public VideoCreator getVideoCreator() {
        return videoCreator;
    }

    public JDA getJDA() {
        return this.jda;
    }
}
