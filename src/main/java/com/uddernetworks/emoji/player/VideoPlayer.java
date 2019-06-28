package com.uddernetworks.emoji.player;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class VideoPlayer {
    private Message message;
    private final TextChannel channel;
    private final Video video;

    // TODO: Sound
    // TODO: Get messages from some type of data source when added to prevent more messages being sent on bot ready
    public VideoPlayer(Video video, TextChannel channel) {
        this.channel = channel;
        this.video = video;
        this.message = this.channel.sendMessage(createEmbed(null, 0)).complete();
    }

    private MessageEmbed createEmbed(String image, int seek) {
        var embed = new EmbedBuilder();

        var hours = Math.floor((double) video.getLength() / 60 / 60);
        var mins = Math.floor(((double) video.getLength() - hours * 60) / 60);
        var secs = video.getLength() - mins * 60;
        var curHours = Math.floor((double) seek / 60 / 60);
        var curMins = Math.floor(((double) video.getLength() - seek * 60) / 60);
        var curSecs = seek - curMins * 60;

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

    public void updatePlayer(String image, int seek) {
        message.editMessage(createEmbed(image, seek)).queue();
    }

    public void joinVoice(VoiceChannel channel) {
        // TODO
    }
}
