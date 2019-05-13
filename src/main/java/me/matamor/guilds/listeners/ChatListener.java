package me.matamor.guilds.listeners;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.matamor.guilds.Guilds;
import me.matamor.guilds.data.GuildPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.logging.Level;

@RequiredArgsConstructor
public class ChatListener implements Listener {

    @Getter
    private final Guilds plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        GuildPlayer guildPlayer = this.plugin.getGuildManager().getPlayer(player.getUniqueId());
        if (guildPlayer != null) {
            String guildName = (guildPlayer.hasGuild() ? guildPlayer.getGuild().getName() : "");
            event.setFormat(event.getFormat().replace("{guild_name}", guildName));

            if (guildPlayer.hasGuild() && guildPlayer.hasChatEnabled()) {
                event.setCancelled(true);

                this.plugin.getLogger().log(Level.INFO, "[Guild Chat] " + player.getName() + ": " + event.getMessage());
                guildPlayer.getGuild().getChat().speakOnChat(event.getMessage(), player);
            }
        }
    }
}