package me.matamor.guilds.data.permission.flags;

import me.matamor.guilds.data.permission.flags.defaults.BooleanFlag;
import me.matamor.commonapi.utils.Validate;
import org.bukkit.Material;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class DefaultFlags {

    private DefaultFlags() {

    }

    private static final Map<String, Flag> flags = new LinkedHashMap<>();

    private static <T> Flag<T> register(Flag<T> flag) {
        Validate.isFalse(flags.containsKey(flag.getName()), "The flag '" + flag.getName() + "' is already registered!");

        flags.put(flag.getName(), flag);

        return flag;
    }

    public static Flag getFlag(String name) {
        return flags.get(name);
    }

    public static Collection<Flag> getFlags() {
        return flags.values();
    }

    public static Collection<Flag> getFlags(FlagType flagType) {
        return flags.values().stream()
                .filter(e -> e.getFlagType() == flagType)
                .collect(Collectors.toList());
    }

    //Group flags

    public static final Flag<Boolean> ALLOW_BUILD = register(new BooleanFlag(FlagType.PLAYER,"allow_build", "&2Construccion", false,"&fPermite construir!", Material.GRASS_BLOCK));
    public static final Flag<Boolean> CHESTS = register(new BooleanFlag(FlagType.PLAYER,"chests", "&6Cofres", false,"&fPermite usar cofres!", Material.CHEST));
    public static final Flag<Boolean> FURNACES = register(new BooleanFlag(FlagType.PLAYER,"furnances", "&8Hornos", false,"&fPermite usar hornos!", Material.FURNACE));
    public static final Flag<Boolean> ANVIL = register(new BooleanFlag(FlagType.PLAYER,"anvils", "&7Anvils", false,"&fPermite usar anvils!", Material.ANVIL));
    public static final Flag<Boolean> DOORS = register(new BooleanFlag(FlagType.PLAYER,"doors", "&ePuertas", false,"&fPermite usar puertas!", Material.OAK_DOOR));
    public static final Flag<Boolean> ENCHANT_TABLE = register(new BooleanFlag(FlagType.PLAYER,"enchant_table", "&bMesa de encantamientos", false,"&fPermite usar mesas de encantamientos!", Material.ENCHANTING_TABLE));
    public static final Flag<Boolean> REDSTONE = register(new BooleanFlag(FlagType.PLAYER,"redstone", "&cRedstone", false,"&fPermite usar redstone!", Material.REDSTONE));
    public static final Flag<Boolean> INTERACT = register(new BooleanFlag(FlagType.PLAYER,"interact", "&aInteractuar", false,"&fPermite interactuar de forma general!", Material.ENCHANTING_TABLE));
    public static final Flag<Boolean> WATER_BUCKET = register(new BooleanFlag(FlagType.PLAYER,"water_bucket","&9Cubo de agua", false,"&fPermite llenar o vaciar un cubo de agua!", Material.WATER_BUCKET));
    public static final Flag<Boolean> LAVA_BUCKET = register(new BooleanFlag(FlagType.PLAYER,"lava_bucket","&cCubo de lava", false,"&fPermite llenar o vaciar un cubo de lava!", Material.LAVA_BUCKET));
    public static final Flag<Boolean> OTHER_BUCKET = register(new BooleanFlag(FlagType.PLAYER,"other_bucket","&7Otros cubos", false,"&fPermite llenar o vaciar otros tipos de cubos!", Material.PUFFERFISH_BUCKET));
    public static final Flag<Boolean> MONSTERS_PROTECTION = register(new BooleanFlag(FlagType.PLAYER,"monsters_protection","&eProteccion de Monstruos", false,"&fProtege a los monstruos!", Material.ZOMBIE_HEAD));
    public static final Flag<Boolean> ANIMALS_PROTECTION = register(new BooleanFlag(FlagType.PLAYER,"animals_protection","&eProtection de Animales", true,"&fProtege a los animales!", Material.HAY_BLOCK));
    public static final Flag<Boolean> LIGHTER_USE = register(new BooleanFlag(FlagType.PLAYER,"lighter_use","&cUso de mechero", false,"&fActiva o desactiva el uso del mechero!", Material.FLINT_AND_STEEL));

    //Setting flags

    public static final Flag<Boolean> PVP = register(new BooleanFlag(FlagType.SETTING,"pvp","&4PvP", false,"&fActiva o desactiva el PvP!", Material.DIAMOND_SWORD));
    public static final Flag<Boolean> WATER_FLOW = register(new BooleanFlag(FlagType.SETTING,"water_flow","&9Movimiento de agua", true,"&fActiva o desactiva el movimiento del agua!", Material.WATER_BUCKET));
    public static final Flag<Boolean> LAVA_FLOW = register(new BooleanFlag(FlagType.SETTING,"lava_flow","&cMovimiento de lava", false,"&fActiva o desactiva el movimiento de la lava!", Material.LAVA_BUCKET));
    public static final Flag<Boolean> FIRE_BURNING = register(new BooleanFlag(FlagType.SETTING,"fire_burn","&cDestruccion Fuego", false,"&fActiva o desactiva la destruccion por fuego!", Material.FLINT_AND_STEEL));

}
