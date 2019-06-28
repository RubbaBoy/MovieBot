package com.uddernetworks.emoji.player;

import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.gif.VideoGifProcessor;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VideoPlayer {

    private static Logger LOGGER = LoggerFactory.getLogger(Video.class);
    private static final int SECTION_DURATION = 15; // Seconds;

    private final TextChannel channel;
    private final Video video;

    private AudioPlayer audioPlayer;
    private Message message;
    private VideoPlayerState state = VideoPlayerState.PLAYING;

    // Edited in multiple threads
    private volatile String currentImage;
    private volatile String nextImage = "https://media1.giphy.com/media/2WjpfxAI5MvC9Nl8U7/source.gif";
    private volatile int seek = -SECTION_DURATION;

    // TODO: Sound
    // TODO: Get messages from some type of data source when added to prevent more messages being sent on bot ready
    public VideoPlayer(Video video, TextChannel channel) throws IOException {
        this.channel = channel;
        this.video = video;
        this.audioPlayer = new AudioPlayer(this.video.getfFmpegManager(), channel.getJDA());

        LOGGER.info("Created player in " + channel.getId());

        CompletableFuture.runAsync(this::nextSet);
    }

    private MessageEmbed createEmbed(String image) {
        var embed = new EmbedBuilder();

        int hours = (int) Math.floor((double) video.getLength() / 60 / 60);
        int mins = (int) Math.floor(((double) video.getLength() - hours * 60) / 60);
        int secs = video.getLength() - mins * 60;
        int curHours = (int) Math.floor((double) seek / 60 / 60);
        int curMins = (int) Math.floor(((double) (seek - curHours * 60) * 60) / 60);
        int curSecs = seek - curMins * 60;

        var totalTime = mins + ":" + secs;
        var seekTime = curMins + ":" + curSecs;

        if (hours > 0) totalTime = hours + ":" + totalTime;
        if (curHours > 0) seekTime = curHours + ":" + seekTime;

        var prog = (double) seek / video.getLength();

        StringBuilder playBar = new StringBuilder();

        for (double i = 0; i < 1; i += 0.1) {
            if (prog >= i && prog < i + 0.1) {
                playBar.append("\u25CF");
            } else {
                playBar.append("\u2500");
            }
        }

        embed.setTitle(video.getTitle());
        embed.setDescription(seekTime + " " + playBar + " " + totalTime);
        if (image != null) embed.setImage(image);
        embed.setColor(0);

        return embed.build();
    }

    public void setState(VideoPlayerState state) {
        this.state = state;
        var currentSeek = seek;
        seek = currentSeek - SECTION_DURATION;
        nextImage = currentImage;
        nextSet(false);
    }

    public VideoPlayerState getState() {
        return state;
    }

    private void nextSet() {
        nextSet(true);
    }

    private CompletableFuture<String> uploadImage(File file) {
        return CompletableFuture.supplyAsync(() -> {
            Message msg = this.channel.getJDA().getTextChannelById(594037372694298635L).sendFile(file).complete();
            return msg.getAttachments().get(0).getUrl();
        });
    }

    private void nextSet(boolean loop) {
        long start = System.currentTimeMillis();

        if (seek >= this.video.getLength()) {
            return;
        }

        var currentSeek = seek;
        seek = currentSeek + SECTION_DURATION;

        LOGGER.info("seek " + currentSeek);

        currentImage = nextImage;

        try {
            if (seek != 0 && !this.audioPlayer.isPlaying()) {
                this.audioPlayer.play();
            } else if (this.audioPlayer.isPlaying()) {
                this.audioPlayer.seekTrack((seek - SECTION_DURATION) * 1000);
            }

            if (message == null) {
                this.message = channel.sendMessage(createEmbed(currentImage)).complete();
            } else {
                message.editMessage(createEmbed(currentImage)).queue();
            }

            if (seek == 0) {
                this.audioPlayer.initialPlay(this.video);
                this.audioPlayer.waitForTrackLoaded().get();
            }

            var file = this.video.convertToGif(seek, SECTION_DURATION).get();
            var url = uploadImage(file).get();
            this.nextImage = url;
            LOGGER.info(url);
        } catch (Exception e) {
            LOGGER.error("Error while sending or generating set", e);
        }


        if (loop) {
            // First set was just loading image, so we can skip right on ahead
            var millis = SECTION_DURATION * 1000 - (System.currentTimeMillis() - start);
            if (seek != 0 && millis > 0) {
                // Wait for next time to update
                try {
                    LOGGER.info("wait for " + millis);
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    LOGGER.error("Error while sleeping", e);
                    return;
                }
            }

            CompletableFuture.runAsync(this::nextSet);
        }
    }

    public void joinVoice(VoiceChannel channel) {
        // TODO
    }
}
