package me.matamor.guilds.commands;

import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.ChunkEntry;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.permission.group.GuildRank;
import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.commonapi.storage.identifier.Identifier;
import org.bukkit.entity.Player;

import java.util.Map;

public class GuildInfoCommand extends ICommand<Guilds> {

    public GuildInfoCommand(Guilds plugin) {
        super(plugin, "info", "informacion de tu gremio", new String[] { });
    }

    @Override
    public void onCommand(CommandArgs commandArgs) throws ICommandException {
        Player player = commandArgs.getPlayer();

        Guild guild;

        //If no arguments provides it's a self claim
        if (commandArgs.length == 0) {
            Identifier identifier = CommonAPI.getInstance().getIdentifierManager().getIdentifier(player.getUniqueId());
            notNull(identifier, "&cNo se ha podido cargar tu información, reconectate y prueba de nuevo!");

            guild = getPlugin().getGuildManager().getGuild(identifier.getId(), GuildRank.MEMBER);
            notNull(guild, "&cNo eres miembro de ningun gremio!");
        } else { //Arguments provided, player is trying to claim for another guild!
            ifFalse(player.hasPermission("guilds.bypass"), "&cNo tienes permisos para ver la informacion de otra guild!");

            guild = getPlugin().getGuildManager().getGuild(commandArgs.getString(0));
            notNull(guild, "&cLa guild &4%s&c no existe!", commandArgs.getString(0));
        }

        commandArgs.sendMessage("&aCargando información...");

        getPlugin().getDatabase().runAsync(() -> {
            commandArgs.sendMessage("&aInformación de la guild: &2" + guild.getName());
            commandArgs.sendMessage("&f> &7Id &8" + guild.getId());

            Identifier owner = CommonAPI.getInstance().getIdentifierManager().load(guild.getOwnerId());
            if (owner != null) {
                commandArgs.sendMessage("&f> &7Dueño &8" + owner.getName());
            }

            //Send members data
            if (guild.getMembers().isEmpty()) {
                commandArgs.sendMessage("&f> &7Miembros &cninguno");
            } else {
                commandArgs.sendMessage("&f> &7Miembros: ");

                for (Map.Entry<Integer, GuildRank> entry : guild.getMembers().entrySet()) {
                    if (entry.getValue() == GuildRank.OWNER) continue;

                    Identifier memberIdentifier = CommonAPI.getInstance().getIdentifierManager().load(entry.getKey());
                    if (memberIdentifier != null) {
                        commandArgs.sendMessage("&f    > &8%s &f- &8%s", memberIdentifier.getName(), entry.getValue());
                    }
                }
            }

            //Set data data
            if (guild.getChunks().isEmpty()) {
                commandArgs.sendMessage("&f> &7Chunks &cninguno");
            } else {
                commandArgs.sendMessage("&f> &7Chunks");

                for (ChunkEntry chunkEntry : guild.getChunks()) {
                    commandArgs.sendMessage("&f    > &8%s &f/ &8%s &f/ &8%s", chunkEntry.getWorldName(), chunkEntry.getX(), chunkEntry.getZ());
                }
            }
        });
    }
}
