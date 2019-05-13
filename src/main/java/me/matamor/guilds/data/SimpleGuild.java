package me.matamor.guilds.data;

import lombok.Getter;
import me.matamor.guilds.data.chat.GuildChat;
import me.matamor.guilds.data.permission.flags.Flag;
import me.matamor.guilds.data.permission.flags.FlagData;
import me.matamor.guilds.data.permission.flags.FlagStatus;
import me.matamor.guilds.data.permission.group.GuildRank;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleGuild implements Guild {

    @Getter
    private final int id;

    @Getter
    private final String name;

    @Getter
    private final int ownerId;

    @Getter
    private final FlagData settings = new FlagData();

    @Getter
    private final Map<GuildRank, FlagData> rankFlags = new LinkedHashMap<GuildRank, FlagData>() {{
        //Fill the map with all the group types!
        for (GuildRank guildRank : GuildRank.values()) {
            put(guildRank, new FlagData());
        }
    }};

    private final Map<Integer, GuildRank> members = new LinkedHashMap<>();

    @Getter
    private final List<ChunkEntry> chunks = new ArrayList<>();

    @Getter
    private final List<Integer> invitations = new ArrayList<>();

    @Getter
    private final GuildChat chat = new GuildChat(this);

    public SimpleGuild(int groupId, String name, int ownerId) {
        this.id = groupId;
        this.name = name;
        this.ownerId = ownerId;

        //Add the owner to the members list!
        this.members.put(ownerId, GuildRank.OWNER);
    }

    @Override
    public GuildRank getMemberRank(int id) {
        GuildRank groupType;

        if (this.ownerId == id) {
            groupType = GuildRank.OWNER;
        } else {
            groupType = this.members.get(id);

            if (groupType == null) {
                groupType = GuildRank.DEFAULT;
            }
        }

        return groupType;
    }

    @Override
    public FlagData getRankFlags(GuildRank guildRank) {
        return this.rankFlags.get(guildRank);
    }

    @Override
    public Map<Integer, GuildRank> getMembers() {
        return this.members;
    }

    @Override
    public List<Map.Entry<Integer, GuildRank>> getMembers(GuildRank... guildRanks) {
        return this.members.entrySet().stream().filter(e -> e.getValue().is(guildRanks)).collect(Collectors.toList());
    }

    @Override
    public void removeMembers(GuildRank guildRank) {
        this.members.values().removeIf(e -> e == guildRank);
    }

    @Override
    public <T> T getSetting(Flag<T> flag) {
        return this.settings.get(flag);
    }

    @Override
    public <T> T getFlag(GuildRank guildRank, Flag<T> flag) {
        return this.rankFlags.get(guildRank).get(flag);
    }

    @Override
    public <T> FlagStatus checkSetting(Flag<T> flag, T value) {
        return this.settings.checkFlag(flag, value);
    }

    @Override
    public <T> FlagStatus checkFlag(int playerId, Flag<T> flag, T value) {
        if (this.ownerId == playerId) {
            return FlagStatus.ALLOW;
        }

        //Check if the 'playerId' has any given group, if not give default group!
        GuildRank groupType = this.members.get(playerId);
        if (groupType == null) {
            groupType = GuildRank.DEFAULT;
        }

        //Check the Flag for the group!
        FlagData flagData = this.rankFlags.get(groupType);
        return flagData.checkFlag(flag, value);
    }
}
