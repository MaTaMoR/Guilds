package me.matamor.guilds.data.permission.flags;

import me.matamor.guilds.data.permission.flags.editor.FlagEditor;
import me.matamor.commonapi.custominventories.utils.CustomItem;

public interface Flag<T> {

    //Flag type used for building inventory and on engine

    FlagType getFlagType();

    //Name of the Flag

    String getName();

    //Default value of the Flag

    T getDefaultValue();

    //Display name of the flag, used to be displayed to players!

    String getDisplayName();

    //Description used on the lore

    String getDescription();

    //Icon to show Flag on the inventory

    CustomItem getIcon();

    //Icon to show Flag on the inventory with value

    CustomItem getIcon(T value);

    //Serialize Flag value to save it on the database

    String serialize(T value);

    //Deserialize Flag value to load it from the database

    T deserialize(String value);

    //Flag editor to change value
    FlagEditor<T> getFlagEditor();

}
