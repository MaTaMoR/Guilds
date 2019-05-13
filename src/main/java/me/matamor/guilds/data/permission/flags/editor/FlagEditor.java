package me.matamor.guilds.data.permission.flags.editor;

import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.permission.flags.Flag;
import me.matamor.commonapi.utils.map.Callback;
import org.bukkit.entity.Player;

public interface FlagEditor<T> {

    void newFlag(Guilds plugin, Player player, Guild guild, Flag<T> flag, Callback<Boolean> returnCallback);

}
