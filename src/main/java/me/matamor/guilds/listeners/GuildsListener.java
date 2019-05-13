package me.matamor.guilds.listeners;

import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.ChunkEntry;
import me.matamor.guilds.data.permission.flags.DefaultFlags;
import me.matamor.guilds.data.permission.flags.FlagStatus;
import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.commonapi.utils.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class GuildsListener implements Listener {

    private final Guilds plugin;

    public GuildsListener(Guilds plugin) {
        this.plugin = plugin;
    }

    private ChunkEntry getEntry(Chunk chunk) {
        return this.plugin.getChunkEntryManager().getChunkEntry(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    private Identifier getIdentifier(Player player) {
        return CommonAPI.getInstance().getIdentifierManager().getIdentifier(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Identifier identifier = getIdentifier(player);

        if (identifier == null) {
            player.kickPlayer(StringUtils.color("&cNo se ha podido cargar tu informacion, vuelve a entrar!"));
        } else {
            this.plugin.getGuildManager().loadPlayer(identifier);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("guilds.bypass")) {
            return;
        }

        Chunk chunk = event.getBlock().getChunk();
        ChunkEntry chunkEntry = getEntry(chunk);

        if (chunkEntry != null) {
            Identifier identifier = getIdentifier(player);

            if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.ALLOW_BUILD, true) == FlagStatus.DENY) {
                event.setCancelled(true);

                player.sendMessage(StringUtils.color("&cNo tienes permisos para romper bloques!"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("guilds.bypass")) {
            return;
        }

        Chunk chunk = event.getBlock().getChunk();
        ChunkEntry chunkEntry = getEntry(chunk);

        if (chunkEntry != null) {
            Identifier identifier = getIdentifier(player);

            if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.ALLOW_BUILD, true) == FlagStatus.DENY) {
                event.setCancelled(true);

                player.sendMessage(StringUtils.color("&cNo tienes permisos para poner bloques!"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(BlockExplodeEvent event) {
        if (event.isCancelled()) return;

        Map<Chunk, ChunkEntry> entries = new HashMap<>();

        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();

            ChunkEntry chunkEntry = entries.get(block.getChunk());
            if (chunkEntry == null) {
                chunkEntry = getEntry(block.getChunk());

                //Nothing to do here, not in a region!
                if (chunkEntry == null) {
                    continue;
                }

                entries.put(block.getChunk(), chunkEntry);
            }

            iterator.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(EntityExplodeEvent event) {
        if (event.isCancelled()) return;

        Map<Chunk, ChunkEntry> entries = new HashMap<>();

        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();

            ChunkEntry chunkEntry = entries.get(block.getChunk());
            if (chunkEntry == null) {
                chunkEntry = getEntry(block.getChunk());

                //Nothing to do here, not in a region!
                if (chunkEntry == null) {
                    continue;
                }

                entries.put(block.getChunk(), chunkEntry);
            }

            iterator.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || (event.getAction() != Action.PHYSICAL &&
                (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == EquipmentSlot.OFF_HAND ||! event.getClickedBlock().getType().isInteractable()))) return;

        Player player = event.getPlayer();
        if (player.hasPermission("guilds.bypass")) {
            return;
        }

        Block block = event.getClickedBlock();

        Chunk chunk = block.getChunk();
        ChunkEntry chunkEntry = getEntry(chunk);

        if (chunkEntry != null) {
            Identifier identifier = getIdentifier(player);

            BlockData blockData = event.getClickedBlock().getBlockData();
            Material material = block.getType();

            if (event.getAction() == Action.PHYSICAL) {
                if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.REDSTONE, true) == FlagStatus.DENY) {
                    event.setCancelled(true);
                }
            } else {
                if (isDoor(blockData)) { //PUERTAS
                    if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.DOORS, true) == FlagStatus.DENY) {
                        event.setCancelled(true);

                        player.sendMessage(StringUtils.color("&cNo tienes permisos para abrir!"));
                    }
                } else if (isChest(blockData)) { //COFRES
                    if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.CHESTS, true) == FlagStatus.DENY) {
                        event.setCancelled(true);

                        player.sendMessage(StringUtils.color("&cNo tienes permisos para usar cofres!"));
                    }
                } else if (isFurnace(blockData)) { //HORNOS
                    if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.FURNACES, true) == FlagStatus.DENY) {
                        event.setCancelled(true);

                        player.sendMessage(StringUtils.color("&cNo tienes permisos para usar hornos!"));
                    }
                } else if (isAnvil(material)) { //ANVILES
                    if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.ANVIL, true) == FlagStatus.DENY) {
                        event.setCancelled(true);

                        player.sendMessage(StringUtils.color("&cNo tienes permisos para usar anviles!"));
                    }
                } else if (isEnchantingTable(material)) { //MESA DE ENCANTAMIENTOS
                    if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.ENCHANT_TABLE, true) == FlagStatus.DENY) {
                        event.setCancelled(true);

                        player.sendMessage(StringUtils.color("&cNo tienes permisos para usar mesas de encantamiento!"));
                    }
                } else {
                    if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.INTERACT, true) == FlagStatus.DENY) {
                        event.setCancelled(true);

                        player.sendMessage(StringUtils.color("&cNo tienes permisos para interactuar!"));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucket(PlayerBucketFillEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("guilds.bypass")) {
            return;
        }

        Block block = event.getBlockClicked();

        Chunk chunk = block.getChunk();
        ChunkEntry chunkEntry = getEntry(chunk);

        if (chunkEntry != null) {
            Identifier identifier = getIdentifier(player);

            if (block.getType() == Material.WATER) {
                if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.WATER_BUCKET, true) == FlagStatus.DENY) {
                    event.setCancelled(true);

                    player.sendMessage(StringUtils.color("&cNo tienes permisos para llenar un cubo de agua!"));
                }
            } else if (block.getType() == Material.LAVA) {
                if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.LAVA_BUCKET, true) == FlagStatus.DENY) {
                    event.setCancelled(true);

                    player.sendMessage(StringUtils.color("&cNo tienes permisos para llenar un cubo de lava!"));
                }
            } else {
                if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.OTHER_BUCKET, true) == FlagStatus.DENY) {
                    event.setCancelled(true);

                    player.sendMessage(StringUtils.color("&cNo tienes permisos para llenar un cubo!"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucket(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("guilds.bypass")) {
            return;
        }

        Block block = event.getBlockClicked();

        Chunk chunk = block.getChunk();
        ChunkEntry chunkEntry = getEntry(chunk);

        if (chunkEntry != null) {
            Identifier identifier = getIdentifier(player);

            if (event.getBucket() == Material.WATER_BUCKET) {
                if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.WATER_BUCKET, true) == FlagStatus.DENY) {
                    event.setCancelled(true);

                    player.sendMessage(StringUtils.color("&cNo tienes permisos para vaciar el cubo de agua!"));
                }
            } else if (event.getBucket() == Material.LAVA_BUCKET) {
                if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.LAVA_BUCKET, true) == FlagStatus.DENY) {
                    event.setCancelled(true);

                    player.sendMessage(StringUtils.color("&cNo tienes permisos para vaciar el cubo de lava!"));
                }
            } else {
                if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.OTHER_BUCKET, true) == FlagStatus.DENY) {
                    event.setCancelled(true);

                    player.sendMessage(StringUtils.color("&cNo tienes permisos para vaciar el cubo!"));
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;

        Player damager = null;

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            if (entityEvent.getDamager() instanceof Player) {
                damager = (Player) entityEvent.getDamager();
            } else if (entityEvent.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) entityEvent.getDamager();

                if (projectile.getShooter() instanceof Player) {
                    damager = (Player) projectile.getShooter();
                }
            } else if (entityEvent.getDamager() instanceof TNTPrimed) {
                TNTPrimed tntPrimed = (TNTPrimed) entityEvent.getDamager();

                if (tntPrimed.getSource() instanceof Player) {
                    damager = (Player) tntPrimed.getSource();
                }
            } else if (entityEvent.getDamager() instanceof ThrownPotion) {
                ThrownPotion potion = (ThrownPotion) entityEvent.getDamager();

                if (potion.getShooter() instanceof Player) {
                    damager = (Player) potion.getShooter();
                }
            }

            if (damager != null) {
                if (damager.hasPermission("guilds.bypass")) {
                    return;
                }

                Entity damaged = event.getEntity();

                ChunkEntry chunkEntry = getEntry(damaged.getLocation().getChunk());
                if (chunkEntry == null) {
                    return;
                }

                if (damaged instanceof Player) {
                    if (chunkEntry.getGuild().checkSetting(DefaultFlags.PVP, true) == FlagStatus.DENY) {
                        damager.sendMessage(StringUtils.color("&cEl PvP esta desactivado en esta region!"));
                        event.setCancelled(true);
                    }
                } else {
                    if (damaged instanceof Creature) {
                        Identifier identifier = getIdentifier(damager);

                        //Check if damage entity is a Monster!
                        if (damaged instanceof Monster) {
                            //If the flag MONSTERS_PROTECTION is disabled cancel the event EntityDamageEvent
                            if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.MONSTERS_PROTECTION, false) == FlagStatus.DENY) {
                                damager.sendMessage(StringUtils.color("&cNo puedes dañar los monstruos de esta region!"));
                                event.setCancelled(true);
                            }
                        } else {
                            //If the flag ANIMALS_PROTECTION is disabled cancel the event EntityDamageEvent
                            if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.ANIMALS_PROTECTION, false) == FlagStatus.DENY) {
                                damager.sendMessage(StringUtils.color("&cNo puedes dañar los animales de esta region!"));
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getToBlock();

        ChunkEntry chunkEntry = getEntry(block.getChunk());
        if (chunkEntry == null) return;

        Material material = event.getBlock().getType();
        if (material == Material.WATER) {
            //If the flag WATER_FLOW is disabled cancel the BlockFromToEvent
            if (chunkEntry.getGuild().checkSetting(DefaultFlags.WATER_FLOW, true) == FlagStatus.DENY) {
                event.setCancelled(true);
            }
        } else if (material == Material.LAVA) {
            //If the flag LAVA_FLOW is disabled cancel the BlockFromToEvent
            if (chunkEntry.getGuild().checkSetting(DefaultFlags.LAVA_FLOW, true) == FlagStatus.DENY) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBurn(BlockBurnEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getIgnitingBlock();

        ChunkEntry chunkEntry = getEntry(block.getChunk());
        if (chunkEntry == null) return;

        //If the flag FIRE_BURNING is disabled cancel the BlockBurnEvent
        if (chunkEntry.getGuild().checkSetting(DefaultFlags.FIRE_BURNING, true) == FlagStatus.DENY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireSpread(BlockSpreadEvent event) {
        if (event.isCancelled() || event.getSource().getType() != Material.FIRE) return;

        Block block = event.getBlock();

        ChunkEntry chunkEntry = getEntry(block.getChunk());
        if (chunkEntry == null) return;

        //If the flag FIRE_BURNING is disabled cancel the BlockSpreadEvent
        if (chunkEntry.getGuild().checkSetting(DefaultFlags.FIRE_BURNING, true) == FlagStatus.DENY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLighterUse(BlockIgniteEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();

        ChunkEntry chunkEntry = getEntry(block.getChunk());
        if (chunkEntry == null) return;

        if (event.getIgnitingEntity() instanceof Player) {
            Player player = (Player) event.getIgnitingEntity();
            if (player.hasPermission("guilds.bypass")) {
                return;
            }

            Identifier identifier = getIdentifier(player);

            //If the flag LIGHTER_USE is disabled for the player cancel the BlockIgniteEvent
            if (chunkEntry.getGuild().checkFlag(identifier.getId(), DefaultFlags.LIGHTER_USE, true) == FlagStatus.DENY) {
                event.setCancelled(true);
            }
        } else {
            //If the flag FIRE_BURNING is disabled cancel the BlockIgniteEvent
            if (chunkEntry.getGuild().checkSetting(DefaultFlags.FIRE_BURNING, true) == FlagStatus.DENY) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isDoor(BlockData blockData) {
        return blockData instanceof Door ||
                blockData instanceof Gate ||
                blockData instanceof TrapDoor;
    }

    private boolean isChest(BlockData blockData) {
        return blockData instanceof Chest;
    }

    private boolean isFurnace(BlockData blockData) {
        return blockData instanceof Furnace;
    }

    private boolean isAnvil(Material material) {
        switch (material) {
            case ANVIL:
            case CHIPPED_ANVIL:
            case DAMAGED_ANVIL:
                return true;
            default:
                return false;
        }
    }

    private boolean isEnchantingTable(Material material) {
        return material == Material.ENCHANTING_TABLE;
    }
}
