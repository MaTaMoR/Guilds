package me.matamor.guilds.commands;

import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.GuildPlayer;
import me.matamor.guilds.data.permission.flags.DefaultFlags;
import me.matamor.guilds.data.permission.flags.Flag;
import me.matamor.guilds.data.permission.flags.FlagType;
import me.matamor.guilds.data.permission.group.GuildRank;
import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.commonapi.custominventories.actions.ClickAction;
import me.matamor.commonapi.custominventories.icons.InventoryIcon;
import me.matamor.commonapi.custominventories.icons.NormalInventoryIcon;
import me.matamor.commonapi.custominventories.inventories.CustomInventory;
import me.matamor.commonapi.custominventories.inventories.SimpleCustomInventory;
import me.matamor.commonapi.custominventories.utils.CustomItem;
import me.matamor.commonapi.custominventories.utils.Size;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.commonapi.utils.StringUtils;
import me.matamor.commonapi.utils.map.Callback;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GuildMenuCommand extends ICommand<Guilds> {

    public GuildMenuCommand(Guilds plugin) {
        super(plugin, "menu", "edita la guild", new String[] { "m" });

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
            ifFalse(player.hasPermission("guilds.bypass"), "&cNo tienes permisos para abrir el menu de otra guild!");

            guild = getPlugin().getGuildManager().getGuild(commandArgs.getString(0));
            notNull(guild, "&cLa guild &4%s&c no existe!", commandArgs.getString(0));
        }

        openMenu(player, guild);
    }

    public void openMenu(Player player, Guild guild) {
        CustomInventory customInventory = new SimpleCustomInventory(Size.FIVE_LINE, "&8Menu de " + guild.getName());

        InventoryIcon groupsIcon = new NormalInventoryIcon(CustomItem.builder(Material.BOOK).setName("&2Rangos").setLore("&fEdita los rangos!").build()).addClickAction((ClickAction) clicker ->
                openGroups(player, guild));

        customInventory.setIcon(11, groupsIcon);

        InventoryIcon settingsIcon = new NormalInventoryIcon(CustomItem.builder(Material.MAP).setName("&8Opciones").setLore("&fEdita las opciones!").build()).addClickAction((ClickAction) clicker ->
                openSettings(player, guild));

        customInventory.setIcon(15, settingsIcon);

        customInventory.setIcon(31, new NormalInventoryIcon(CustomItem.builder(Material.FLINT_AND_STEEL).setName("&cCerrar!").setLore("&fPulsa para cerrar el menu!").build()).addClickAction((ClickAction) clicker ->
                player.closeInventory()));

        customInventory.openInventory(player);
    }

    public void openGroups(Player player, Guild guild) {
        CustomInventory customInventory = new SimpleCustomInventory(Size.FIVE_LINE, "&8Rangos de " + guild.getName());

        InventoryIcon defaultIcon = new NormalInventoryIcon(CustomItem.builder(Material.IRON_INGOT).setName(GuildRank.DEFAULT.getDisplayName()).setLore("&fPulsa para abrir el menu del rango predeterminado!").build()).addClickAction((ClickAction) clicker ->
                openGroupsMenu(player, guild, GuildRank.DEFAULT));

        customInventory.setIcon(11, defaultIcon);

        InventoryIcon memberIcon = new NormalInventoryIcon(CustomItem.builder(Material.GOLD_INGOT).setName(GuildRank.MEMBER.getDisplayName()).setLore("&fPulsa para abrir el menu del rango miembros!").build()).addClickAction((ClickAction) clicker ->
                openGroupsMenu(player, guild, GuildRank.MEMBER));

        customInventory.setIcon(13, memberIcon);

        InventoryIcon officialIcon = new NormalInventoryIcon(CustomItem.builder(Material.DIAMOND).setName(GuildRank.OFFICIAL.getDisplayName()).setLore("&fPulsa para abrir el menu del rango oficiales!").build()).addClickAction((ClickAction) clicker ->
                openGroupsMenu(player, guild, GuildRank.OFFICIAL));

        customInventory.setIcon(15, officialIcon);

        customInventory.setIcon(31, new NormalInventoryIcon(CustomItem.builder(Material.FLINT_AND_STEEL).setName("&cVolver!").setLore("&fPulsa para volver al menu anterior!").build()).addClickAction((ClickAction) clicker ->
                openMenu(player, guild)));

        customInventory.openInventory(player);
    }

    @SuppressWarnings("unchecked")
    public void openSettings(Player player, Guild guild) {
        CustomInventory customInventory = new SimpleCustomInventory(Size.THREE_LINE, "&8Menu de " + guild.getName());

        int count = 0;

        for (Flag flag : DefaultFlags.getFlags(FlagType.SETTING)) {
            Object value = guild.getSetting(flag);

            //Create the flag icon for the inventory!
            NormalInventoryIcon inventoryIcon = new NormalInventoryIcon(flag.getIcon(value));

            //Add the click action to update the flag value!
            inventoryIcon.addClickAction((ClickAction) clicker -> {
                //Create the callback for the flag edit, it will save the value to the database!
                Callback<Boolean> callback = (newValue, e) -> {
                    //Save the Setting async!
                    getPlugin().getDatabase().saveSettingAsync(guild.getId(), flag, newValue, (result, exception) -> {
                        //Go back the main thread to finish the flag updating!
                        getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                            if (result) {
                                player.sendMessage(StringUtils.color("&aSe ha guardado el nuevo valor de la flag!"));
                            } else {
                                player.sendMessage(StringUtils.color("&cNo se ha podido guardar el nuevo valor de la flag!"));
                            }

                            //Open the menu with the updated values!
                            openSettings(player, guild);
                        });
                    });
                };

                //Open flag edit with the callback we just created!
                flag.getFlagEditor().newFlag(getPlugin(), player, guild, flag, callback);
            });
            
            //Set the icon in the inventory
            customInventory.setIcon(count++, inventoryIcon);
        }

        customInventory.setIcon(22, new NormalInventoryIcon(CustomItem.builder(Material.FLINT_AND_STEEL).setName("&cVolver!").setLore("&fPulsa para volver al menu anterior!").build()).addClickAction((ClickAction) clicker ->
                openMenu(player, guild)));

        customInventory.openInventory(player);
    }

    public void openGroupsMenu(Player player, Guild guild, GuildRank guildRank) {
        CustomInventory customInventory = new SimpleCustomInventory(Size.FIVE_LINE, "&8Menu del rango " + guildRank.getDisplayName());

        InventoryIcon membersIcon = new NormalInventoryIcon(CustomItem.builder(Material.PLAYER_HEAD).setName("&dMiembros").setLore("&fAdministra los miembros con este rango!").build()).addClickAction((ClickAction) clicker ->
                openMembers(player, guild, guildRank));

        customInventory.setIcon(11, membersIcon);

        InventoryIcon flagsIcon = new NormalInventoryIcon(CustomItem.builder(Material.SIGN).setName("&eFlags").setLore("&fAdministra las flags del rango!").build()).addClickAction((ClickAction) clicker ->
                openFlags(player, guild, guildRank));

        customInventory.setIcon(15, flagsIcon);

        customInventory.setIcon(31, new NormalInventoryIcon(CustomItem.builder(Material.FLINT_AND_STEEL).setName("&cVolver!").setLore("&fPulsa para volver al menu anterior!").build()).addClickAction((ClickAction) clicker ->
                openGroups(player, guild)));

        customInventory.openInventory(player);
    }

    public void openMembers(Player player, Guild guild, GuildRank guildRank) {
        if (guildRank == GuildRank.DEFAULT) {
            player.sendMessage(StringUtils.color("&cEl rango " + guildRank.getDisplayName() + " &cno tiene miembros!"));
        } else {
            CustomInventory customInventory = new SimpleCustomInventory(Size.FIVE_LINE, "&7Miembros del rango " + guildRank.getDisplayName());

            int count = 0;

            List<Map.Entry<Integer, GuildRank>> members = guild.getMembers(guildRank);

            for (Map.Entry<Integer, GuildRank> entry : members) {
                Identifier identifier = CommonAPI.getInstance().getIdentifierManager().getIdentifier(entry.getKey());

                CustomItem customItem = new CustomItem(Material.PLAYER_HEAD);
                if (identifier == null) {
                    //No identifier so set it as loading
                    customItem.setName("&aCargando nombre - &2" + entry.getKey());

                    //Load the Identifier and set the name!
                    getPlugin().getDatabase().runAsync(() -> {
                        Identifier result = CommonAPI.getInstance().getIdentifierManager().load(entry.getKey());
                        if (result != null) {
                            //Identifier successfully loaded, switch to main thread and update it!
                            getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                                //Update name!
                                customItem.setName("&a" + result.getName());
                                customItem.setSkullOwner(result.getName(), result.getUUID());

                                customInventory.update(player);
                            });
                        }
                    });
                } else {
                    //Identifier loaded so just set it!
                    customItem.setName("&a" + identifier.getName());
                    customItem.setSkullOwner(identifier.getName(), identifier.getUUID());
                }

                customItem.setLore("&fPulsa para abrir las opciones del jugador!");

                customInventory.setIcon(count++, new NormalInventoryIcon(customItem).addClickAction((ClickAction) clicker ->
                        openMemberOptions(player, guild, guildRank, entry.getKey())));
            }

            //Add member option

            InventoryIcon addMemberIcon = new NormalInventoryIcon(CustomItem.builder(Material.NETHER_STAR).setName("&aAñadir miembro").setLore("&fAñade un miembro a este rango!").build()).addClickAction((ClickAction) clicker -> {
                if (guild.getMembers().size() >= 18) {
                    player.sendMessage(StringUtils.color("&cEste rango ya ha llegado al limite de 18 miembros!"));
                } else {
                    openAddMember(player, guild, guildRank);
                }
            });

            customInventory.setIcon(29, addMemberIcon);

            //Remove all members option

            InventoryIcon removeAllMembersIcon = new NormalInventoryIcon(CustomItem.builder(Material.BARRIER).setName("&aQuitar miembros").setLore("&fElimina todos los miembros de este rango!").build()).addClickAction((ClickAction) clicker -> 
                    openDeleteAllMembers(player, guild, guildRank));

            customInventory.setIcon(33, removeAllMembersIcon);

            //Return to menu option

            customInventory.setIcon(31, new NormalInventoryIcon(CustomItem.builder(Material.FLINT_AND_STEEL).setName("&cVolver!").setLore("&fPulsa para volver al menu anterior!").build()).addClickAction((ClickAction) clicker ->
                    openGroupsMenu(player, guild, guildRank)));

            customInventory.openInventory(player);
        }
    }

    public void openAddMember(Player player, Guild guild, GuildRank guildRank) {
        Collection<GuildPlayer> guildPlayers = getPlugin().getGuildManager().getPlayers().stream()
                .filter(e -> !e.hasGuild())
                .filter(e -> e.getIdentifier().getPlayer() != null)
                .collect(Collectors.toList());

        if (guildPlayers.isEmpty()) {
            player.sendMessage(StringUtils.color("&cNo hay jugadores online que se puedan añadir!"));
        } else {
            Size size = Size.fit(guildPlayers.size());
            int maxSize;

            if (size == Size.SIX_LINE) {
                maxSize = Size.FOUR_LINE.getSize();
            } else {
                maxSize = size.getSize();
            }

            CustomInventory customInventory = new SimpleCustomInventory(size, "&8Selecciona el jugador a añadir!");

            int count = 0;

            for (GuildPlayer guildPlayer : guildPlayers) {
                if (count == maxSize) break;

                Identifier identifier = guildPlayer.getIdentifier();

                CustomItem customItem = CustomItem.builder(Material.PLAYER_HEAD)
                        .setName("&a" + identifier.getName())
                        .setSkullOwner(identifier.getName(), identifier.getUUID()).build();

                if (guild.getInvitations().contains(identifier.getId())) {
                    customItem.setLore("&fPulsa para cancelar la invitacion a la guild!");
                } else {
                    customItem.setLore("&fPulsa para invitar el jugador a la guild!");
                }

                customInventory.setIcon(count++, new NormalInventoryIcon(customItem).addClickAction((ClickAction) clicker -> {
                    //Check if the player is already a member!
                    if (guildPlayer.hasGuild()) {
                        player.sendMessage(StringUtils.color("&cEste player ya pertenece a una guild!"));
                    } else if (guild.getMemberRank(identifier.getId()).isHigherEqual(GuildRank.MEMBER)) {
                        player.sendMessage(StringUtils.color("&cEste jugador ya es miembro de la guild!"));
                    } else {
                        //If the player is already invited cancel the invitation!
                        if (guild.getInvitations().contains(identifier.getId())) {
                            guild.getInvitations().remove(identifier.getId());

                            player.sendMessage(StringUtils.color("&cHas cancelado la invitacion a la guild!"));
                        } else {
                            guild.getInvitations().add(identifier.getId());

                            player.sendMessage(StringUtils.color("&aHas invitado el jugador a la guild!"));

                            Player target = identifier.getPlayer();
                            if (target != null) {
                                target.sendMessage(StringUtils.color("&aHas sido invitado por &2" + player.getName() + "&a a la guild &2" + guild.getName() + "&a!"));
                            }
                        }

                        openMembers(player, guild, guildRank);
                    }
                }));
            }

            customInventory.setIcon(maxSize + 5, new NormalInventoryIcon(CustomItem.builder(Material.FLINT_AND_STEEL).setName("&cVolver!").setLore("&fPulsa para volver al menu anterior!").build()).addClickAction((ClickAction) clicker ->
                    openMembers(player, guild, guildRank)));

            customInventory.openInventory(player);
        }
    }

    public void openDeleteAllMembers(Player player, Guild guild, GuildRank guildRank) {
        List<Map.Entry<Integer, GuildRank>> members = guild.getMembers(guildRank);

        if (members.isEmpty()) {
            player.sendMessage(StringUtils.color("&cEste rango no tiene ningun miembro!"));
        } else {
            CustomInventory customInventory = new SimpleCustomInventory(Size.THREE_LINE, "&8Confirmacion");

            customInventory.setIcon(11, new NormalInventoryIcon(CustomItem.builder(Material.LIME_WOOL).setName("&aConfirmar").setLore("&fPulsa para borrar todos los miembros de este rango!").build()).addClickAction((ClickAction) clicker -> {
                player.sendMessage(StringUtils.color("&2Borrando miembros..."));

                getPlugin().getDatabase().deleteMembersAsync(guild.getId(), guildRank.name(), (result, exception) -> getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                    if (result) {
                        player.sendMessage(StringUtils.color("&aSe han borrado todos los miembros de este rango correctamente!"));

                        guild.removeMembers(guildRank);
                    } else {
                        player.sendMessage(StringUtils.color("&cNo se han podido borrar todos los miembros!"));
                    }

                    openMembers(player, guild, guildRank);
                }));
            }));

            customInventory.setIcon(15, new NormalInventoryIcon(CustomItem.builder(Material.RED_WOOL).setName("&cCancelar").setLore("&fPulsa para cancelar!").build()).addClickAction((ClickAction) clicker ->
                    openMembers(player, guild, guildRank)));

            customInventory.openInventory(player);
        }
    }

    public void openMemberOptions(Player player, Guild guild, GuildRank guildRank, int memberId) {
        Identifier identifier = CommonAPI.getInstance().getIdentifierManager().getIdentifier(memberId);
        if (identifier == null) {
            player.sendMessage(StringUtils.color("&cNo se ha podido cargar la informacion del jugador!"));
        } else {
            CustomInventory customInventory = new SimpleCustomInventory(Size.FOUR_LINE, "&8Opciones del jugador " + identifier.getName());

            //Delete member option

            InventoryIcon deleteMemberIcon = new NormalInventoryIcon(CustomItem.builder(Material.BARRIER).setName("&cBorrar miembro").setLore("&fPulsa para borrar el miembro del rango!").build()).addClickAction((ClickAction) clicker ->
                    openDeleteMember(player, guild, guildRank, memberId));

            customInventory.setIcon(11, deleteMemberIcon);

            //Change group option

            InventoryIcon changeGroupIcon = new NormalInventoryIcon(CustomItem.builder(Material.NAME_TAG).setName("&eCambiar grupo").setLore("&fPulsa para cambiar el rango de este miembro!").build()).addClickAction((ClickAction) clicker1 ->
                    openSelectChangeGroup(player, guild, guildRank, memberId));

            customInventory.setIcon(15, changeGroupIcon);

            //Return to members option

            customInventory.setIcon(31, new NormalInventoryIcon(CustomItem.builder(Material.FLINT_AND_STEEL).setName("&cVolver!").setLore("&fPulsa para volver al menu anterior!").build()).addClickAction((ClickAction) clicker ->
                    openMembers(player, guild, guildRank)));

            customInventory.openInventory(player);
        }
    }

    public void openDeleteMember(Player player, Guild guild, GuildRank guildRank, int memberId) {
        CustomInventory customInventory = new SimpleCustomInventory(Size.THREE_LINE, "&8Confirmacion");

        customInventory.setIcon(11, new NormalInventoryIcon(CustomItem.builder(Material.LIME_WOOL).setName("&aConfirmar").setLore("&fPulsa para borrar el miembro del rango!").build()).addClickAction((ClickAction) clicker -> {
            player.sendMessage(StringUtils.color("&2Borrando miembro..."));

            getPlugin().getDatabase().deleteMemberAsync(guild.getId(), memberId, (result, exception) -> getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                if (result) {
                    player.sendMessage(StringUtils.color("&aSe ha borrado el miembro del grupo correctamente!"));

                    guild.getMembers().remove(memberId);
                } else {
                    player.sendMessage(StringUtils.color("&cNo se ha podido borrar el miembro del grupo!"));
                }

                openMembers(player, guild, guildRank);
            }));
        }));

        customInventory.setIcon(15, new NormalInventoryIcon(CustomItem.builder(Material.RED_WOOL).setName("&cCancelar").setLore("&fPulsa para cancelar!").build()).addClickAction((ClickAction) clicker ->
                openMemberOptions(player, guild, guildRank, memberId)));

        customInventory.openInventory(player);
    }

    public void openSelectChangeGroup(Player player, Guild guild, GuildRank guildRank, int memberId) {
        CustomInventory customInventory = new SimpleCustomInventory(Size.FOUR_LINE, "&8Selecciona el nuevo grupo");

        //Default option

        InventoryIcon defaultIcon = new NormalInventoryIcon(CustomItem.builder(Material.IRON_INGOT).setName(GuildRank.DEFAULT.getDisplayName()).setLore("&fCambiar rango a predeterminado!").build()).addClickAction((ClickAction) clicker ->
                player.sendMessage(StringUtils.color("&cEl grupo " + GuildRank.DEFAULT.getDisplayName() + " &cno tiene miembros!")));

        customInventory.setIcon(11, defaultIcon);

        //Member option

        InventoryIcon memberIcon = new NormalInventoryIcon(CustomItem.builder(Material.GOLD_INGOT).setName(GuildRank.MEMBER.getDisplayName()).setLore("&fCambiar rango a miembro!").build()).addClickAction((ClickAction) clicker ->
                openChangeGroup(player, guild, guildRank, memberId, GuildRank.MEMBER));

        customInventory.setIcon(13, memberIcon);

        //Official option

        InventoryIcon officialIcon = new NormalInventoryIcon(CustomItem.builder(Material.DIAMOND).setName(GuildRank.OFFICIAL.getDisplayName()).setLore("&fCambiar rango a oficial!").build()).addClickAction((ClickAction) clicker ->
                openChangeGroup(player, guild, guildRank, memberId, GuildRank.OFFICIAL));

        customInventory.setIcon(15, officialIcon);

        //Return to members option

        customInventory.setIcon(31, new NormalInventoryIcon(CustomItem.builder(Material.FLINT_AND_STEEL).setName("&cVolver!").setLore("&fPulsa para volver al menu anterior!").build()).addClickAction((ClickAction) clicker ->
                openMembers(player, guild, guildRank)));

        customInventory.openInventory(player);
    }

    public void openChangeGroup(Player player, Guild guild, GuildRank guildRank, int memberId, GuildRank newguildRank) {
        if (guildRank == newguildRank) {
            player.sendMessage(StringUtils.color("&cEl jugador ya es rango " + guildRank.getDisplayName()));
        } else {
            CustomInventory customInventory = new SimpleCustomInventory(Size.THREE_LINE, "&8Confirmacion");

            customInventory.setIcon(11, new NormalInventoryIcon(CustomItem.builder(Material.LIME_WOOL).setName("&aConfirmar").setLore("&fPulsa cambiar el rango del miembro!").build()).addClickAction((ClickAction) clicker -> {
                player.sendMessage(StringUtils.color("&2Cambiando rango..."));

                getPlugin().getDatabase().saveMemberAsync(guild.getId(), memberId, newguildRank.name(), (result, exception) -> getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                    if (result) {
                        player.sendMessage(StringUtils.color("&aSe ha cambiado el rango del miembro correctamente!"));

                        guild.getMembers().put(memberId, newguildRank);
                    } else {
                        player.sendMessage(StringUtils.color("&cNo se ha podido cambiar el rango del miembro!"));
                    }

                    openMembers(player, guild, guildRank);
                }));
            }));

            customInventory.setIcon(15, new NormalInventoryIcon(CustomItem.builder(Material.RED_WOOL).setName("&cCancelar").setLore("&fPulsa para cancelar!").build()).addClickAction((ClickAction) clicker ->
                    openMemberOptions(player, guild, guildRank, memberId)));

            customInventory.openInventory(player);
        }
    }

    @SuppressWarnings("unchecked")
    public void openFlags(Player player, Guild guild, GuildRank guildRank) {
        CustomInventory customInventory = new SimpleCustomInventory(Size.FOUR_LINE, "&8Flags de rango " + guildRank.getDisplayName());

        int count = 0;

        for (Flag flag : DefaultFlags.getFlags(FlagType.PLAYER)) {
            Object value = guild.getFlag(guildRank, flag);

            //Create the flag icon for the inventory!
            NormalInventoryIcon inventoryIcon = new NormalInventoryIcon(flag.getIcon(value));

            //Add the click action to update the flag value!
            inventoryIcon.addClickAction((ClickAction) clicker -> {
                //Create the callback for the flag edit, it will save the value to the database!
                Callback<Boolean> callback = (newValue, e) -> {
                    //Save the Flag async!
                    getPlugin().getDatabase().saveFlagAsync(guild.getId(), guildRank.name(), flag, newValue, (result, exception) -> {
                        //Go back the main thread to finish the flag updating!
                        getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                            if (result) {
                                player.sendMessage(StringUtils.color("&aSe ha guardado el nuevo valor de la flag!"));
                            } else {
                                player.sendMessage(StringUtils.color("&cNo se ha podido guardar el nuevo valor de la flag!"));
                            }

                            //Open the menu with the updated values!
                            openFlags(player, guild, guildRank);
                        });
                    });
                };

                //Open flag edit with the callback we just created!
                flag.getFlagEditor().newFlag(getPlugin(), player, guild, flag, callback);
            });

            //Set the icon in the inventory
            customInventory.setIcon(count++, inventoryIcon);
        }

        customInventory.setIcon(31, new NormalInventoryIcon(CustomItem.builder(Material.FLINT_AND_STEEL).setName("&cVolver!").setLore("&fPulsa para volver al menu anterior!").build()).addClickAction((ClickAction) clicker ->
                openGroupsMenu(clicker, guild, guildRank)));

        customInventory.openInventory(player);
    }
}