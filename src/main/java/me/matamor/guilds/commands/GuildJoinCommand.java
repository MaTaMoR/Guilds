package me.matamor.guilds.commands;

import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.commonapi.utils.StringUtils;
import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.GuildPlayer;
import me.matamor.guilds.data.permission.group.GuildRank;
import org.bukkit.entity.Player;

public class GuildJoinCommand extends ICommand<Guilds> {

    public GuildJoinCommand(Guilds plugin) {
        super(plugin, "unirse", "unete a un gremio", new String[] { "join", "j" });

        setOnlyInGame(true);
    }

    @Override
    public String getArguments() {
        return "<nombre>";
    }

    @Override
    public void onCommand(CommandArgs commandArgs) throws ICommandException {
        ifTrue(commandArgs.length == 0, "&cUso incorrecto: &4" + commandArgs.getSelfUsage(this));

        Player player = commandArgs.getPlayer();

        GuildPlayer guildPlayer = getPlugin().getGuildManager().getPlayer(player.getUniqueId());
        notNull(guildPlayer, "&cNo se ha podido cargar tu informacion!");

        //Check if player is already in a guild!
        ifTrue(guildPlayer.hasGuild(), "&cYa perteneces a un gremio!");

        Guild guild = getPlugin().getGuildManager().getGuild(commandArgs.getString(0));
        notNull(guild, "&cEl gremio &4%s&c no existe!", commandArgs.getString(0));

        Identifier identifier = guildPlayer.getIdentifier();

        if (guild.getInvitations().contains(identifier.getId())) {
            commandArgs.sendMessage("&2Uniendote al gremio...");

            //Save the player to the database!
            getPlugin().getDatabase().saveMemberAsync(guild.getId(), identifier.getId(), GuildRank.MEMBER.name(), (result, exception) -> getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                if (result) {
                    //Notify the player and guild!
                    commandArgs.sendMessage("&aTe has unido al gremio &2%s&a!", guild.getName());
                    guild.getChat().sendChatMessage("&aEl jugador &2" + player.getName() + "&a se ha unido al gremio!");

                    //Add the Player to the Guild and set the Guild to the Player!
                    guildPlayer.setGuild(guild);
                    guild.getMembers().put(identifier.getId(), GuildRank.MEMBER);
                    guild.getInvitations().remove(identifier.getId());
                } else {
                    player.sendMessage(StringUtils.color("&cNo se ha podido guardar el nuevo miembro en la database!"));
                }
            }));
        }
    }
}
