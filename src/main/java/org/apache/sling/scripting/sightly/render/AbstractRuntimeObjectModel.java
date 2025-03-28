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

import java.time.Instant;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.scripting.sightly.Record;

/**
 * Default abstract implementation of {@link RuntimeObjectModel}.
 */
public abstract class AbstractRuntimeObjectModel implements RuntimeObjectModel {

    @Override
    public boolean isPrimitive(Object obj) {
        return ObjectModel.isPrimitive(obj);
    }

    @Override
    public boolean isDate(Object target) {
        return (target instanceof Date || target instanceof Calendar || target instanceof Instant);
    }

    @Override
    public boolean isNumber(Object target) {
        if (target == null) {
            return false;
        }
        if (target instanceof Number) {
            return true;
        }
        String value = toString(target);
        return NumberUtils.isCreatable(value);
    }

    @Override
    public boolean isCollection(Object target) {
        return (target instanceof Collection)
                || (target instanceof Object[])
                || (target instanceof Iterable)
                || (target instanceof Iterator);
    }

    @Override
    public Object resolveProperty(Object target, Object property) {
        if (target == null || property == null) {
            return null;
        }
        Object resolved = null;
        if (property instanceof Number) {
            resolved = ObjectModel.getIndex(target, ((Number) property).intValue());
        }
        if (resolved == null) {
            resolved = getProperty(target, property);
        }
        return resolved;
    }

    @Override
    public boolean toBoolean(Object object) {
        return ObjectModel.toBoolean(object);
    }

    @Override
    public Number toNumber(Object object) {
        return ObjectModel.toNumber(object);
    }

    @Override
    public Date toDate(Object object) {
        if (object instanceof Date) {
            return (Date) object;
        } else if (object instanceof Calendar) {
            return ((Calendar) object).getTime();
        } else if (object instanceof Instant) {
            return Date.from((Instant) object);
        }
        return null;
    }

    @Override
    public Instant toInstant(Object object) {
        if (object instanceof Date) {
            return ((Date) object).toInstant();
        } else if (object instanceof Calendar) {
            return (((Calendar) object)).toInstant();
        } else if (object instanceof Instant) {
            return (Instant) object;
        }
        return null;
    }

    @Override
    public String toString(Object target) {
        return ObjectModel.toString(target);
    }

    @Override
    public Collection<Object> toCollection(Object object) {
        if (object instanceof Record) {
            return ((Record) object).getPropertyNames();
        }
        return ObjectModel.toCollection(object);
    }

    @Override
    public Map toMap(Object object) {
        if (object instanceof Map) {
            return (Map) object;
        } else if (object instanceof Record) {
            Map<String, Object> map = new HashMap<>();
            Record record = (Record) object;
            Set<String> properties = record.getPropertyNames();
            for (String property : properties) {
                map.put(property, record.getProperty(property));
            }
            return map;
        }
        return Collections.emptyMap();
    }

    protected Object getProperty(Object target, Object propertyObj) {
        if (target == null || propertyObj == null) {
            return null;
        }
        String property = ObjectModel.toString(propertyObj);
        Object result = null;
        if (target instanceof Record) {
            result = ((Record) target).getProperty(property);
        }
        if (result == null) {
            result = ObjectModel.resolveProperty(target, propertyObj);
        }
        return result;
    }
}
