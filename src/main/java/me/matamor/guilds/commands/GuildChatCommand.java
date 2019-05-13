package me.matamor.guilds.commands;

import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.GuildPlayer;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import org.bukkit.entity.Player;

public class GuildChatCommand extends ICommand<Guilds> {

    public GuildChatCommand(Guilds plugin) {
        super(plugin, "chat", "activa el chat de gremio", new String[] { });

        setOnlyInGame(true);
    }

    @Override
    public void onCommand(CommandArgs commandArgs) throws ICommandException {
        Player player = commandArgs.getPlayer();

        GuildPlayer guildPlayer = getPlugin().getGuildManager().getPlayer(player.getUniqueId());
        notNull(guildPlayer, "&cNo se ha podido cargar tu información, reconectate y prueba de nuevo!");

        ifFalse(guildPlayer.hasGuild(), "&cNo perteneces a una guild!");

        guildPlayer.setChatEnabled(!guildPlayer.hasChatEnabled());

        commandArgs.sendMessage("&aGuild chat: " + (guildPlayer.hasChatEnabled() ? "&aactivado" : "&cdesactivado"));
    }
}
