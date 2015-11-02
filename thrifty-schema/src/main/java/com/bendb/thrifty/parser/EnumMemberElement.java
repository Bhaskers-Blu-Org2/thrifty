package com.bendb.thrifty.parser;

import com.bendb.thrifty.Location;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
abstract class EnumMemberElement {
    public abstract Location location();
    public abstract String documentation();
    public abstract String name();
    @Nullable public abstract Integer value();

    public static Builder builder(Location location) {
        return new AutoValue_EnumMemberElement.Builder()
                .location(location)
                .documentation("");
    }

    @AutoValue.Builder
    public interface Builder {
        Builder location(Location location);
        Builder documentation(String documentation);
        Builder name(String name);
        Builder value(Integer value);

        EnumMemberElement build();
    }
}
