package me.matamor.guilds.data.permission.flags.editor.defaults;

import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.Guild;
import me.matamor.guilds.data.permission.flags.Flag;
import me.matamor.guilds.data.permission.flags.editor.FlagEditor;
import me.matamor.commonapi.custominventories.actions.ClickAction;
import me.matamor.commonapi.custominventories.icons.NormalInventoryIcon;
import me.matamor.commonapi.custominventories.inventories.CustomInventory;
import me.matamor.commonapi.custominventories.inventories.SimpleCustomInventory;
import me.matamor.commonapi.custominventories.utils.CustomItem;
import me.matamor.commonapi.custominventories.utils.Size;
import me.matamor.commonapi.utils.map.Callback;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BooleanFlagEditor implements FlagEditor<Boolean> {

    @Override
    public void newFlag(Guilds plugin, Player player, Guild guild, Flag<Boolean> flag, Callback<Boolean> returnCallback) {
        CustomInventory customInventory = new SimpleCustomInventory(Size.FOUR_LINE, "&8Editor " + flag.getDisplayName());

        customInventory.setIcon(11, new NormalInventoryIcon(CustomItem.builder(Material.LIME_WOOL).setName("&aActivar").setLore("&fPulsa para &aactivar&f el flag!").build()).addClickAction((ClickAction) player1 -> {
            returnCallback.done(true, null);
        }));

        customInventory.setIcon(15, new NormalInventoryIcon(CustomItem.builder(Material.RED_WOOL).setName("&cDesactivar").setLore("&fPulsa para &cdesactivar&f el flag!").build()).addClickAction((ClickAction) player1 -> {
            returnCallback.done(false, null);
        }));

        customInventory.setIcon(31, new NormalInventoryIcon(CustomItem.builder(Material.FLINT_AND_STEEL).setName("&cVolver!").setLore("&fPulsa para volver al menu anterior!").build()).addClickAction((ClickAction) player1 ->
                returnCallback.done(false, null)));

        customInventory.openInventory(player);
    }
}
