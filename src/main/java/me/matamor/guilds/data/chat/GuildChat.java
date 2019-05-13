package me.matamor.guilds.data.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.matamor.commonapi.utils.Validate;
import me.matamor.guilds.data.Guild;
import me.matamor.commonapi.CommonAPI;
import me.matamor.commonapi.storage.identifier.Identifier;
import me.matamor.commonapi.utils.StringUtils;
import org.bukkit.entity.Player;

import java.util.*;

@RequiredArgsConstructor
public class GuildChat {

    @Getter
    private final Guild guild;

    @Getter
    private final Set<Integer> speakers = new HashSet<>();

    public void sendChatMessage(String message) {
        Validate.notNull(message, "Message can't be null!");

        //Give color to the Message
        message = StringUtils.color(message);

        Set<Integer> members = this.guild.getMembers().keySet();

        //Send the message to all the online members!
        for (Integer memberId : members) {
            Identifier memberIdentifier = CommonAPI.getInstance().getIdentifierManager().getIdentifier(memberId);
            if (memberIdentifier == null) continue;

            Player member = memberIdentifier.getPlayer();
            if (member == null) continue;

            member.sendMessage(message);
        }
    }

    public void speakOnChat(String message, Player speaker) {
        sendChatMessage("&2[&a" + this.guild.getName() + "&2] " + speaker.getName() + ": &f" + message);
    }
}
