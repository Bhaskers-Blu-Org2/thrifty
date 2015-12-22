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
package com.bendb.thrifty.protocol;

public class MapMetadata {
    public final byte keyTypeId;
    public final byte valueTypeId;
    public final int size;

    public MapMetadata(byte keyTypeId, byte valueTypeId, int size) {
        this.keyTypeId = keyTypeId;
        this.valueTypeId = valueTypeId;
        this.size = size;
    }
}
