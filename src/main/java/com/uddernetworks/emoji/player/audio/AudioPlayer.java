package com.uddernetworks.emoji.player.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.main.Main;
import com.uddernetworks.emoji.player.video.Video;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AudioPlayer extends ListenerAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger(AudioPlayer.class);

    private final AudioPlayerManager playerManager;

    private FFmpegManager fFmpegManager;
    private JDA jda;
    private TextChannel general;
    private VoiceChannel listen;
    private AudioTrack currentTrack;
    private GuildMusicManager musicManager;
    private boolean playing = false;

    public AudioPlayer(FFmpegManager fFmpegManager, JDA jda) {
        this.fFmpegManager = fFmpegManager;
        (this.jda = jda).addEventListener(this);
        this.general = this.jda.getTextChannelById(591482977989427211L); // TODO: Make this configurable or something in prod
        this.listen = this.jda.getVoiceChannelById(591484863455166474L); // TODO: Make this configurable or something in prod

        AudioSourceManagers.registerLocalSource(this.playerManager = new DefaultAudioPlayerManager());
    }

    public VoiceChannel getVoiceChannel() {
        return listen;
    }

    public void disconnect() {
        this.listen.getGuild().getAudioManager().closeAudioConnection();
        this.musicManager = null;
        this.currentTrack = null;
    }

    private CompletableFuture<File> generateAudio(Video video) {
        return CompletableFuture.supplyAsync(() -> {
            var outDir = new File("audio");
            outDir.mkdirs();
            var outFile = new File(outDir, video.getVideoFile().getName() + ".mp3");
            if (outFile.exists()) {
                LOGGER.info("{} already exists, skipping!", outFile.getAbsolutePath());
                return outFile;
            }

            this.fFmpegManager.createJob(new FFmpegBuilder()
                    .setInput(video.getVideoFile().getAbsolutePath())
                    .addOutput(outFile.getAbsolutePath())
                    .addExtraArgs("-f", "mp3", "-ab", "192000")
                    .done());
            return outFile;
        });
    }

    /**
     * Should only be ran once.
     *
     * @param video The video
     */
    public void initialPlay(Video video) {
        generateAudio(video).thenAccept(file -> {
            try {
                LOGGER.info("Generated audio, tyring to play it now...");
                loadAndPlay(this.general, file.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.error("Error while loading/playing track!", e);
            }
        });
    }

    /**
     * Pauses the track.
     */
    public void pauseTrack() {
        GuildMusicManager musicManager = getGuildAudioPlayer(this.general.getGuild());
        musicManager.scheduler.pauseTrack();
    }

    /**
     * Resumes the track.
     */
    public void resumeTrack() {
        GuildMusicManager musicManager = getGuildAudioPlayer(this.general.getGuild());
        musicManager.scheduler.resumeTrack();
    }

    /**
     * Seeks the track.
     */
    public void seekTrack(long position) {
        currentTrack.setPosition(position);
    }

    public boolean isPlaying() {
        return playing;
    }

    public CompletableFuture<AudioTrack> waitForTrackLoaded() {
        return CompletableFuture.supplyAsync(() -> {
            while (currentTrack == null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {}
            }
            return currentTrack;
        });
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl) {
        this.musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                currentTrack = track;
//                play(); // TODO: Remove
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                channel.sendMessage("How did you get a playlist working on here? Like bruh...").queue();
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Audio file not found! Contact administrators. Failed to find: " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Failed to play audio! " + exception.getMessage()).queue();
                LOGGER.error("Failed to play audio! {}", exception.getLocalizedMessage());
            }
        });
    }

    public void play() {
        if (isPlaying()) {
            resumeTrack();
            return;
        }

        var audioManager = this.listen.getGuild().getAudioManager();

        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            audioManager.openAudioConnection(this.listen);
        }

        musicManager.scheduler.queue(currentTrack);
        this.playing = true;
    }

}
