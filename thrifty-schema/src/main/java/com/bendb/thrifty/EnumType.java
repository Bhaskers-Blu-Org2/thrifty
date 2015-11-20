package com.bendb.thrifty;

import com.bendb.thrifty.parser.EnumElement;
import com.bendb.thrifty.parser.EnumMemberElement;
import com.google.common.collect.ImmutableList;

import java.util.Map;

public final class EnumType extends Named {
    private final EnumElement element;
    private final ThriftType type;
    private final ImmutableList<Member> members;

    EnumType(EnumElement element, ThriftType type, Map<NamespaceScope, String> namespaces) {
        super(element.name(), namespaces);
        this.element = element;
        this.type = type;

        ImmutableList.Builder<Member> membersBuilder = ImmutableList.builder();
        for (EnumMemberElement memberElement : element.members()) {
            membersBuilder.add(new Member(memberElement));
        }
        this.members = membersBuilder.build();
    }

    public String documentation() {
        return element.documentation();
    }

    public ImmutableList<Member> members() {
        return members;
    }

    @Override
    public ThriftType type() {
        return null;
    }

    public static final class Member {
        private final EnumMemberElement element;

        Member(EnumMemberElement element) {
            this.element = element;
        }

        public String name() {
            return element.name();
        }

        public Integer value() {
            return element.value();
        }

        public String documentation() {
            return element.documentation();
        }
    }
}
