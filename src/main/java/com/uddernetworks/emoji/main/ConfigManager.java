package com.uddernetworks.emoji.main;

import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigManager {

    private static Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    private Map<Long, Map<String, String>> config = new HashMap<>();

    public ConfigManager() {
        new File("configs").mkdirs();
    }

    public void loadConfig(Guild guild) {
        try {
            Properties prop = new Properties();
            var file = new File("configs\\" + guild.getIdLong() + ".properties");
            if (!file.exists()) return;
            InputStream in = new FileInputStream(file);
            prop.load(in);

            config.put(guild.getIdLong(), prop.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), entry2 -> entry2.getValue().toString())));
        } catch (IOException e) {
            LOGGER.error("Error while loading " + guild.getId() + "'s config", e);
        }
    }

    public Optional<String> getValue(Guild guild, String key) {
        return Optional.ofNullable(config.computeIfAbsent(guild.getIdLong(), id -> new HashMap<>()).getOrDefault(key, null));
    }

    public void setValue(Guild guild, String key, String value) {
        config.computeIfAbsent(guild.getIdLong(), id -> new HashMap<>()).put(key, value);
        writeConfig(guild);
    }

    public void writeConfig(Guild guild) {
        var id = guild.getIdLong();
        if (!config.containsKey(id)) return;
        try (OutputStream output = new FileOutputStream("configs\\" + id + ".properties")) {
            Properties prop = new Properties();

            config.get(id).forEach(prop::setProperty);

            prop.store(output, null);
            LOGGER.info("Write properties for guild {}", id);
        } catch (IOException e) {
            LOGGER.error("Error writing to properties file for guild " + id, e);
        }
    }
}
