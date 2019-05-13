package me.matamor.guilds.commands;

import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.guilds.Guilds;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.permission.group.GuildRank;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class GuildListCommand extends ICommand<Guilds> {

    public GuildListCommand(Guilds plugin) {
        super(plugin, "lista", "los gremios a los que perteneces", new String[] { });

        setOnlyInGame(true);
    }

    @Override
    public void onCommand(CommandArgs commandArgs) throws ICommandException {
        Player player = commandArgs.getPlayer();

        Identifier identifier = CommonAPI.getInstance().getIdentifierManager().getIdentifier(player.getUniqueId());
        notNull(identifier, "&cNo se ha podido cargar tu informacion!");

        List<Guild> guilds = getPlugin().getGuildManager().getGuilds().stream()
                .filter(e -> e.getMemberRank(identifier.getId()).isHigherEqual(GuildRank.MEMBER))
                .collect(Collectors.toList());

        if (guilds.isEmpty()) {
            commandArgs.sendMessage("&cNo perteneces a ningun gremio!");
        } else {
            for (Guild guild : guilds) {
                commandArgs.sendMessage("&7>  &f" + guild.getName() + " &7/ &e" + guild.getMemberRank(identifier.getId()));
            }
        }
    }
}
