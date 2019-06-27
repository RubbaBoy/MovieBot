package com.uddernetworks.emoji.main;

import com.uddernetworks.emoji.emoji.DefaultVideoParser;
import com.uddernetworks.emoji.emoji.ProcessableVideo;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.File;

public class Main extends ListenerAdapter {

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

    private DefaultVideoParser parser;
    private ProcessableVideo video;

    @Override
    public void onReady(ReadyEvent event) {
        parser = new DefaultVideoParser();

        video = parser.getVideo(new File("videos\\video.mp4"));
        parser.preprocessVideo(video);


    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

    }
}
