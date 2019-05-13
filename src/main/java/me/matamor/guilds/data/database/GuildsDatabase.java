package me.matamor.guilds.data.database;

import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.ChunkEntry;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.SimpleChunkEntry;
import me.matamor.guilds.data.SimpleGuild;
import me.matamor.guilds.data.permission.flags.DefaultFlags;
import me.matamor.guilds.data.permission.flags.Flag;
import me.matamor.guilds.data.permission.flags.FlagData;
import me.matamor.guilds.data.permission.group.GuildRank;
import me.matamor.commonapi.storage.database.SimpleDatabaseManager;
import me.matamor.commonapi.storage.database.queries.Queries;
import me.matamor.commonapi.utils.map.Callback;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class GuildsDatabase extends SimpleDatabaseManager {

    public GuildsDatabase(Guilds plugin) {
        super(plugin, "Guilds", 1.0);
    }

    @Override
    public void onConnect(Connection connection) throws SQLException {
        Queries queries = getQueries();

        for (String createQuery : Arrays.asList(queries.getCreate("Guild"), queries.getCreate("Members"), queries.getCreate("ChunksData"), queries.getCreate("Settings"), queries.getCreate("Flags"))) {
            connection.createStatement().execute(createQuery);
        }
    }

    @SuppressWarnings("unchecked")
    public void save(List<Guild> guilds) {
        try {
            try (Connection connection = getConnection()) {
                //Save all the chunk groups
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getInsert("Guild"))) {
                    for (Guild guild : guilds) {
                        statement.setInt(1, guild.getId());
                        statement.setString(2, guild.getName());
                        statement.setInt(3, guild.getOwnerId());

                        //Add entry to the statement
                        statement.addBatch();
                    }

                    //Save all the statement entries
                    statement.executeBatch();
                }

                //Save all the members
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getInsert("Members"))) {
                    for (Guild guild : guilds) {
                        for (Map.Entry<Integer, GuildRank> entry : guild.getMembers().entrySet()) {
                            statement.setInt(1, guild.getId());
                            statement.setInt(2, entry.getKey());
                            statement.setString(3, entry.getValue().name());

                            //Add entry to the statement
                            statement.addBatch();
                        }
                    }

                    //Save all the statement entries
                    statement.executeBatch();
                }

                //Save all the data
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getInsert("ChunksData"))) {
                    //Loop trough all the guilds
                    for (Guild guild : guilds) {
                        //Loop trough all the chunk entries
                        for (ChunkEntry chunkEntry : guild.getChunks()) {
                            statement.setInt(1, guild.getId()); //Guild ID
                            statement.setString(2, chunkEntry.getWorldName()); //World NAME
                            statement.setInt(3, chunkEntry.getX()); //Chunk X
                            statement.setInt(4, chunkEntry.getZ()); //Chunk Z

                            //Add entry to the statement
                            statement.addBatch();
                        }

                        //Save all the statement entries
                        statement.executeBatch();
                    }
                }

                //Save all the settings
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getInsert("Settings"))) {
                    //Loop trough all the guilds
                    for (Guild guild : guilds) {
                        //Loop trough all the settings flags
                        for (Map.Entry<Flag, Object> flagEntry : guild.getSettings().getEntries().entrySet()) {
                            statement.setInt(1, guild.getId()); //Guild ID
                            statement.setString(2, flagEntry.getKey().getName()); //Flag Name
                            statement.setString(3, flagEntry.getKey().serialize(flagEntry.getValue())); //Flag VALUE

                            //Add entry to the statement
                            statement.addBatch();
                        }
                    }

                    //Save all the statement entries
                    statement.executeBatch();
                }

                //Save all the flags
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getInsert("Flags"))) {
                    //Loop trough all the guilds
                    for (Guild guild : guilds) {
                        //Loop trough all the ranks flags data
                        for (Map.Entry<GuildRank, FlagData> entry : guild.getRankFlags().entrySet()) {
                            //Loop trough all the rank flags
                            for (Map.Entry<Flag, Object> flagEntry : entry.getValue().getEntries().entrySet()) {
                                statement.setInt(1, guild.getId()); //Guild ID
                                statement.setString(2, entry.getKey().name()); //Rank NAME
                                statement.setString(3, flagEntry.getKey().getName()); //Flag NAME
                                statement.setString(4, flagEntry.getKey().serialize(flagEntry.getValue())); //Flag VALUE

                                //Add entry to the statement
                                statement.addBatch();
                            }
                        }
                    }

                    //Save all the statement entries
                    statement.executeBatch();
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Couldn't save Guilds!", e);
        }
    }

    public List<Guild> loadGroups() {
        List<Guild> groups = new ArrayList<>();

        try {
            try (Connection connection = getConnection()) {
                //First load the guild_id and name
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getSelect("Guild"))) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            int guildId = resultSet.getInt("guild_id");
                            String name = resultSet.getString("name");
                            int ownerId = resultSet.getInt("owner_id");

                            Guild guild = new SimpleGuild(guildId, name, ownerId);

                            //Load all the 'member_id' with the 'guild_id' we just loaded
                            try (PreparedStatement membersStatement = connection.prepareStatement(getQueries().getSelect("Members"))) {
                                membersStatement.setInt(1, guildId);

                                try (ResultSet membersResultSet = membersStatement.executeQuery()) {
                                    while (membersResultSet.next()) {
                                        int memberId = membersResultSet.getInt("member_id");
                                        GuildRank guildRank = GuildRank.matchType(membersResultSet.getString("rank"));

                                        //Add Member to the Guild
                                        guild.getMembers().put(memberId, guildRank);
                                    }
                                }
                            }

                            //Load all the ChunkEntry using the 'chunk_id'
                            try (PreparedStatement chunksStatement = connection.prepareStatement(getQueries().getSelect("ChunksData"))) {
                                chunksStatement.setInt(1, guildId);

                                try (ResultSet chunksResultSet = chunksStatement.executeQuery()) {
                                    while (chunksResultSet.next()) {
                                        String worldName = chunksResultSet.getString("world_name");
                                        int x = chunksResultSet.getInt("x");
                                        int z = chunksResultSet.getInt("z");

                                        //Add ChunkEntry to the Guild
                                        guild.getChunks().add(new SimpleChunkEntry(guild, worldName, x, z));
                                    }
                                }
                            }

                            //Load all the Settings using the 'guild_id'
                            try (PreparedStatement settingsStatement = connection.prepareStatement(getQueries().getSelect("Settings"))) {
                                settingsStatement.setInt(1, guildId);

                                try (ResultSet settingsResultSet = settingsStatement.executeQuery()) {
                                    while (settingsResultSet.next()) {
                                        Flag flag = DefaultFlags.getFlag(settingsResultSet.getString("flag"));
                                        if (flag == null) {
                                            getPlugin().getLogger().log(Level.SEVERE, String.format("Missing settings flag '%s' for guild '%s'!", settingsResultSet.getString("flag"), guildId));
                                            continue;
                                        }

                                        Object value = flag.deserialize(settingsResultSet.getString("value"));
                                        if (value == null) {
                                            getPlugin().getLogger().log(Level.SEVERE, String.format("Missing settings flag '%s' value for guild '%s'!", flag.getName(), guild.getId()));
                                            continue;
                                        }

                                        //Set the flag value to the FlagData on the Guild
                                        guild.getSettings().set(flag, value);
                                    }
                                }
                            }

                            //Load all the Flags using the 'guild_id'
                            try (PreparedStatement flagsStatement = connection.prepareStatement(getQueries().getSelect("Flags"))) {
                                flagsStatement.setInt(1, guildId);

                                try (ResultSet flagsResultSet = flagsStatement.executeQuery()) {
                                    while (flagsResultSet.next()) {
                                        GuildRank guildRank = GuildRank.matchType(flagsResultSet.getString("rank"));
                                        if (guildRank == null) {
                                            getPlugin().getLogger().log(Level.SEVERE, String.format("Missing rank on flags for guild '%s'!", flagsResultSet.getString("rank")));
                                            continue;
                                        }

                                        Flag flag = DefaultFlags.getFlag(flagsResultSet.getString("flag"));
                                        if (flag == null) {
                                            getPlugin().getLogger().log(Level.SEVERE, String.format("Missing rank flag '%s' for guild '%s'!", flagsResultSet.getString("flag"), guildId));
                                            continue;
                                        }

                                        Object value = flag.deserialize(flagsResultSet.getString("value"));
                                        if (value == null) {
                                            getPlugin().getLogger().log(Level.SEVERE, String.format("Missing rank flag '%s' value for guild '%s'!", flag.getName(), guild.getId()));
                                            continue;
                                        }

                                        //Set the flag value to the FlagData on the Guild
                                        FlagData flagData = guild.getRankFlags().get(guildRank);
                                        flagData.set(flag, value);
                                    }
                                }
                            }

                            //Create the Guild with all the data we loaded!
                            groups.add(guild);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Couldn't load Guilds!", e);
        }

        return groups;
    }

    public Guild createChunkGroup(String name, int ownerId) {
        try {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getInsert("Guild"), Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, name);
                    statement.setInt(2, ownerId);

                    statement.executeUpdate();

                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    generatedKeys.next();

                    return new SimpleGuild(generatedKeys.getInt(1), name, ownerId);
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, String.format("Couldn't create Guild (name= %s, owner_id= %s)!", name, ownerId), e);
        }

        return null;
    }

    public void createChunkGroupAsync(String name, int ownerId, Callback<Guild> callback) {
        runAsync(() -> {
            Guild guild = createChunkGroup(name, ownerId);
            callback.done(guild, null);
        });
    }

    public ChunkEntry createChunkEntry(Guild guild, String worldName, int x, int z) {
        try {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getInsert("ChunksData"))) {
                    statement.setInt(1, guild.getId());
                    statement.setString(2, worldName);
                    statement.setInt(3, x);
                    statement.setInt(4, z);

                    statement.executeUpdate();

                    return new SimpleChunkEntry(guild, worldName, x, z);
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, String.format("Couldn't create ChunkEntry (guild_id= %s, world= %s, x= %s, z= %s)!",
                    guild.getId(), worldName, x, z), e);
        }

        return null;
    }

    public void createChunkEntryAsync(Guild guild, String worldName, int x, int z, Callback<ChunkEntry> callback) {
        runAsync(() -> {
            ChunkEntry chunkEntry = createChunkEntry(guild, worldName, x, z);
            callback.done(chunkEntry, null);
        });
    }

    public boolean deleteGuild(int guildId) {
        try {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getDelete("Guild"))) {
                    statement.setInt(1, guildId);

                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(getQueries().getDelete("ChunkData"))) {
                    statement.setInt(1, guildId);

                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(getQueries().getDelete("MembersAll"))) {
                    statement.setInt(1, guildId);

                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(getQueries().getDelete("Settings"))) {
                    statement.setInt(1, guildId);

                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(getQueries().getDelete("Flags"))) {
                    statement.setInt(1, guildId);

                    statement.executeUpdate();
                }

                return true;
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Couldn't delete Guild (guild_id=" + guildId + ")!", e);
        }

        return false;
    }

    public void deleteGuildAsync(int guildId, Callback<Boolean> callback) {
        runAsync(() -> {
            boolean result = deleteGuild(guildId);
            callback.done(result, null);
        });
    }

    public boolean deleteChunkEntry(String worldName, int x, int z) {
        try {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getDelete("ChunkEntry"))) {
                    statement.setString(1, worldName);
                    statement.setInt(2, x);
                    statement.setInt(3, z);

                    statement.executeUpdate();

                    return true;
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, String.format("Couldn't delete ChunkEntry (world_name= %s, x= %s, z= %s)!",
                    worldName, x, z), e);
        }

        return false;
    }

    public void deleteChunkEntryAsync(String worldName, int x, int z, Callback<Boolean> callback) {
        runAsync(() -> {
            boolean result = deleteChunkEntry(worldName, x, z);
            callback.done(result, null);
        });
    }

    public boolean saveMember(int guildId, int memberId, String guildRank) {
        try {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getInsert("Members"))) {
                    statement.setInt(1, guildId);
                    statement.setInt(2, memberId);
                    statement.setString(3, guildRank);

                    statement.executeUpdate();

                    return true;
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, String.format("Couldn't save Members (guild_id= %s, member_id= %s, rank= %s)!",
                    guildId, memberId, guildRank), e);
        }

        return false;
    }

    public void saveMemberAsync(int guildId, int memberId, String guildRank, Callback<Boolean> callback) {
        runAsync(() -> {
            boolean result = saveMember(guildId, memberId, guildRank);
            callback.done(result, null);
        });
    }

    public boolean deleteMembers(int guildId, String guildRank) {
        try {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getDelete("MembersRank"))) {
                    statement.setInt(1, guildId);
                    statement.setString(2, guildRank);

                    statement.executeUpdate();

                    return true;
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, String.format("Couldn't delete Members (guild_id= %s, rank= %s)!",
                    guildId, guildRank), e);
        }

        return false;
    }

    public void deleteMembersAsync(int guildId, String guildRank, Callback<Boolean> callback) {
        runAsync(() -> {
            boolean result = deleteMembers(guildId, guildRank);
            callback.done(result, null);
        });
    }


    public boolean deleteMember(int guildId, int memberId) {
        try {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getDelete("Member"))) {
                    statement.setInt(1, guildId);
                    statement.setInt(2, memberId);

                    statement.executeUpdate();

                    return true;
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, String.format("Couldn't delete Member (guild_id= %s, member_id= %s)!",
                    guildId, memberId), e);
        }

        return false;
    }

    public void deleteMemberAsync(int guildId, int memberId, Callback<Boolean> callback) {
        runAsync(() -> {
            boolean result = deleteMember(guildId, memberId);
            callback.done(result, null);
        });
    }

    @SuppressWarnings("unchecked")
    public boolean saveSetting(int guildId, Flag flag, Object value) {
        try {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getInsert("Settings"))) {
                    statement.setInt(1, guildId);
                    statement.setString(2, flag.getName());
                    statement.setString(3, flag.serialize(value));

                    statement.executeUpdate();

                    return true;
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, String.format("Couldn't save Settings (guild_id= %s, flag= %s, value= %s)!",
                    guildId, flag.getName(), value), e);
        }

        return false;
    }

    public void saveSettingAsync(int guildId, Flag flag, Object value, Callback<Boolean> callback) {
        runAsync(() -> {
            boolean result = saveSetting(guildId, flag, value);
            callback.done(result, null);
        });
    }

    @SuppressWarnings("unchecked")
    public boolean saveFlag(int guildId, String guildRank, Flag flag, Object value) {
        try {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(getQueries().getInsert("Flags"))) {
                    statement.setInt(1, guildId);
                    statement.setString(2, guildRank);
                    statement.setString(3, flag.getName());
                    statement.setString(4, flag.serialize(value));

                    statement.executeUpdate();

                    return true;
                }
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, String.format("Couldn't save Flag (guild_id= %s, rank= %s, flag= %s, value= %s)!",
                    guildId, guildRank, flag.getName(), value), e);
        }

        return false;
    }

    public void saveFlagAsync(int guildId, String guildRank, Flag flag, Object value, Callback<Boolean> callback) {
        runAsync(() -> {
            boolean result = saveFlag(guildId, guildRank, flag, value);
            callback.done(result, null);
        });
    }
}
