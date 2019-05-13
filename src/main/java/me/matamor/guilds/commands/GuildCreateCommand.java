package me.matamor.guilds.commands;

import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.GuildPlayer;
import me.matamor.guilds.data.permission.group.GuildRank;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.commonapi.utils.StringUtils;
import org.bukkit.entity.Player;

public class GuildCreateCommand extends ICommand<Guilds> {

    public GuildCreateCommand(Guilds plugin) {
        super(plugin, "crear", "crea un gremio", new String[] { });

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
        notNull(guildPlayer, "&cNo se ha podido cargar tu información, reconectate y prueba de nuevo!");

        Identifier identifier = guildPlayer.getIdentifier();

        String groupName = StringUtils.replaceNonAlphanumeric(commandArgs.getString(0));
        ifTrue(groupName.length() < 3, "&cEl nombre debe tener al menos 3 caracteres!");
        ifTrue(groupName.length() > 16, "&cEl nombre debe tener menos de 16 caracteres!");

        Guild guild = getPlugin().getGuildManager().getGuild(groupName);
        ifFalse(guild == null, "&cYa existe un gremio con ese nombre!");

        guild = getPlugin().getGuildManager().getGuild(identifier.getId(), GuildRank.MEMBER);
        ifFalse(guild == null, "&cYa eres miembro de un gremio!");

        commandArgs.sendMessage("&2Creando la guild &2" + groupName);

        getPlugin().getDatabase().createChunkGroupAsync(groupName, identifier.getId(), (result, exception) -> {
            if (result == null) {
                commandArgs.sendMessage("&cNo se pudo crear el gremio, contacta con MaTaMoR_!");
            } else {
                getPlugin().getGuildManager().storeGuild(result);
                guildPlayer.setGuild(result);

                commandArgs.sendMessage("&aGremio creada correctamente!{new_line}&aAhora puedes añadir claims con el comando &2/gremio claim");
            }
        });
    }
}
