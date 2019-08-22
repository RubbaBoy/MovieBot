<h1 align="center">
    <img src="branding/Banner.png">
</h1>

Discord is a place that brings people together to play games, chat, and now with Discord Movie Bot you can watch movies with your friends right in the Discord app. Brought to you by the creator of the [best IDE](https://github.com/MSPaintIDE/MSPaintIDE) around, [@RubbaBoy](https://github.com/RubbaBoy) and a developer know one knows, [@AL1L](https://al1l.com) using sophisticated audio and video technology we bring our [Discord Hack Week](https://blog.discordapp.com/discord-community-hack-week-build-and-create-alongside-us-6b2a7b7bba33) submission and your favorite movies and videos straight to the Discord client.

Join the official Movie Bot Discord server [here](https://discord.gg/PrjEt3u)

## Screenshots

![img](https://rubbaboy.me/images/nymr7o3)



![](https://rubbaboy.me/images/97w2jyl)



![](https://rubbaboy.me/images/7dktv43)

## How does it work?

Thanks for letting me ask you your behalf, here's how:

- Add the bot with [this invite link](https://discordapp.com/api/oauth2/authorize?client_id=591485290355490825&permissions=3147776&scope=bot)
- Run the `dem setrole [tag role]` command to set which role can play movies, then
- Run the `dem settext [channel ID]` command to set what channel the movie is played in, we suggest somewhere with no messages. Next
- Run the `dem setvoice [channel ID]` command to set what voice channel you can listen to the movie to, then
- Run the `dem enable` to finish up the setup process
- Now you're ready to watch movies!
- Run `dem list` to see what videos we have available. Don't like the selection? run `dem request [video url]` and we'll get back to you.
- Once you have your eyes locked on a video you want to eat popcorn to, run `dem play [video name]`, join the voice channel, set back, eat your snacks and enjoy the show!


## Getting Technical

The bot breaks up a given video file into gifs a few (By default 10) seconds long, and plays them synced with audio from a voice channel. They are synced up to provide a 'seamless' viewing experience. If you host your own version you can slap whatever .mp4 you want in the /videos/ folder and reload with the `dem reload` command.
