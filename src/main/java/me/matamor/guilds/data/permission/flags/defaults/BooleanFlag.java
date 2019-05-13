package me.matamor.guilds.data.permission.flags.defaults;

import me.matamor.guilds.data.permission.flags.FlagType;
import me.matamor.guilds.data.permission.flags.SimpleFlag;
import me.matamor.guilds.data.permission.flags.editor.defaults.BooleanFlagEditor;
import org.bukkit.Material;

public class BooleanFlag extends SimpleFlag<Boolean> {

    public BooleanFlag(FlagType flagType, String name, String displayName, boolean defaultValue, String description, Material material) {
        super(flagType, name, displayName, defaultValue, description, material, new BooleanFlagEditor());
    }

    @Override
    public String serialize(Boolean value) {
        return value.toString();
    }

    @Override
    public Boolean deserialize(String value) {
        return value.equalsIgnoreCase(Boolean.TRUE.toString());
    }

    @Override
    protected String toString(Boolean value) {
        return (value ? "&aactivada" : "&cdesactivada");
    }
}
