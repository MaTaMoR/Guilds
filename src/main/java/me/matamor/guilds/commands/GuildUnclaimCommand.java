package me.matamor.guilds.commands;

import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.ChunkEntry;
import me.matamor.guilds.data.permission.group.GuildRank;
import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.commonapi.storage.identifier.Identifier;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class GuildUnclaimCommand extends ICommand<Guilds> {

    public GuildUnclaimCommand(Guilds plugin) {
        super(plugin, "unclaim", "quita un claim de tu gremio", new String[] { });

        setOnlyInGame(true);
    }

    @Override
    public void onCommand(CommandArgs commandArgs) throws ICommandException {
        Player player = commandArgs.getPlayer();

        //Load the player data
        Identifier identifier = CommonAPI.getInstance().getIdentifierManager().getIdentifier(player.getUniqueId());
        notNull(identifier, "&cNo se ha podido cargar tu información, reconectate y prueba de nuevo!");

        //Check if any claim exists on this chunk
        Chunk chunk = player.getLocation().getChunk();
        ChunkEntry chunkEntry = getPlugin().getChunkEntryManager().getChunkEntry(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        notNull(chunkEntry, "&cNo existe ningun claim donde estas!");

        //Check if the player has permissions to unclaim the land!
        ifFalse((chunkEntry.getGuild().getMemberRank(identifier.getId()).isHigherEqual(GuildRank.OWNER) || player.hasPermission("guilds.bypass")), "&cEste claim no te partenece!");

        commandArgs.sendMessage("&2Borrando claim!");

        //Delete the claim
        getPlugin().getDatabase().deleteChunkEntryAsync(chunkEntry.getWorldName(), chunkEntry.getX(), chunkEntry.getZ(), (result, exception) -> {
            if (result) {
                getPlugin().getChunkEntryManager().removeChunkEntry(chunkEntry.getWorldName(), chunkEntry.getX(), chunkEntry.getZ());

                chunkEntry.getGuild().getChunks().remove(chunkEntry);

                commandArgs.sendMessage("&aClaim borrado correctamente! (mundo: %s, x: %s, z: %s)", chunkEntry.getWorldName(), chunkEntry.getX(), chunkEntry.getZ());
            } else {
                commandArgs.sendMessage("&cNo se pudo borrar el claim, contacta con MaTaMoR_!");
            }
        });
    }
}
