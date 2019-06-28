package com.uddernetworks.emoji.main;

import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.player.audio.AudioPlayer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.function.Consumer;

public class CommandManager extends ListenerAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger(AudioPlayer.class);
    private FFmpegManager fFmpegManager;
    private JDA jda;
    private Main main;
    private ConfigManager configManager;

    public CommandManager(Main main) {
        this.main = main;
        this.fFmpegManager = main.getfFmpegManager();
        (this.jda = main.getJDA()).addEventListener(this);
        this.configManager = this.main.getConfigManager();

//        this.general = this.jda.getTextChannelById(591482977989427211L); // TODO: Make this configurable or something in prod
//        this.listen = this.jda.getVoiceChannelById(591484863455166474L); // TODO: Make this configurable or something in prod

//        AudioSourceManagers.registerLocalSource(this.playerManager = new DefaultAudioPlayerManager());
    }

    // TODO: Temporary commands, move them to another file and make the command recognition at least semi-modular lol
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        var args = event.getMessage().getContentRaw().split("\\s+", 3);
        Guild guild = event.getGuild();
        var author = event.getMember();
        var channel = event.getChannel();

        if (guild == null) return;
        if (!args[0].equalsIgnoreCase("dem")) return;

        switch (args[1].toLowerCase()) {
            case "help":
                channel.sendMessage(createEmbed(author, "Help", embedBuilder -> {
                    embedBuilder.addField("General Help",
                            "**dem help** Shows this help menu\n" +
                                    "**dem list** Displays the available videos\n" +
                                    "**dem play [video name]** Plays the given video\n" +
                                    "**dem stop** Stops any currently running video"
                            , false);

                    embedBuilder.addField("Admin Commands",
                            "**dem disable** Disables videos from being played\n" +
                                    "**dem enable** Allows videos to be played\n" +
                                    "**dem setrole [tag role]** Sets the role that can play videos\n" +
                                    "**dem setvoice [channel ID]** Sets the voice channel to join and play audio\n" +
                                    "**dem settext [channel ID]** Sets the text channel to play the video in"
                            , false);
                })).queue();
                break;
            case "list":

                break;
            case "play":
                var video = args[2];

                break;
            case "stop":

                break;

            case "disable":
                if (checkAdmin(channel, author)) return;
                this.configManager.setValue(guild, "enabled", "false");
                sendEmbed(channel, author, "Disabled videos", embedBuilder -> embedBuilder.setDescription("Videos have been disabled!"));
                break;
            case "enable":
                if (checkAdmin(channel, author)) return;
                this.configManager.setValue(guild, "enabled", "true");
                sendEmbed(channel, author, "Enabled videos", embedBuilder -> embedBuilder.setDescription("Videos have been enabled!"));
                break;
            case "setrole":
                if (checkAdmin(channel, author)) return;

                var role = args[2];
                var mentioned = event.getMessage().getMentionedRoles();
                if (mentioned.isEmpty()) {
                    error(channel, author, "You need to mention a role!");
                    return;
                }
                this.configManager.setValue(guild, "role", role);
                sendEmbed(channel, author, "Set role", embedBuilder -> embedBuilder.setDescription("The role that can play videos is now " + role));
                break;
            case "setvoice":
                if (checkAdmin(channel, author)) return;
                var voice = args[2].trim();
                System.out.println("voice = " + voice);

                if (!NumberUtils.isDigits(voice) || voice.length() != 18) {
                    error(channel, author, "You need to include a single voice channel ID!");
                    return;
                }

                var voiceChannel = guild.getVoiceChannelById(voice);

                if (voiceChannel == null) {
                    error(channel, author, "You need to include a single voice channel ID!");
                    return;
                }

                this.configManager.setValue(guild, "voicechannel", voiceChannel.getId());
                sendEmbed(channel, author, "Set voice channel", embedBuilder -> embedBuilder.setDescription("The voice channel has been set to " + voiceChannel.getName()));
                break;
            case "settext":
                if (checkAdmin(channel, author)) return;
                var text = args[2].trim();

                if (!NumberUtils.isDigits(text) || text.length() != 18) {
                    error(channel, author, "You need to include a single text channel ID!");
                    return;
                }

                var textChannel = guild.getTextChannelById(text);

                if (textChannel == null) {
                    error(channel, author, "You need to include a single text channel ID!");
                    return;
                }

                this.configManager.setValue(guild, "textchannel", textChannel.getId());
                sendEmbed(channel, author, "Set text channel", embedBuilder -> embedBuilder.setDescription("The text channel has been set to " + textChannel.getName()));
                break;
        }
    }

    private boolean checkAdmin(MessageChannel channel, Member author) {
        if (!PermissionUtil.checkPermission(author, Permission.ADMINISTRATOR)) {
            error(channel, author, "You must be administrator to do that!");
            return true;
        }

        return false;
    }

    private void error(MessageChannel channel, Member author, String message) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Error", null);
        eb.setColor(new Color(0xFF0000));

        eb.setDescription(message);

        eb.setFooter("In response to " + author.getEffectiveName(), author.getUser().getAvatarUrl());
        channel.sendMessage(eb.build()).queue();
    }

    private void sendEmbed(MessageChannel channel, Member author, String title, Consumer<EmbedBuilder> embedBuilderConsumer) {
        channel.sendMessage(createEmbed(author, title, embedBuilderConsumer)).queue();
    }

    private MessageEmbed createEmbed(Member author, String title, Consumer<EmbedBuilder> embedBuilderConsumer) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(title, null);
        eb.setColor(new Color(0xe91e63));

        eb.setFooter("Requested by " + author.getEffectiveName(), author.getUser().getAvatarUrl());

        embedBuilderConsumer.accept(eb);
        return eb.build();
    }

}
