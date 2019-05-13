package me.matamor.guilds.data;

import me.matamor.commonapi.storage.identifier.Identifier;

public interface GuildPlayer {

    Identifier getIdentifier();

    void setGuild(Guild guild);

    Guild getGuild();

    boolean hasGuild();

    void setChatEnabled(boolean chatEnabled);

    boolean hasChatEnabled();

}
