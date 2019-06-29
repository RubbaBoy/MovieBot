package com.uddernetworks.emoji.main;

import com.uddernetworks.emoji.ffmpeg.FFmpegManager;
import com.uddernetworks.emoji.player.audio.AudioPlayer;
import com.uddernetworks.emoji.player.video.VideoPlayer;
import com.uddernetworks.emoji.player.video.VideoPlayerState;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
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
        VideoPlayer player;

        if (guild == null) return;
        if (!args[0].equalsIgnoreCase("dem")) return;

        switch (args[1].toLowerCase()) {
            case "help":
                channel.sendMessage(createEmbed(author, "Help", embedBuilder -> {
                    embedBuilder.addField("General Help",
                            "**dem help** Shows this help menu\n" +
                                    "**dem list** Displays the available videos\n" +
                                    "**dem play [video name]** Plays the given video\n" +
                                    "**dem download [video url]** Plays the given video\n" +
                                    "**dem pause** Pauses current playing video\n" +
                                    "**dem resume** Resumes current playing video\n" +
                                    "**dem stop** Stops current playing video"
                            , false);

                    if (PermissionUtil.checkPermission(author, Permission.ADMINISTRATOR))
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
                channel.sendMessage(createEmbed(author, "Movie List", embedBuilder -> {
                    var description = new StringBuilder();
                    var videos = this.main.getLoadedVideos();

                    for (int i = 0; i < videos.size(); i++) {
                        description
                                .append(i + 1)
                                .append(". ")
                                .append(videos.get(i).getTitle())
                                .append("\n");
                    }

                    embedBuilder.setDescription(description.toString());
                })).queue();
                break;
            case "play":
                if (checkCanPlay(channel, author)) return;

                if (this.main.getPlaying().get(guild.getId()) != null) {
                    error(channel, author, "There is already a video playing in this server");
                    return;
                }

                var videoName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                var video = this.main.getVideo(videoName);

                if (video == null) {
                    error(channel, author, "Can't find the video " + videoName);
                    return;
                }

                try {
                    player = this.main.playVideo(guild, video);
                } catch (IOException e) {
                    e.printStackTrace();
                    error(channel, author, "Something happened when attempting to play " + video.getTitle());
                    return;
                }

                channel.sendMessage(createEmbed(author, "Now Playing", embedBuilder ->
                        embedBuilder.setDescription("Spinning up  " + video.getTitle() + " in " +
                                player.getChannel().getAsMention() + "\n\nEnjoy the show!"))).queue();

                break;
            case "download":
                if (checkCanPlay(channel, author)) return;
                try {
                    var url = new URL(args[2]);
                } catch (MalformedURLException e) {
                    error(channel, author, "Please specify a valid URL");
                    return;
                }

                break;
            case "pause":
                if (checkCanPlay(channel, author)) return;
                player = this.main.getPlaying().get(guild.getId());

                if (player == null) {
                    error(channel, author, "There is no video being played in this server. Try `dem play`");
                    return;
                }

                if (player.getState() == VideoPlayerState.PAUSED) {
                    error(channel, author, "This video is already paused");
                    return;
                }

                player.setState(VideoPlayerState.PAUSED);
                break;
            case "resume":
                if (checkCanPlay(channel, author)) return;
                player = this.main.getPlaying().get(guild.getId());

                if (player == null) {
                    error(channel, author, "There is no video being played in this server. Try `dem play`");
                    return;
                }

                if (player.getState() == VideoPlayerState.PLAYING) {
                    error(channel, author, "This video is already playing");
                    return;
                }

                player.setState(VideoPlayerState.PLAYING);

                break;
            case "stop":
                if (checkCanPlay(channel, author)) return;
                player = this.main.getPlaying().get(guild.getId());

                if (player == null) {
                    error(channel, author, "There is no video being played in this server. Try `dem play`");
                    return;
                }

                player.setState(VideoPlayerState.END);
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

                var roleId = args[2].replaceAll("[^0-9]", "");

                var role = guild.getRoleById(roleId);

                if (role == null) {
                    error(channel, author, "Please specify a valid role");
                    return;
                }

                this.configManager.setValue(guild, "role", roleId);
                sendEmbed(channel, author, "Set role", embedBuilder -> embedBuilder.setDescription("The role that can play videos is now " + role.getAsMention()));
                break;
            case "setvoice":
                if (checkAdmin(channel, author)) return;
                var voice = args[2].trim().replaceAll("[^0-9]", "");

                System.out.println("voice = " + voice);

                var voiceChannel = guild.getVoiceChannelById(voice);

                if (voiceChannel == null) {
                    error(channel, author, "Please specify a valid voice channel");
                    return;
                }

                this.configManager.setValue(guild, "voicechannel", voiceChannel.getId());
                sendEmbed(channel, author, "Set voice channel", embedBuilder -> embedBuilder.setDescription("The voice channel has been set to " + voiceChannel.getName()));
                break;
            case "settext":
                if (checkAdmin(channel, author)) return;
                var text = args[2].replaceAll("[^0-9]", "");

                var textChannel = guild.getTextChannelById(text);

                if (textChannel == null) {
                    error(channel, author, "Please specify a valid text channel");
                    return;
                }

                this.configManager.setValue(guild, "textchannel", textChannel.getId());
                sendEmbed(channel, author, "Set text channel", embedBuilder -> embedBuilder.setDescription("The text channel has been set to " + textChannel.getName()));
                break;
        }
    }

    private boolean checkAdmin(MessageChannel channel, Member author) {
        if (!PermissionUtil.checkPermission(author, Permission.ADMINISTRATOR)) {
            error(channel, author, "You must be administrator to do that");
            return true;
        }

        return false;
    }

    private boolean checkCanPlay(MessageChannel channel, Member author) {
        if (PermissionUtil.checkPermission(author, Permission.ADMINISTRATOR)) return false;

        var roleId = this.configManager.getValue(author.getGuild(), "role").orElse(null);

        if (roleId == null) {
            error(channel, author, "You must be administrator to do that");
            return true;
        }

        var role = author.getGuild().getRoleById(roleId);

        if (role == null) {
            error(channel, author, "You must be administrator to do that");
            return true;
        }

        for (var memberRole : author.getRoles())
            if (memberRole.getIdLong() == role.getIdLong())
                return false;

        error(channel, author, "You must have the " + role.getAsMention() + " role to do that");
        return true;
    }

    private void error(MessageChannel channel, Member author, String message) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Oh no", null);
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
