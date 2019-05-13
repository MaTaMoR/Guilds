package me.matamor.guilds.data.permission.flags;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.matamor.guilds.data.permission.flags.editor.FlagEditor;
import me.matamor.commonapi.custominventories.utils.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public abstract class SimpleFlag<T> implements Flag<T> {

    @Getter
    private final FlagType flagType;

    @Getter
    private final String name;

    @Getter
    private final String displayName;

    @Getter
    private final T defaultValue;

    @Getter
    private final String description;

    @Getter
    private final Material material;

    @Getter
    private final FlagEditor<T> flagEditor;

    protected String toString(T value) {
        return value.toString();
    }

    @Override
    public CustomItem getIcon() {
        CustomItem customItem = new CustomItem(this.material);
        customItem.setRemoveAttributes(true);

        customItem.setName(this.displayName);

        List<String> lore = new ArrayList<>();
        lore.add(this.description);
        lore.add("");
        lore.add("&fPulsa para editar!");

        customItem.setLore(lore);

        return customItem;
    }

    @Override
    public CustomItem getIcon(T value) {
        CustomItem customItem = new CustomItem(this.material);
        customItem.setRemoveAttributes(true);

        customItem.setName(this.displayName);

        List<String> lore = new ArrayList<>();
        lore.add(this.description);
        lore.add("");
        lore.add("&7Estado: &8" + toString(value));
        lore.add("");
        lore.add("&fPulsa para editar!");

        customItem.setLore(lore);

        return customItem;
    }
}
