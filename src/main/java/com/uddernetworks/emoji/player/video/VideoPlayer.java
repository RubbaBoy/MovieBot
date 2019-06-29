package com.uddernetworks.emoji.player.video;

import com.uddernetworks.emoji.main.Main;
import com.uddernetworks.emoji.player.audio.AudioPlayer;
import com.uddernetworks.emoji.utils.Thread;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class VideoPlayer {

    private static Logger LOGGER = LoggerFactory.getLogger(LocalVideo.class);
    private static final int SECTION_DURATION = 10; // Seconds;

    private final TextChannel channel;
    private final Video video;
    private final Main main;

    private AudioPlayer audioPlayer;
    private Message message;
    private VideoPlayerState state = VideoPlayerState.PLAYING;

    // Edited in multiple threads
    private volatile String currentImage;
    private volatile String nextImage = "https://media1.giphy.com/media/2WjpfxAI5MvC9Nl8U7/source.gif";
    private volatile int seek = -SECTION_DURATION;

    // TODO: Sound
    // TODO: Get messages from some type of data source when added to prevent more messages being sent on bot ready
    public VideoPlayer(Main main, Video video, TextChannel channel) throws IOException {
        this.main = main;
        this.channel = channel;
        this.video = video;
        this.audioPlayer = new AudioPlayer(this.video.getfFmpegManager(), main, channel);

        LOGGER.info("Created player in " + channel.getId());

        CompletableFuture.runAsync(this::nextSet);
    }

    private MessageEmbed createEmbed(String image) {
        var embed = new EmbedBuilder();

        /*
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
         */

        var prog = (double) (seek - SECTION_DURATION) / video.getLength();

        StringBuilder playBar = new StringBuilder();

        playBar.append(state == VideoPlayerState.PLAYING ? "\u25B6" : "\u23F8");
        playBar.append(" ");

        for (double i = 0; i < 1; i += 0.05) {
            if (prog >= i && prog < i + 0.05) {
                playBar.append("\u25CF");
            } else {
                playBar.append("\u2500");
            }
        }

        embed.setTitle(video.getTitle());
        embed.setFooter(playBar.toString(), null);
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

    private CompletableFuture<Void> playAudioFor(int duration) {
        return CompletableFuture.runAsync(() -> {
            Thread.sleep(300);
            if (!this.audioPlayer.isPlaying()) {
                this.audioPlayer.play();
            } else {
                this.audioPlayer.resumeTrack();
            }
        })
                .thenRunAsync(() -> Thread.sleep(duration))
                .thenRunAsync(() -> this.audioPlayer.pauseTrack());
    }

    private void nextSet(boolean loop) {
        long start = System.currentTimeMillis();

        if (seek >= this.video.getLength() || this.state == VideoPlayerState.END) {
            this.state = VideoPlayerState.END;
            this.audioPlayer.pauseTrack();
            this.message.delete().queue();
            this.audioPlayer.disconnect();
            this.main.removePlayer(this);
            return;
        }

        if (this.state == VideoPlayerState.PAUSED) {
            this.audioPlayer.pauseTrack();
            if (loop) Thread.sleep(250);
            CompletableFuture.runAsync(this::nextSet);
            return;
        }

        var currentSeek = seek;
        seek = currentSeek + SECTION_DURATION;

        LOGGER.info("seek " + currentSeek);

        currentImage = nextImage;

        try {
            if (message == null) {
                this.message = channel.sendMessage(createEmbed(currentImage)).complete();
                this.message.addReaction("\u23EF").queue();
                // this.message.addReaction("\uD83D\uDD04").queue();
                this.message.addReaction("\u23F9").queue();
            } else {
                var embed = createEmbed(currentImage);
                audioPlayer.play();
                message.editMessage(embed).queue(cons -> {
                    LOGGER.info("Finished embed");
                });
            }

            if (seek == 0) {
                var shit = System.currentTimeMillis();
                this.audioPlayer.initialPlay(this.video);
                this.audioPlayer.waitForTrackLoaded().get();
                LOGGER.info("Took {}ms uwu", System.currentTimeMillis() - shit);
            }

            long trime = System.currentTimeMillis();
            var file = this.video.convertToGif(seek, SECTION_DURATION).get();

            LOGGER.info("Took {}ms", System.currentTimeMillis() - trime);
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
                LOGGER.info("wait for " + millis);
//                playAudioFor((int) millis + 200);
                Thread.sleep(millis);
                audioPlayer.pauseTrack();
            }
            CompletableFuture.runAsync(this::nextSet);
        }
    }

    public TextChannel getChannel() {
        return channel;
    }

    public Message getMessage() {
        return message;
    }

    public void seekTo(int seek) {
        this.seek = seek;
        this.audioPlayer.seekTrack((seek - SECTION_DURATION) * 1000);
        nextSet(false);
    }

    public void startFromBeginning() {
        seekTo(-SECTION_DURATION);
    }
}
