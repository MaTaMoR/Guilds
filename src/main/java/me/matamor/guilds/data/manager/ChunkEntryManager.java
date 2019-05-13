package me.matamor.guilds.data.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.ChunkEntry;
import me.matamor.commonapi.utils.Validate;

import java.util.*;

public class ChunkEntryManager {

    private final Map<String, WorldChunkEntryManager> entries = new LinkedHashMap<>();

    @Getter
    private final Guilds plugin;

    public ChunkEntryManager(Guilds plugin) {
        this.plugin = plugin;
    }

    private WorldChunkEntryManager getWorldManager(String worldName) {
        return this.entries.get(worldName);
    }

    private WorldChunkEntryManager getOrCreateWorldManager(String worldName) {
        return this.entries.computeIfAbsent(worldName, (k) -> new WorldChunkEntryManager(worldName));
    }

    public void storeChunkEntry(ChunkEntry chunkEntry) {
        Validate.notNull(chunkEntry, "ChunkEntry can't be null!");

        WorldChunkEntryManager worldManager = getOrCreateWorldManager(chunkEntry.getWorldName());
        worldManager.storeChunkEntry(chunkEntry);
    }

    public ChunkEntry removeChunkEntry(String worldName, int x, int z) {
        WorldChunkEntryManager worldManager = getWorldManager(worldName);
        if (worldManager == null) return null;

        return worldManager.removeChunkEntry(x, z);
    }

    public ChunkEntry getChunkEntry(String worldName, int x, int z) {
        WorldChunkEntryManager worldManager = getWorldManager(worldName);
        if (worldManager == null) return null;

        return worldManager.getChunkEntry(x, z);
    }

    public void unloadWorld(String worldName) {
        this.entries.remove(worldName);
    }

    public void unload() {
        this.entries.clear();
    }

    @RequiredArgsConstructor
    private class WorldChunkEntryManager {

        private final Map<String, ChunkEntry> entries = new LinkedHashMap<>();

        @Getter
        private final String worldName;

        private String combine(int x, int z) {
            return Integer.toString(x) + Integer.toString(z);
        }

        private ChunkEntry getChunkEntry(int x, int z) {
            return this.entries.get(combine(x, z));
        }

        public void storeChunkEntry(ChunkEntry chunkEntry) {
            this.entries.put(combine(chunkEntry.getX(), chunkEntry.getZ()), chunkEntry);
        }

        public ChunkEntry removeChunkEntry(int x, int z) {
            return this.entries.remove(combine(x, z));
        }

        public void unload() {
            this.entries.clear();
        }
    }
}
