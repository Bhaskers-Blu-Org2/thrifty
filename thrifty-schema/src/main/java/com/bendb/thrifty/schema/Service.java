/*
 * Copyright (C) 2015 Benjamin Bader
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bendb.thrifty.schema;

import com.bendb.thrifty.schema.parser.AnnotationElement;
import com.bendb.thrifty.schema.parser.FunctionElement;
import com.bendb.thrifty.schema.parser.ServiceElement;
import com.bendb.thrifty.schema.parser.TypeElement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class Service extends Named {
    private final ServiceElement element;
    private final ImmutableList<ServiceMethod> methods;
    private final ThriftType type;
    private final ImmutableMap<String, String> annotations;

    private ThriftType extendsService;

    Service(ServiceElement element, ThriftType type, Map<NamespaceScope, String> namespaces) {
        super(element.name(), namespaces);
        this.element = element;
        this.type = type;

        ImmutableList.Builder<ServiceMethod> methods = ImmutableList.builder();
        for (FunctionElement functionElement : element.functions()) {
            ServiceMethod method = new ServiceMethod(functionElement);
            methods.add(method);
        }
        this.methods = methods.build();

        ImmutableMap.Builder<String, String> annotationBuilder = ImmutableMap.builder();
        AnnotationElement anno = element.annotations();
        if (anno != null) {
            annotationBuilder.putAll(anno.values());
        }
        this.annotations = annotationBuilder.build();
    }

    @Override
    public ThriftType type() {
        return type;
    }

    public String documentation() {
        return element.documentation();
    }

    @Override
    public Location location() {
        return element.location();
    }

    public ImmutableList<ServiceMethod> methods() {
        return methods;
    }

    public ThriftType extendsService() {
        return extendsService;
    }

    public ImmutableMap<String, String> annotations() {
        return annotations;
    }

    void link(Linker linker) {
        TypeElement extendsType = element.extendsService();
        if (extendsType != null) {
            extendsService = linker.resolveType(extendsType);
            // TODO: Validate that this is actually a service type
        }

        for (ServiceMethod method : methods) {
            method.link(linker);
        }
    }

    void validate(Linker linker) {
        // TODO: Implement
        throw new IllegalStateException("not implemented");
    }
}
