package com.bendb.thrifty;

import com.bendb.thrifty.parser.TypedefElement;

import java.util.Map;

public final class Typedef extends Named {
    private final TypedefElement element;
    private ThriftType oldType;
    private ThriftType type;

    Typedef(TypedefElement element, Map<NamespaceScope, String> namespaces) {
        super(element.newName(), namespaces);
        this.element = element;
    }

    @Override
    public ThriftType type() {
        return type;
    }

    public String documentation() {
        return element.documentation();
    }

    public String oldName() {
        return element.oldName();
    }

    public ThriftType oldType() {
        return oldType;
    }

    boolean link(Linker linker) {
        ThriftType tt = linker.resolveType(element.oldName());
        if (tt == ThriftType.PLACEHOLDER) {
            return false;
        }

        oldType = tt;
        type = ThriftType.typedefOf(oldType, element.newName());
        return false;
    }
}
