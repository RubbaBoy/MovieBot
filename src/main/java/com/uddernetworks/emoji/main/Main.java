package com.uddernetworks.emoji.main;

import com.uddernetworks.emoji.emoji.DefaultVideoParser;
import com.uddernetworks.emoji.emoji.ProcessableVideo;
import com.uddernetworks.emoji.player.VideoPlayer;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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


    List<Guild> emojiGuilds = new ArrayList<>();
    private Emote[] currentEmotes;
    private Emote[] bufferedEmotes;
    private DefaultVideoParser parser;
    private ProcessableVideo video;
    private List<VideoPlayer> players = new ArrayList<>();
    private int nextEmoji = 0;

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Bot Ready");

        emojiGuilds.add(event.getJDA().getGuildById(593887502360772638L));
        emojiGuilds.add(event.getJDA().getGuildById(593887468189777922L));
        emojiGuilds.add(event.getJDA().getGuildById(593887422459412542L));
        emojiGuilds.add(event.getJDA().getGuildById(593887383431151647L));

        players.add(new VideoPlayer(event.getJDA().getTextChannelById(591484659913981972L)));

        System.out.println("Clearing emojis...");
        for (Guild guild : emojiGuilds) {
            for (Emote emote : guild.getEmotes()) {
                emote.delete().reason("Starting movie").complete();
            }
        }

        System.out.println("Pre-processing video...");
        parser = new DefaultVideoParser();

        video = parser.getVideo(new File("videos\\video.mp4"));
        parser.preprocessVideo(video, 0);

        System.out.println("Uploading first sets of emojis...");

        createNextEmotes();

        System.out.println("Starting movie...");
        CompletableFuture.runAsync(this::runUpdate);
    }

    private void createNextEmotes() {
        bufferedEmotes = new Emote[144];

        for (int i = 0; i < 144; i++) {
            for (var guild : emojiGuilds) {
                if (guild.getEmotes().size() < 50) {
                    try {
                        var icon = Icon.from(new File(String.format("gifs\\%03d.gif", i)));
                        bufferedEmotes[i] = guild.getController().createEmote(String.format("%02X", i), icon).complete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    private void runUpdate() {
        long start = System.currentTimeMillis();

        // Move buffer to current
        for (var emote : currentEmotes) emote.delete().queue();
        currentEmotes = bufferedEmotes;

        // Update players
        for (var player : players) player.updateMessages(currentEmotes);

        nextEmoji++;

        // Generate next buffer
        parser.preprocessVideo(video, nextEmoji);
        createNextEmotes();

        // Wait for next time to update
        try {
            Thread.sleep(10 * 1000 - (System.currentTimeMillis() - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        CompletableFuture.runAsync(this::runUpdate);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

    }
}
