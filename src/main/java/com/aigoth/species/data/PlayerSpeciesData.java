package com.aigoth.species.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlayerSpeciesData {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, String> PLAYER_RACE_DATA = new HashMap<>();
    private static Path dataFile;
    // 添加读写锁来保证线程安全
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static void setDataPath(Path worldPath) {
        dataFile = worldPath.resolve("serverconfig").resolve("species_players.json");
        loadData();
    }

    public static void setPlayerRace(UUID playerUUID, String raceId) {
        // 防止传入null UUID
        if (playerUUID == null) {
            LOGGER.warn("Attempted to set player race with null UUID, ignoring");
            return;
        }
        
        lock.writeLock().lock();
        try {
            PLAYER_RACE_DATA.put(playerUUID, raceId);
            saveData();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static String getPlayerRace(UUID playerUUID) {
        lock.readLock().lock();
        try {
            return PLAYER_RACE_DATA.get(playerUUID);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void removePlayerRace(UUID playerUUID) {
        // 防止传入null UUID
        if (playerUUID == null) {
            LOGGER.warn("Attempted to remove player race with null UUID, ignoring");
            return;
        }
        
        lock.writeLock().lock();
        try {
            PLAYER_RACE_DATA.remove(playerUUID);
            saveData();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void loadData() {
        if (dataFile == null || !dataFile.toFile().exists()) {
            return;
        }

        lock.writeLock().lock();
        try (Reader reader = new FileReader(dataFile.toFile())) {
            Map<String, String> data = GSON.fromJson(reader, new TypeToken<Map<String, String>>(){}.getType());
            if (data != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    try {
                        UUID uuid = UUID.fromString(entry.getKey());
                        PLAYER_RACE_DATA.put(uuid, entry.getValue());
                    } catch (IllegalArgumentException e) {
                        // 无效的UUID，跳过
                        LOGGER.warn("Invalid UUID in player species data: {}", entry.getKey());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load player species data", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void saveData() {
        if (dataFile == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            // 确保目录存在
            File parentDir = dataFile.getParent().toFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                LOGGER.warn("Failed to create directory: {}", parentDir.getAbsolutePath());
            }
            
            Map<String, String> data = new HashMap<>();
            for (Map.Entry<UUID, String> entry : PLAYER_RACE_DATA.entrySet()) {
                // 防御性检查：跳过null键或null值
                if (entry.getKey() != null && entry.getValue() != null) {
                    data.put(entry.getKey().toString(), entry.getValue());
                } else {
                    LOGGER.warn("Skipping invalid player species data entry with null key or value");
                }
            }

            try (Writer writer = new FileWriter(dataFile.toFile())) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save player species data", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}