package me.matamor.guilds.data.permission.flags;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class FlagData {

    @Getter
    private final Map<Flag, Object> entries = new LinkedHashMap<>();

    public FlagData set(Flag flag, Object value) {
        this.entries.put(flag, value);
        return this;
    }

    public boolean has(Flag flag) {
        return this.entries.containsKey(flag);
    }

    public <T> T get(Flag<T> flag) {
        Object object = this.entries.get(flag);
        return (object == null ? flag.getDefaultValue() : (T) object);
    }

    public <T> FlagStatus checkFlag(Flag<T> flag, T value) {
        return (Objects.equals(get(flag), value) ? FlagStatus.ALLOW : FlagStatus.DENY);
    }
}
