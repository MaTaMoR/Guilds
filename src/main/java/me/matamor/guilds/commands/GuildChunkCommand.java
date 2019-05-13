package me.matamor.guilds.commands;

import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.ChunkEntry;
import me.matamor.guilds.data.Guild;
import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.commonapi.storage.identifier.Identifier;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class GuildChunkCommand extends ICommand<Guilds> {

    public GuildChunkCommand(Guilds plugin) {
        super(plugin, "chunk", "informacion del chunk en el que te encuentras", new String[]{});

        setOnlyInGame(true);
    }

    @Override
    public void onCommand(CommandArgs commandArgs) throws ICommandException {
        Player player = commandArgs.getPlayer();

        Chunk chunk = player.getLocation().getChunk();
        ChunkEntry chunkEntry = getPlugin().getChunkEntryManager().getChunkEntry(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        notNull(chunkEntry, "&cNo existe ningun claim donde estas!");

        Identifier identifier = CommonAPI.getInstance().getIdentifierManager().getIdentifier(chunkEntry.getGuild().getOwnerId());
        if (identifier == null) {
            commandArgs.sendMessage("&2Cargando informacion del dueño...");

            getPlugin().getDatabase().runAsync(() -> {
                Identifier result = CommonAPI.getInstance().getIdentifierManager().load(chunkEntry.getGuild().getOwnerId());
                if (result == null) {
                    commandArgs.sendMessage("&cNo se ha podido cargar la informacion del dueño...");
                } else {
                    commandArgs.sendMessage("&aEste chunk ha sido claimeado por el gremio &2%s(&f%s&2)!", chunkEntry.getGuild().getName(), result.getName());

                    if (player.hasPermission("guilds.bypass")) {
                        sendInfo(commandArgs, chunkEntry.getGuild());
                    }
                }
            });
        } else {
            commandArgs.sendMessage("&aEste chunk ha sido claimeado por el gremio &2%s(&f%s&2)!", chunkEntry.getGuild().getName(), identifier.getName());

            if (player.hasPermission("guilds.bypass")) {
                sendInfo(commandArgs, chunkEntry.getGuild());
            }
        }
    }

    private void sendInfo(CommandArgs commandArgs, Guild guild) {

    }
}
