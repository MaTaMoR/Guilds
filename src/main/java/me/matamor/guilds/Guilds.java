package me.matamor.guilds;

import lombok.Getter;
import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.commonapi.storage.identifier.listener.IdentifierListener;
import me.matamor.guilds.data.ChunkEntry;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.SimpleGuildPlayer;
import me.matamor.guilds.data.database.GuildsDatabase;
import me.matamor.guilds.data.manager.ChunkEntryManager;
import me.matamor.guilds.data.manager.GuildManager;
import me.matamor.guilds.commands.GuildCommand;
import me.matamor.guilds.data.permission.group.GuildRank;
import me.matamor.guilds.listeners.GuildsListener;
import me.matamor.guilds.listeners.ChatListener;
import me.matamor.commonapi.utils.InstantUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.logging.Level;

public class Guilds extends JavaPlugin {

    @Getter
    private GuildsDatabase database;

    @Getter
    private GuildManager guildManager;

    @Getter
    private ChunkEntryManager chunkEntryManager;

    @Override
    public void onEnable() {
        this.database = new GuildsDatabase(this);
        if (!this.database.loadDatabase()) {
            getLogger().log(Level.SEVERE, "Couldn't load Database, disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.guildManager = new GuildManager(this);
        this.chunkEntryManager = new ChunkEntryManager(this);

        // -------------------- LOAD GUILDS --------------------

        getLogger().log(Level.INFO, "Starting to load all the Guilds...");

        //Save the starting time
        Instant start = Instant.now();

        int totalGuilds = 0;
        int totalChunkEntries = 0;
        int totalMembers = 0;

        for (Guild guild : this.database.loadGroups()) {
            //Sum the Guild statics!
            totalGuilds++;

            //Store the Guild so players can access it!
            this.guildManager.storeGuild(guild);

            //Register chunks
            for (ChunkEntry chunkEntry : guild.getChunks()) {
                //Sum the ChunkEntry statics!
                totalChunkEntries++;

                //Register the ChunkEntry into the ChunkEntryManager so it can be detected by the world!
                this.chunkEntryManager.storeChunkEntry(chunkEntry);
            }

            //Register players!
            for (Map.Entry<Integer, GuildRank> entry : guild.getMembers().entrySet()) {
                //Sum the Member statics!
                totalMembers++;

                //Register member if it's not online
                Identifier identifier = CommonAPI.getInstance().getIdentifierManager().getIdentifier(entry.getKey());
                if (identifier != null) {
                    this.guildManager.storePlayer(new SimpleGuildPlayer(identifier, (entry.getValue().isHigherEqual(GuildRank.MEMBER) ? guild : null), false));
                }
            }
        }

        //Register the GuildPlayer when the Identifier is loaded!
        CommonAPI.getInstance().getIdentifierManager().registerListener(getName(), new IdentifierListener() {
            @Override
            public void onLoad(Identifier identifier) {
                getGuildManager().loadPlayer(identifier);
            }

            @Override
            public void onUnload(Identifier identifier) {
                getGuildManager().deletePlayer(identifier.getUUID());
            }
        });

        getLogger().log(Level.INFO, String.format("%s guild(s) have been loaded, containing a total of %s claim(s) and %s player(s)!", totalGuilds, totalChunkEntries, totalMembers));

        Instant end = Instant.now();

        getLogger().log(Level.INFO, "Guilds loaded finished, duration: " + InstantUtils.humanReadableFormat(Duration.between(start, end)));

        // -------------------- LOAD GUILDS --------------------

        getServer().getPluginManager().registerEvents(new GuildsListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        GuildCommand command = new GuildCommand(this);
        command.register();
    }
}
