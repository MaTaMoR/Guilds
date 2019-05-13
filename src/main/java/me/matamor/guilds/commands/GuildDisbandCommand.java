package me.matamor.guilds.commands;

import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.commonapi.utils.StringUtils;
import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.ChunkEntry;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.GuildPlayer;
import me.matamor.guilds.data.permission.group.GuildRank;
import org.bukkit.entity.Player;

public class GuildDisbandCommand extends ICommand<Guilds> {

    public GuildDisbandCommand(Guilds plugin) {
        super(plugin, "disolver", "disuelve tu gremio", new String[] { "disband", "d" });

        setOnlyInGame(true);
    }

    @Override
    public void onCommand(CommandArgs commandArgs) throws ICommandException {
        Player player = commandArgs.getPlayer();

        Identifier identifier = CommonAPI.getInstance().getIdentifierManager().getIdentifier(player.getUniqueId());
        notNull(identifier, "&cNo se ha podido cargar tu información, reconectate y prueba de nuevo!");

        Guild guild;

        //If no arguments provides it's a self claim
        if (commandArgs.length == 0) {
            guild = getPlugin().getGuildManager().getGuild(identifier.getId(), GuildRank.OWNER);
            notNull(guild, "&cNo eres dueño de ningun gremio!");
        } else { //Arguments provided, player is trying to claim for another guild!
            guild = getPlugin().getGuildManager().getGuild(commandArgs.getString(0));
            notNull(guild, "&cEl gremio &4%s&c no existe!", commandArgs.getString(0));

            ifTrue(guild.getMemberRank(identifier.getId()).isLower(GuildRank.OWNER) && !player.hasPermission("guilds.bypass"), "&cNo tienes permisos para disolver otrp gremio!");
        }

        commandArgs.sendMessage("&2Borrando gremio...");

        getPlugin().getDatabase().deleteGuildAsync(guild.getId(), (result, exception) -> getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
            //Remove all the chunk entries!
            for (ChunkEntry chunkEntry : guild.getChunks()) {
                getPlugin().getChunkEntryManager().removeChunkEntry(chunkEntry.getWorldName(), chunkEntry.getX(), chunkEntry.getZ());
            }

            //Delete the guild!
            getPlugin().getGuildManager().deleteGuild(guild.getId());

            //Un set the guild for the members!
            for (int memberId : guild.getMembers().keySet()) {
                GuildPlayer guildPlayer = getPlugin().getGuildManager().getPlayer(memberId);
                if (guildPlayer == null) continue;

                Guild newGuild = getPlugin().getGuildManager().getGuild(memberId, GuildRank.MEMBER);
                guildPlayer.setGuild(newGuild);
            }

            //Notify the guild!
            guild.getChat().sendChatMessage(StringUtils.color("&cTu gremio ha sido disuelto!"));
        }));
    }
}
