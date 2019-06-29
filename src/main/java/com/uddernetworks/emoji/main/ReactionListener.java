package com.uddernetworks.emoji.main;

import com.uddernetworks.emoji.player.video.VideoPlayer;
import com.uddernetworks.emoji.player.video.VideoPlayerState;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class ReactionListener extends ListenerAdapter {

    private final Main main;

    public ReactionListener(Main main) {
        this.main = main;

        main.getJDA().addEventListener(this);
    }

    private boolean checkCanPlay(MessageChannel channel, Member author) {
        if (PermissionUtil.checkPermission(author, Permission.ADMINISTRATOR)) return false;

        var roleId = this.main.getConfigManager().getValue(author.getGuild(), "role").orElse(null);

        if (roleId == null) return true;

        var role = author.getGuild().getRoleById(roleId);

        if (role == null) return true;

        for (var memberRole : author.getRoles())
            if (memberRole.getIdLong() == role.getIdLong())
                return false;

        return true;
    }

    private void reactionEvent(GenericGuildMessageReactionEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (checkCanPlay(event.getChannel(), event.getMember())) return;

        var reaction = event.getReactionEmote().getName();
        var player = main.getPlaying().get(event.getGuild().getId());

        if (player == null) return;
        if (!player.getMessage().getId().equals(event.getMessageId())) return;

        if (reaction.equals("\u23EF")) {
            if (player.getState() == VideoPlayerState.PAUSED) {
                player.setState(VideoPlayerState.PLAYING);
            } else {
                player.setState(VideoPlayerState.PAUSED);
            }
        } else if (reaction.equals("\u23F9")) {
            player.setState(VideoPlayerState.END);
        }
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        reactionEvent(event);
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        reactionEvent(event);
    }
}
