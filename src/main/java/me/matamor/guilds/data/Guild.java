package me.matamor.guilds.data;

import me.matamor.guilds.data.chat.GuildChat;
import me.matamor.guilds.data.permission.flags.Flag;
import me.matamor.guilds.data.permission.flags.FlagData;
import me.matamor.guilds.data.permission.flags.FlagStatus;
import me.matamor.guilds.data.permission.group.GuildRank;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Guild {

    int getId();

    String getName();

    int getOwnerId();

    FlagData getSettings();

    Map<GuildRank, FlagData> getRankFlags();

    FlagData getRankFlags(GuildRank guildRank);

    Map<Integer, GuildRank> getMembers();

    GuildRank getMemberRank(int id);

    List<Map.Entry<Integer, GuildRank>> getMembers(GuildRank... guildRanks);

    Collection<Integer> getInvitations();

    GuildChat getChat();

    void removeMembers(GuildRank guildRank);

    List<ChunkEntry> getChunks();

    <T> T getSetting(Flag<T> flag);

    <T> T getFlag(GuildRank groupType, Flag<T> flag);

    <T> FlagStatus checkSetting(Flag<T> flag, T value);

    <T> FlagStatus checkFlag(int playerId, Flag<T> flag, T value);

}
