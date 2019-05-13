package me.matamor.guilds.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SimpleChunkEntry implements ChunkEntry {

    @Getter
    private final Guild guild;

    @Getter
    private final String worldName;

    @Getter
    private final int x;

    @Getter
    private final int z;

}
