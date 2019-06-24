package com.uddernetworks.emoji.main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// The following is modified code I made earlier, delete this comment when it doesn't look like trash
public class Main {

    private static String prefix = "t";

    private static int icon_size = 120;
//    private static int icon_size = 256;

    public static void main(String[] args) throws Exception {
        BufferedImage image;
        File file;

        //read image
        try {
            file = new File("E:\\DiscordEmojiCreator\\movie.png");
            image = ImageIO.read(file);
            System.out.println("Reading complete.");

            System.out.println("width = " + image.getWidth());
            System.out.println("height = " + image.getHeight());

            if (image.getWidth() % icon_size != 0) throw new Exception("Size is not divisible by " + icon_size);
            if (image.getHeight() % icon_size != 0) throw new Exception("Size is not divisible by " + icon_size);

            int col = image.getWidth() / icon_size;
            int row = image.getHeight() / icon_size;

            System.out.println("row = " + row);
            System.out.println("col = " + col);

            StringBuilder paste = new StringBuilder();

            int num = 0;

            for (int yBlock = 0; yBlock < row; yBlock++) {
                for (int xBlock = 0; xBlock < col; xBlock++) {
                    String str = String.format("%03d", num++);

                    System.out.println((yBlock * icon_size) + " " + icon_size);

                    BufferedImage subImage = image.getSubimage(xBlock * icon_size, yBlock * icon_size, icon_size, icon_size);

                    file = new File("E:\\DiscordEmojiCreator\\output\\" + str + ".png");
                    ImageIO.write(subImage, "png", file);

                    paste.append(':').append(str).append(':');
                }

                paste.append('\n');
            }

            System.out.println("Paste the following after uploading files in discord:\n\n\n");
            System.out.println(paste);

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
