package com.bendb.thrifty.schema;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;

/**
 * Represents a type name and all containing namespaces.
 */
public abstract class ThriftType {
    private static final String LIST_PREFIX = "list<";
    private static final String SET_PREFIX = "set<";
    private static final String MAP_PREFIX = "map<";

    public static final ThriftType BOOL = new BuiltinType("bool");
    public static final ThriftType BYTE = new BuiltinType("byte");
    public static final ThriftType I16 = new BuiltinType("i16");
    public static final ThriftType I32 = new BuiltinType("i32");
    public static final ThriftType I64 = new BuiltinType("i64");
    public static final ThriftType DOUBLE = new BuiltinType("double");
    public static final ThriftType STRING = new BuiltinType("string");
    public static final ThriftType BINARY = new BuiltinType("binary");

    // Only valid as a "return type" for service methods
    public static final ThriftType VOID = new BuiltinType("void");

    private static final ImmutableMap<String, ThriftType> BUILTINS;
    static {
        ImmutableMap.Builder<String, ThriftType> builtins = ImmutableMap.builder();
        builtins.put(BOOL.name, BOOL);
        builtins.put(BYTE.name, BYTE);
        builtins.put(I16.name, I16);
        builtins.put(I32.name, I32);
        builtins.put(I64.name, I64);
        builtins.put(DOUBLE.name, DOUBLE);
        builtins.put(STRING.name, STRING);
        builtins.put(BINARY.name, BINARY);
        BUILTINS = builtins.build();
    }

    private final String name;

    protected ThriftType(String name) {
        this.name = name;
    }

    public static ThriftType list(ThriftType elementType) {
        String name = LIST_PREFIX + elementType.name + ">";
        return new ListType(name, elementType);
    }

    public static ThriftType set(ThriftType elementType) {
        String name = SET_PREFIX + elementType.name + ">";
        return new SetType(name, elementType);
    }

    public static ThriftType map(ThriftType keyType, ThriftType valueType) {
        String name = MAP_PREFIX + keyType.name + "," + valueType.name + ">";
        return new MapType(name, keyType, valueType);
    }

    /**
     * Gets a {@link ThriftType} for the given type name.
     *
     * Preconditions:
     * The given name is non-null, non-empty, and is the product
     * of ThriftParser.  In particular, it is assumed that collection
     * types are already validated.
     *
     * @param name
     * @return
     */
    public static ThriftType get(@Nonnull String name) {
        ThriftType t = BUILTINS.get(name);
        if (t != null) {
            return t;
        }

        return new UserType(name);
    }

    public static ThriftType enumType(@Nonnull String name) {
        return new UserType(name, true);
    }

    public static ThriftType typedefOf(ThriftType oldType, String name) {
        if (BUILTINS.get(name) != null) {
            throw new IllegalArgumentException("Cannot typedef built-in type: " + name);
        }
        return new TypedefType(name, oldType);
    }

    public String name() {
        return this.name;
    }

    public boolean isBuiltin() {
        return false;
    }

    public boolean isTypedef() {
        return false;
    }

    public boolean isList() {
        return getTrueType() instanceof ListType;
    }

    public boolean isSet() {
        return getTrueType() instanceof SetType;
    }

    public boolean isMap() {
        return getTrueType() instanceof MapType;
    }

    public boolean isEnum() {
        return false;
    }

    public ThriftType getTrueType() {
        ThriftType t = this;
        while (t instanceof TypedefType) {
            t = ((TypedefType) t).originalType;
        }
        return t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThriftType that = (ThriftType) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static class BuiltinType extends ThriftType {
        BuiltinType(String name) {
            super(name);
        }

        @Override
        public boolean isBuiltin() {
            return true;
        }
    }

    public static class UserType extends ThriftType {
        private final boolean isEnum;

        UserType(String name) {
            this(name, false);
        }

        UserType(String name, boolean isEnum) {
            super(name);
            this.isEnum = isEnum;
        }

        @Override
        public boolean isEnum() {
            return isEnum;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o) && isEnum == ((UserType) o).isEnum;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), isEnum);
        }
    }

    public static class ListType extends ThriftType {
        private final ThriftType elementType;

        ListType(String name, ThriftType elementType) {
            super(name);
            this.elementType = elementType;
        }

        public ThriftType elementType() {
            return elementType;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o)
                    && elementType.equals(((ListType) o).elementType);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), elementType.hashCode());
        }
    }

    public static class SetType extends ThriftType {
        private final ThriftType elementType;

        SetType(String name, ThriftType elementType) {
            super(name);
            this.elementType = elementType;
        }

        public ThriftType elementType() {
            return elementType;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o)
                    && elementType.equals(((SetType) o).elementType);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), elementType.hashCode());
        }
    }

    public static class MapType extends ThriftType {
        private final ThriftType keyType;
        private final ThriftType valueType;

        MapType(String name, ThriftType keyType, ThriftType valueType) {
            super(name);
            this.keyType = keyType;
            this.valueType = valueType;
        }

        public ThriftType keyType() {
            return keyType;
        }

        public ThriftType valueType() {
            return valueType;
        }

        @Override
        public boolean equals(Object o) {
            if (super.equals(o)) {
                MapType that = (MapType) o;
                return this.keyType.equals(that.keyType)
                        && this.valueType.equals(that.valueType);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(
                    super.hashCode(),
                    keyType.hashCode(),
                    valueType.hashCode());
        }
    }

    public static class TypedefType extends ThriftType {
        private final ThriftType originalType;

        TypedefType(String name, ThriftType originalType) {
            super(name);
            this.originalType = originalType;
        }

        @Override
        public boolean isTypedef() {
            return true;
        }

        public ThriftType originalType() {
            return originalType;
        }

        @Override
        public boolean equals(Object o) {
            if (!super.equals(o)) {
                return false;
            }
            TypedefType that = (TypedefType) o;

            return originalType.equals(that.originalType);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + originalType.hashCode();
            return result;
        }
    }
}
