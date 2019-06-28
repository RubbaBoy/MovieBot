package com.uddernetworks.emoji.player;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayer {
    private final List<Message> messages;
    private final TextChannel channel;

    // TODO: Sound
    // TODO: Get messages from some type of data source when added to prevent more messages being sent on bot ready
    public VideoPlayer(TextChannel channel) {
        messages = new ArrayList<>();
        this.channel = channel;
    }

    public void updateMessages(Emote[] emotes) {
        List<String> messages = new ArrayList<>();

        for (int i = 0; i < 3; i += 48) {
            StringBuilder msg = new StringBuilder();

            for (int j = i; j < i + 48; j++)
                msg.append(emotes[j].getAsMention());

            messages.add(msg.toString());
        }

        if (this.messages.size() == 0) {
            for (String msg : messages) {
                this.messages.add(channel.sendMessage(msg).complete());
            }
        } else {
            for (int i = 0; i < messages.size(); i++) {
                this.messages.get(i).editMessage(messages.get(i)).queue();
            }
        }
    }
}
