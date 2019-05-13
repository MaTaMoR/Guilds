package me.matamor.guilds.data.manager;

import lombok.Getter;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.commonapi.utils.Validate;
import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.GuildPlayer;
import me.matamor.guilds.data.SimpleGuildPlayer;
import me.matamor.guilds.data.permission.group.GuildRank;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GuildManager {

    private final Map<Integer, Guild> entries = new ConcurrentHashMap<>();
    private final Map<UUID, GuildPlayer> players = new ConcurrentHashMap<>();

    @Getter
    private final Guilds plugin;

    public GuildManager(Guilds plugin) {
        this.plugin = plugin;
    }

    public void storeGuild(Guild guild) {
        Validate.notNull(guild, "Guild can't be null!");

        this.entries.put(guild.getId(), guild);
    }

    public void storePlayer(GuildPlayer guildPlayer) {
        Validate.notNull(guildPlayer, "GuildPlayer can't be null!");

        this.players.put(guildPlayer.getIdentifier().getUUID(), guildPlayer);
    }

    public void loadPlayer(Identifier identifier) {
        if (!this.players.containsKey(identifier.getUUID())) {
            Guild guild = getGuild(identifier.getId(), GuildRank.MEMBER);
            this.players.put(identifier.getUUID(), new SimpleGuildPlayer(identifier, guild, false));
        }
    }

    public Guild getGuild(int guildId) {
        return this.entries.get(guildId);
    }

    public Guild getGuild(String name) {
        return this.entries.values().stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public Guild getGuild(int memberId, GuildRank guildRank) {
        return this.entries.values().stream()
                .filter(e -> e.getMemberRank(memberId).isHigherEqual(guildRank))
                .findFirst().orElse(null);
    }

    public GuildPlayer getPlayer(int memberId) {
        return this.players.values().stream()
                .filter(e -> e.getIdentifier().getId() == memberId)
                .findFirst().orElse(null);
    }

    public GuildPlayer getPlayer(UUID uuid) {
        return this.players.get(uuid);
    }

    public GuildPlayer getPlayer(String name) {
        return this.players.values().stream()
                .filter(e -> e.getIdentifier().getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public Collection<Guild> getGuilds() {
        return Collections.unmodifiableCollection(this.entries.values());
    }

    public Collection<GuildPlayer> getPlayers() {
        return Collections.unmodifiableCollection(this.players.values());
    }

    public void deleteGuild(int guildId) {
        this.entries.remove(guildId);
    }

    public void deletePlayer(UUID uuid) {
        this.players.remove(uuid);
    }
}
