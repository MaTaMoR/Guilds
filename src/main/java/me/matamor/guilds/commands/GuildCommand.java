package me.matamor.guilds.commands;

import me.matamor.guilds.Guilds;
import me.matamor.commonapi.commands.CommandArgs;
import me.matamor.commonapi.commands.ICommand;
import me.matamor.commonapi.commands.ICommandException;
import me.matamor.commonapi.commands.cmds.HelpCommand;

public class GuildCommand extends ICommand<Guilds> {

    public GuildCommand(Guilds plugin) {
        super(plugin, "Gremio", new String[] { "guild", "g" });

        setHelpCommand(new HelpCommand(this));

        addChild(new GuildCreateCommand(plugin));
        addChild(new GuildClaimCommand(plugin));
        addChild(new GuildUnclaimCommand(plugin));
        addChild(new GuildJoinCommand(plugin));
        addChild(new GuildLeaveCommand(plugin));
        addChild(new GuildListCommand(plugin));
        addChild(new GuildInfoCommand(plugin));
        addChild(new GuildChunkCommand(plugin));
        addChild(new GuildChatCommand(plugin));
        addChild(new GuildMenuCommand(plugin));
        addChild(new GuildDisbandCommand(plugin));
    }

    @Override
    public void onCommand(CommandArgs commandArgs) throws ICommandException {

    }
}
