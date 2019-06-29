package com.uddernetworks.emoji.main;

import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.gif.VideoGifProcessor;
import com.uddernetworks.emoji.player.video.LocalVideo;
import com.uddernetworks.emoji.player.video.Video;
import com.uddernetworks.emoji.player.video.VideoCreator;
import com.uddernetworks.emoji.player.video.VideoPlayer;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
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
import java.util.Objects;

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
                .setGame(Game.watching("0 movies!"))

                // add the listeners
                .addEventListener(new Main())
                .build();
    }

    private JDA jda;
    private ConfigManager configManager;
    private FFmpegManager fFmpegManager;
    private VideoGifProcessor videoGifProcessor;
    private VideoCreator videoCreator;
    private HashMap<String, VideoPlayer> playing = new HashMap<>();
    private List<Video> videos = new ArrayList<>();

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

        var videoDirectory = new File("videos");
        videoDirectory.mkdirs();

        for (var file : Objects.requireNonNull(videoDirectory.listFiles())) {
            if (file.isDirectory()) continue;

            try {
                videos.add(this.videoCreator.createVideo(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("Got videos " + videos.size());

        new CommandManager(this);
    }

    public HashMap<String, VideoPlayer> getPlaying() {
        return playing;
    }

    public List<Video> getLoadedVideos() {
        return videos;
    }

    public Video getVideo(String name) {
        for (var video : videos)
            if (video.getTitle().toLowerCase().equals(name.toLowerCase()))
                return video;
        return null;
    }

    public VideoPlayer playVideo(Guild guild, Video video) throws IOException {
        var channelId = configManager.getValue(guild, "textchannel").orElse(null);

        if (channelId == null) return null;

        var channel = guild.getTextChannelById(channelId);

        if (channel == null) return null;

        var player = new VideoPlayer(this, video, channel);
        playing.put(guild.getId(), player);

        jda.getPresence().setGame(Game.watching(playing.size() + " movies!"));

        return player;
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

    public void removePlayer(VideoPlayer videoPlayer) {
        this.playing.remove(videoPlayer.getChannel().getGuild().getId());

        jda.getPresence().setGame(Game.watching(playing.size() + " movies!"));
    }
}
