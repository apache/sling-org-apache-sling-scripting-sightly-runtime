/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.scripting.sightly.render;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.sling.scripting.sightly.Record;

/**
 * Basic unit of rendering. This also extends the record interface. The properties for a unit are the sub-units.
 */
public abstract class RenderUnit implements Record<RenderUnit> {

    private final Map<String, RenderUnit> subTemplates = new HashMap<>();

    private Map<String, RenderUnit> siblings;

    /**
     * Render the main script template
     *
     * @param out           the {@link PrintWriter} to which the commands are written
     * @param renderContext the rendering context
     * @param arguments     the arguments for this unit
     */
    public final void render(PrintWriter out, RenderContext renderContext, Bindings arguments) {
        Bindings globalBindings = renderContext.getBindings();
        render(out, buildGlobalScope(globalBindings), new CaseInsensitiveBindings(arguments), renderContext);
    }

    @Override
    public RenderUnit getProperty(String name) {
        return subTemplates.get(name.toLowerCase());
    }

    @Override
    public Set<String> getPropertyNames() {
        return subTemplates.keySet();
    }

    protected abstract void render(PrintWriter out, Bindings bindings, Bindings arguments, RenderContext renderContext);

    @SuppressWarnings({"unused", "unchecked"})
    protected void callUnit(PrintWriter out, RenderContext renderContext, Object templateObj, Object argsObj) {
        if (!(templateObj instanceof RenderUnit)) {
            if (templateObj == null) {
                throw new RuntimeException("data-sly-call: expression evaluates to null.");
            }
            if (renderContext.getObjectModel().isPrimitive(templateObj)) {
                throw new RuntimeException("data-sly-call: primitive \"" + templateObj.toString()
                        + "\" does not represent a HTL template.");
            } else if (templateObj instanceof String) {
                throw new RuntimeException(
                        "data-sly-call: String '" + templateObj.toString() + "' does not represent a HTL template.");
            }
            throw new RuntimeException(
                    "data-sly-call: " + templateObj.getClass().getName() + " does not represent a HTL template.");
        }
        RenderUnit unit = (RenderUnit) templateObj;
        Map<String, Object> argumentsMap = renderContext.getObjectModel().toMap(argsObj);
        Bindings arguments = new SimpleBindings(Collections.unmodifiableMap(argumentsMap));
        unit.render(out, renderContext, arguments);
    }

    @SuppressWarnings("UnusedDeclaration")
    protected FluentMap obj() {
        return new FluentMap();
    }

    @SuppressWarnings("unused")
    protected final void addSubTemplate(String name, RenderUnit renderUnit) {
        renderUnit.setSiblings(subTemplates);
        subTemplates.put(name.toLowerCase(), renderUnit);
    }

    private void setSiblings(Map<String, RenderUnit> siblings) {
        this.siblings = siblings;
    }

    private Bindings buildGlobalScope(Bindings bindings) {
        CaseInsensitiveBindings caseInsensitiveBindings = new CaseInsensitiveBindings(bindings);
        if (siblings != null) {
            caseInsensitiveBindings.putAll(siblings);
        }
        caseInsensitiveBindings.putAll(subTemplates);
        return caseInsensitiveBindings;
    }

    protected static class FluentMap extends HashMap<String, Object> {

        /**
         * Fluent variant of put.
         *
         * @param name  the name of the property
         * @param value the value of the property
         * @return this instance
         */
        public FluentMap with(String name, Object value) {
            put(name, value);
            return this;
        }
    }

    private static final class CaseInsensitiveBindings implements Bindings {

        private final Map<String, Object> wrapped;
        private final Map<String, String> keyMappings;

        private CaseInsensitiveBindings(Map<String, Object> wrapped) {
            this.wrapped = wrapped;
            keyMappings = new HashMap<>();
            for (String key : this.wrapped.keySet()) {
                keyMappings.put(key.toLowerCase(), key);
            }
        }

        @Override
        public Object get(Object key) {
            if (!(key instanceof String)) {
                throw new ClassCastException("key should be a String");
            }
            String mappedKey = keyMappings.get(((String) key).toLowerCase());
            if (mappedKey != null) {
                return wrapped.get(mappedKey);
            }
            return null;
        }

        @Override
        public boolean containsKey(Object key) {
            if (!(key instanceof String)) {
                throw new ClassCastException("key should be a String");
            }
            return keyMappings.containsKey(((String) key).toLowerCase());
        }

        @Override
        public Object put(String key, Object value) {
            keyMappings.put(key.toLowerCase(), key);
            return wrapped.put(key, value);
        }

        @Override
        public void putAll(Map<? extends String, ?> toMerge) {
            for (Map.Entry<? extends String, ?> entry : toMerge.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public Object remove(Object key) {
            if (!(key instanceof String)) {
                throw new ClassCastException("key should be a String");
            }
            String originalKey = keyMappings.remove(((String) key).toLowerCase());
            if (originalKey != null) {
                return wrapped.remove(originalKey);
            }
            return null;
        }

        @Override
        public int size() {
            return wrapped.size();
        }

        @Override
        public boolean isEmpty() {
            return wrapped.isEmpty();
        }

        @Override
        public boolean containsValue(Object value) {
            return wrapped.containsValue(value);
        }

        @Override
        public void clear() {
            wrapped.clear();
            keyMappings.clear();
        }

        @Override
        public Set<String> keySet() {
            return keyMappings.keySet();
        }

        @Override
        public Collection<Object> values() {
            return wrapped.values();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            Set<Entry<String, Object>> entrySet = new HashSet<>();
            for (Map.Entry<String, String> entry : keyMappings.entrySet()) {
                entrySet.add(new AbstractMap.SimpleEntry<>(entry.getKey(), wrapped.get(entry.getValue())));
            }
            return entrySet;
        }
    }
}
