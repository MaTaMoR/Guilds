package me.matamor.guilds.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.ChunkEntry;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.permission.group.GuildRank;
import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.commonapi.storage.identifier.Identifier;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class GuildClaimCommand extends ICommand<Guilds> {

    public GuildClaimCommand(Guilds plugin) {
        super(plugin, "claim", "añade un claim a tu gremio", new String[] { });

        setOnlyInGame(true);
    }

    @Override
    public void onCommand(CommandArgs commandArgs) throws ICommandException {
        Player player = commandArgs.getPlayer();

        Guild guild;

        //If no arguments provides it's a self claim
        if (commandArgs.length == 0) {
            Identifier identifier = CommonAPI.getInstance().getIdentifierManager().getIdentifier(player.getUniqueId());
            notNull(identifier, "&cNo se ha podido cargar tu información, reconectate y prueba de nuevo!");

            guild = getPlugin().getGuildManager().getGuild(identifier.getId(), GuildRank.OWNER);
            notNull(guild, "&cNo eres dueño de ningun gremio!");
        } else { //Arguments provided, player is trying to claim for another guild!
            ifFalse(player.hasPermission("guilds.bypass"), "&cNo tienes permisos para claimear por otra guild!");

            guild = getPlugin().getGuildManager().getGuild(commandArgs.getString(0));
            notNull(guild, "&cLa guild &4%s&c no existe!", commandArgs.getString(0));
        }

        //Check if the Chunk is already claimed!
        Chunk chunk = player.getLocation().getChunk();
        ChunkEntry chunkEntry = getPlugin().getChunkEntryManager().getChunkEntry(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        ifFalse(chunkEntry == null, "&cYa existe un claim donde estas!");

        //Check if the player is trying to claim a chunk in a region
        if (checkRegion(chunk)) {
            commandArgs.sendMessage("&cNo puedes crear un claim dentro de una region!");
        } else {
            ifTrue(guild.getChunks().size() >= 18, "&cNo puedes crear mas de 18 claims!");

            commandArgs.sendMessage("&2Creando claim!");

            //Create claim in database
            getPlugin().getDatabase().createChunkEntryAsync(guild, chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), (result, exception) -> {
                if (result == null) {
                    commandArgs.sendMessage("&cNo se pudo crear el claim, contacta con MaTaMoR_!");
                } else {
                    getPlugin().getChunkEntryManager().storeChunkEntry(result);

                    guild.getChunks().add(result);

                    commandArgs.sendMessage("&aClaim creado correctamente! (mundo: %s, x: %s, z: %s)", result.getWorldName(), result.getX(), result.getZ());
                }
            });
        }
    }

    private boolean checkRegion(Chunk chunk) {
        if (getPlugin().getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            int bx = chunk.getX() << 4;
            int bz = chunk.getZ() << 4;

            BlockVector3 pt1 = BlockVector3.at(bx, 0, bz);
            BlockVector3  pt2 = BlockVector3.at(bx + 15, 256, bz + 15);

            ProtectedRegion region = new ProtectedCuboidRegion("test-region", pt1, pt2);

            World world = BukkitAdapter.adapt(chunk.getWorld());

            RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
            if (regions == null) return false;

            ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(region);
            return applicableRegionSet.getRegions().size() > 0;
        }

        return false;
    }
}
