package com.uddernetworks.emoji.emoji;

import com.uddernetworks.emoji.utils.IntPair;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Optional;

public interface VideoParser {

    void preprocessVideo(ProcessableVideo video);

    Optional<Map<IntPair, BufferedImage>> separateImages(BufferedImage input);

    ProcessableVideo getVideo(File file);

    GifGenerator getGifGenerator();
}
