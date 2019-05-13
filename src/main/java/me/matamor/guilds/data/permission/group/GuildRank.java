package me.matamor.guilds.data.permission.group;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum GuildRank {

    DEFAULT(0, "Predeterminado", "&7Predeterminado"),
    MEMBER(1, "Miembro", "&aMiembro"),
    OFFICIAL(2, "Oficial", "&bOficial"),
    OWNER(3, "Dueño", "&4Dueño");

    @Getter
    private final int position;

    @Getter
    private final String name;

    @Getter
    private final String displayName;

    public boolean is(GuildRank... groupTypes) {
        for (GuildRank groupType : groupTypes) {
            if (this == groupType) {
                return true;
            }
        }

        return false;
    }

    public boolean isHigher(GuildRank guildRank) {
        return guildRank != null && this.position > guildRank.position;
    }

    public boolean isHigherEqual(GuildRank guildRank) {
        return guildRank != null && this.position >= guildRank.position;
    }

    public boolean isEqual(GuildRank guildRank) {
        return guildRank != null && this.position == guildRank.position;
    }

    public boolean isLower(GuildRank guildRank) {
        return guildRank != null && this.position < guildRank.position;
    }

    public boolean isLowerEqual(GuildRank guildRank) {
        return guildRank != null && this.position <= guildRank.position;
    }

    public static GuildRank matchType(String name) {
        for (GuildRank guildRank : values()) {
            if (guildRank.name().equalsIgnoreCase(name) || guildRank.getName().equalsIgnoreCase(name)) {
                return guildRank;
            }
        }

        return DEFAULT;
    }
}
