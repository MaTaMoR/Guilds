package me.matamor.guilds.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.guilds.data.permission.group.GuildRank;

@AllArgsConstructor
public class SimpleGuildPlayer implements GuildPlayer {

    @Getter
    private final Identifier identifier;

    @Getter @Setter
    private Guild guild;

    private boolean chatEnabled;

    @Override
    public boolean hasGuild() {
        if (this.guild != null) {
            if (this.guild.getMemberRank(this.identifier.getId()).isLower(GuildRank.MEMBER)) {
                this.guild = null;
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    @Override
    public boolean hasChatEnabled() {
        return this.chatEnabled;
    }
}
