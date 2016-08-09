/*
 * (C) Copyright 2006-2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Tiago Cardoso <tcardoso@nuxeo.com>
 */
package org.nuxeo.ecm.platform.threed;

import org.nuxeo.ecm.core.api.Blob;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Object wrapping a transmission format with {@link ThreeD} and {@code lod}
 *
 * @since 8.4
 */
public class TransmissionThreeD extends ThreeD {

    public static final String NAME = "name";

    public static final String CONTENT = "content";

    public static final String LOD = "lod";

    protected final int lod;

    protected final String name;

    public TransmissionThreeD(Blob blob, int lod, String name) {
        super(blob, null);
        this.lod = lod;
        this.name = name;
    }

    public TransmissionThreeD(Map<String, Object> map) {
        super((Blob) map.get(CONTENT), null);
        name = (String) map.get(NAME);
        Long lodLong = (Long) map.get(LOD);
        lod = (lodLong != null) ? lodLong.intValue() : 0;
    }

    public String getTitle() {
        return String.valueOf(lod);
    }

    public int getLod() {
        return lod;
    }

    public String getName() {
        return name;
    }

    public Map<String, Serializable> toMap() {
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        map.put(NAME, name);
        map.put(CONTENT, (Serializable) blob);
        map.put(LOD, lod);
        return map;
    }
}
