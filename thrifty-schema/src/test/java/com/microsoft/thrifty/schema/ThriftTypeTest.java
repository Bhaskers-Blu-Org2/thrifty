/*
 * Thrifty
 *
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED ON AN  *AS IS* BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING
 * WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE,
 * FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABLITY OR NON-INFRINGEMENT.
 *
 * See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
 */
package com.microsoft.thrifty.schema;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ThriftTypeTest {
    @Test
    public void byteAndI8AreSynonyms() {
        final AtomicInteger ctr = new AtomicInteger(0);

        ThriftType.Visitor<Void> v = new ThriftType.Visitor<Void>() {
            @Override
            public Void visitBool() {
                return null;
            }

            @Override
            public Void visitByte() {
                ctr.incrementAndGet();
                return null;
            }

            @Override
            public Void visitI16() {
                return null;
            }

            @Override
            public Void visitI32() {
                return null;
            }

            @Override
            public Void visitI64() {
                return null;
            }

            @Override
            public Void visitDouble() {
                return null;
            }

            @Override
            public Void visitString() {
                return null;
            }

            @Override
            public Void visitBinary() {
                return null;
            }

            @Override
            public Void visitVoid() {
                return null;
            }

            @Override
            public Void visitEnum(ThriftType userType) {
                return null;
            }

            @Override
            public Void visitList(ThriftType.ListType listType) {
                return null;
            }

            @Override
            public Void visitSet(ThriftType.SetType setType) {
                return null;
            }

            @Override
            public Void visitMap(ThriftType.MapType mapType) {
                return null;
            }

            @Override
            public Void visitUserType(ThriftType userType) {
                return null;
            }

            @Override
            public Void visitTypedef(ThriftType.TypedefType typedefType) {
                return null;
            }
        };

        ThriftType.I8.accept(v);
        ThriftType.BYTE.accept(v);

        assertThat(ThriftType.I8.isBuiltin(), is(true));
        assertThat(ctr.get(), is(2));
    }
}