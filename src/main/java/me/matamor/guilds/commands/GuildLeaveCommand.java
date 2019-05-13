package me.matamor.guilds.commands;

import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.commonapi.utils.StringUtils;
import me.matamor.guilds.Guilds;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.GuildPlayer;
import me.matamor.guilds.data.permission.group.GuildRank;
import org.bukkit.entity.Player;

public class GuildLeaveCommand extends ICommand<Guilds> {

    public GuildLeaveCommand(Guilds plugin) {
        super(plugin, "abandonar", "abandona tu gremio", new String[] { "leave", "l" });

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
            notNull(guild, "&cNo perteneces a ningun gremio!");
        } else { //Arguments provided, player is trying to claim for another guild!
            guild = getPlugin().getGuildManager().getGuild(commandArgs.getString(0));
            notNull(guild, "&cEl gremio &4%s&c no existe!", commandArgs.getString(0));

            ifTrue(guild.getMemberRank(identifier.getId()).isLower(GuildRank.OWNER) && !player.hasPermission("guilds.bypass"), "&cNo tienes permisos para disolver otra gremio!");
        }

        if (guild.getMemberRank(identifier.getId()).isHigherEqual(GuildRank.OWNER)) {
            commandArgs.sendMessage("&cEres el lider este tu guild, usa el comando &4/gremios disolver&c!");
        } else {
            commandArgs.sendMessage("&2Abandonando guild...");

            getPlugin().getDatabase().deleteMemberAsync(guild.getId(), identifier.getId(), (result, exception) -> getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                if (result) {
                    guild.getMembers().remove(identifier.getId());

                    GuildPlayer guildPlayer = getPlugin().getGuildManager().getPlayer(player.getUniqueId());
                    if (guildPlayer != null) {
                        Guild newGuild = getPlugin().getGuildManager().getGuild(identifier.getId(), GuildRank.MEMBER);

                        //Update the data!
                        guildPlayer.setGuild(newGuild);
                    }

                    //Send notifications
                    commandArgs.sendMessage("&aHas abandonado la guild &4%s&c!", guild.getName());
                    guild.getChat().sendChatMessage("&cEl jugador &4" + player.getName() + "&c ha abandonado la guild!");
                } else {
                    player.sendMessage(StringUtils.color("&cNo has podido abandonar tu guild!"));
                }
            }));
        }
    }
}
