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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code ObjectModel} class provides various static models for object conversion and object property resolution.
 */
public final class ObjectModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectModel.class);
    private static final String EMPTY_STRING = "";

    /**
     * A {@link Set} that stores all the supported primitive classes.
     */
    private static final Set<Class<?>> PRIMITIVE_CLASSES;

    static {
        Set<Class<?>> primitivesBuilder = new HashSet<>();
        primitivesBuilder.add(Boolean.class);
        primitivesBuilder.add(Character.class);
        primitivesBuilder.add(Byte.class);
        primitivesBuilder.add(Short.class);
        primitivesBuilder.add(Integer.class);
        primitivesBuilder.add(Long.class);
        primitivesBuilder.add(Float.class);
        primitivesBuilder.add(Double.class);
        primitivesBuilder.add(Void.class);
        PRIMITIVE_CLASSES = Collections.unmodifiableSet(primitivesBuilder);
    }

    private static final String TO_STRING_METHOD = "toString";

    private ObjectModel() {}

    /**
     * Checks if the provided {@code object} is an instance of a primitive class.
     *
     * @param object the {@code Object} to check
     * @return {@code true} if the {@code object} is a primitive, {@code false} otherwise
     */
    public static boolean isPrimitive(Object object) {
        return PRIMITIVE_CLASSES.contains(object.getClass());
    }

    /**
     * <p>
     *      Given the {@code target} object, this method attempts to resolve and return the value of the passed {@code property}.
     * </p>
     * <p>
     *      The property can be either an index or a name:
     * </p>
     * <ul>
     *      <li>index: the property is considered an index if its value is an integer number and in this case the {@code target}
     *      will be assumed to be either an array or it will be converted to a {@link Collection}; a fallback to {@link Map} will be
     *      made in case the previous two attempts failed
     *      </li>
     *      <li>name: the {@code property} will be converted to a {@link String} (see {@link #toString(Object)}); the {@code target}
     *      will be assumed to be either a {@link Map} or an object; if the {@link Map} attempt fails, the {@code property} will be
     *      used to check if the {@code target} has a publicly accessible field with this name or a publicly accessible method with no
     *      parameters with this name or a combination of the "get" or "is" prefixes plus the capitalised name (see
     *      {@link #invokeBeanMethod(Object, String)})</li>
     * </ul>
     *
     * @param target   the target object
     * @param property the property to be resolved
     * @return the value of the property or {@code null}
     */
    public static Object resolveProperty(Object target, Object property) {
        if (target == null || property == null) {
            return null;
        }
        Object resolved = null;
        if (property instanceof Number) {
            resolved = getIndex(target, ((Number) property).intValue());
        }
        if (resolved == null) {
            String propertyName = toString(property);
            if (StringUtils.isNotEmpty(propertyName)) {
                if (target instanceof Optional) {
                    return resolveProperty(((Optional) target).orElse(null), property);
                }
                if (target instanceof Map) {
                    resolved = ((Map) target).get(property);
                }
                if (resolved == null) {
                    resolved = getEnumValue(target, propertyName);
                }
                if (resolved == null) {
                    resolved = getField(target, propertyName);
                }
                if (resolved == null) {
                    resolved = invokeBeanMethod(target, propertyName);
                }
            }
        }
        return resolved;
    }

    /**
     * Converts the given {@code object} to a boolean value, applying the following rules:
     *
     * <ul>
     *     <li>if the {@code object} is {@code null} the returned value is {@code false}</li>
     *     <li>if the {@code object} is a {@link Number} the method will return {@code false} only if the number's value is 0</li>
     *     <li>if the {@link String} representation of the {@code object} is equal irrespective of its casing to "true", the method will
     *     return {@code true}</li>
     *     <li>if the {@code object} is a {@link Collection} or a {@link Map}, the method will return {@code true} only if the collection /
     *     map is not empty</li>
     *     <li>if the object is an array, the method will return {@code true} only if the array is not empty</li>
     * </ul>
     *
     * @param object the target object
     * @return the boolean representation of the {@code object} according to the conversion rules
     */
    public static boolean toBoolean(Object object) {
        if (object == null) {
            return false;
        }

        if (object instanceof Number) {
            Number number = (Number) object;
            return number.doubleValue() != 0.0;
        }

        if (object instanceof Boolean) {
            return Boolean.TRUE.equals(object);
        }

        if (object instanceof String) {
            return StringUtils.isNotBlank((String) object);
        }

        if (object instanceof Collection<?>) {
            return !((Collection<?>) object).isEmpty();
        }

        if (object instanceof Map<?, ?>) {
            return !((Map<?, ?>) object).isEmpty();
        }

        if (object instanceof Iterable<?>) {
            return ((Iterable<?>) object).iterator().hasNext();
        }

        if (object instanceof Iterator<?>) {
            return ((Iterator<?>) object).hasNext();
        }

        if (object instanceof Optional<?>) {
            Optional<?> optional = (Optional<?>) object;
            return optional.filter(ObjectModel::toBoolean).isPresent();
        }

        if (object.getClass().isArray()) {
            return Array.getLength(object) > 0;
        }
        return true;
    }

    /**
     * Coerces the passed {@code object} to a numeric value. If the passed value is a {@link String} the conversion rules are those of
     * {@link NumberUtils#createNumber(String)}.
     *
     * @param object the target object
     * @return the numeric representation if one can be determined or {@code null}
     * @see NumberUtils#createNumber(String)
     */
    public static Number toNumber(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Number) {
            return (Number) object;
        }
        if (object instanceof Optional) {
            return toNumber(((Optional) object).orElse(null));
        }

        String stringValue = toString(object);
        try {
            return NumberUtils.createNumber(stringValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts the passed {@code object} to a {@link String}. The following rules apply:
     *
     * <ul>
     *     <li>if the {@code object} is {@code null} an empty string will be returned</li>
     *     <li>if the {@code object} is an instance of a {@link String} the object itself will be returned</li>
     *     <li>if the object is a primitive (see {@link #isPrimitive(Object)}), its {@link String} representation will be returned</li>
     *     <li>if the object is an {@link Enum} its name will be returned (see {@link Enum#name()})</li>
     *     <li>if the object is a collection an attempt to convert the object to a {@link Collection} will be made and then the output of
     *     {@link #collectionToString(Collection)} will be returned</li>
     *     <li>otherwise {@code object.toString()} is returned</li>
     * </ul>
     *
     * @param object the target object
     * @return the string representation of the object or an empty string
     */
    public static String toString(Object object) {
        String output = EMPTY_STRING;
        if (object != null) {
            if (object instanceof String) {
                output = (String) object;
            } else if (isPrimitive(object)) {
                output = object.toString();
            } else if (object instanceof Enum) {
                return ((Enum) object).name();
            } else if (object instanceof Optional) {
                return toString(((Optional) object).orElse(EMPTY_STRING));
            } else if (object.getClass().isArray()
                    || object instanceof Collection
                    || object instanceof Enumeration
                    || object instanceof Iterator
                    || object instanceof Iterable) {
                Collection<?> col = toCollection(object);
                output = collectionToString(col);
            } else {
                output = object.toString();
            }
        }
        return output;
    }

    /**
     * Forces the conversion of the passed {@code object} to an immutable collection, according to the following rules:
     *
     * <ul>
     *     <li>if the {@code object} is {@code null} an empty collection will be returned</li>
     *     <li>if the {@code object} is an array a list transformation of the array will be returned</li>
     *     <li>if the {@code object} is a {@link Collection} the object itself will be returned</li>
     *     <li>if the {@code object} is an instance of a {@link Map} the map's key set will be returned (see {@link Map#keySet()})</li>
     *     <li>if the {@code object} is an instance of an {@link Enumeration} a list transformation will be returned</li>
     *     <li>if the {@code object} is an instance of an {@link Iterator} or {@link Iterable} the result of {@link #fromIterator(Iterator)}
     *     will be returned</li>
     *     <li>otherwise the {@code object} is wrapped in a single item list</li>
     * </ul>
     *
     * @param object the target object
     * @return the immutable collection representation of the object
     */
    public static Collection<Object> toCollection(Object object) {
        if (object == null) {
            return Collections.emptyList();
        }
        if (object instanceof Object[]) {
            return Collections.unmodifiableList(Arrays.asList((Object[]) object));
        }
        if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                list.add(Array.get(object, i));
            }
            return Collections.unmodifiableList(list);
        }
        if (object instanceof Optional) {
            return toCollection(((Optional) object).orElse(Collections.emptyList()));
        }
        if (object instanceof List) {
            return Collections.unmodifiableList((List<Object>) object);
        }
        if (object instanceof Collection) {
            return Collections.unmodifiableCollection((Collection<Object>) object);
        }
        if (object instanceof Map) {
            return Collections.unmodifiableCollection(((Map) object).keySet());
        }
        if (object instanceof Enumeration) {
            return Collections.unmodifiableList(Collections.list((Enumeration<Object>) object));
        }
        if (object instanceof Iterator) {
            return fromIterator((Iterator<Object>) object);
        }
        if (object instanceof Iterable) {
            Iterable<Object> iterable = (Iterable<Object>) object;
            return fromIterator(iterable.iterator());
        }
        return Collections.singletonList(object);
    }

    /**
     * Converts the passed {@code collection} to a comma separated values {@link String} representation.
     *
     * @param collection the collection to be converted to CSV
     * @return the CSV; if the {@code collection} is empty then an empty string will be returned
     */
    public static String collectionToString(Collection<?> collection) {
        if (collection == null) {
            return EMPTY_STRING;
        }
        StringBuilder builder = new StringBuilder();
        String prefix = EMPTY_STRING;
        for (Object o : collection) {
            builder.append(prefix).append(toString(o));
            prefix = ",";
        }
        return builder.toString();
    }

    /**
     * Given an {@code iterator}, this method will return a {@link Collection}.
     *
     * @param iterator the iterator to be transformed into a {@code collection}
     * @return an immutable collection with the iterator's elements
     */
    public static Collection<Object> fromIterator(Iterator<Object> iterator) {
        if (iterator == null) {
            return Collections.emptyList();
        }
        ArrayList<Object> result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        return Collections.unmodifiableList(result);
    }

    /**
     * Given an indexable {@code object} (i.e. an array or a collection), this method will return the value available at the {@code
     * index} position.
     *
     * @param object the indexable object
     * @param index  the index
     * @return the value stored at the {@code index} or {@code null}
     */
    public static Object getIndex(Object object, int index) {
        if (object == null) {
            return null;
        }

        // special handing for enum object
        if (object instanceof Class && ((Class<?>) object).isEnum()) {
            Class<?> cls = (Class<?>) object;
            if (index >= 0 && index < cls.getEnumConstants().length) {
                // find the enum constant whose ordinal matches the requested index
                return cls.getEnumConstants()[index];
            } else {
                return null;
            }
        }

        Class<?> cls = object.getClass();
        if (cls.isArray() && index >= 0 && index < Array.getLength(object)) {
            return Array.get(object, index);
        }
        Collection collection = toCollection(object);
        if (collection instanceof List && index >= 0 && index < collection.size()) {
            return ((List) collection).get(index);
        }
        return null;
    }

    /**
     * Given an {@code object}, this method will return the value of the public field identified by {@code fieldName}.
     *
     * @param object    the target object
     * @param fieldName the name of the field
     * @return the value of the field or {@code null} if the field was not found
     */
    public static Object getField(Object object, String fieldName) {
        if (object == null || StringUtils.isEmpty(fieldName)) {
            return null;
        }

        Class<?> cls;
        // special handing for enum object
        if (object instanceof Class && ((Class<?>) object).isEnum()) {
            cls = (Class<?>) object;
        } else {
            cls = object.getClass();
        }

        if (cls.isArray() && "length".equals(fieldName)) {
            return Array.getLength(object);
        }
        for (Field field : cls.getFields()) {
            if (field.getName().equals(fieldName)) {
                try {
                    return field.get(object);
                } catch (IllegalAccessException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Given a bean {@code object}, this method will invoke the public method without parameters identified by {@code methodName} and
     * return the invocation's result.
     *
     * @param object     the target object
     * @param methodName the name of the public method without parameters to invoke
     * @return the invocation's result or {@code null} if such a method cannot be found
     */
    public static Object invokeBeanMethod(Object object, String methodName) {
        if (object == null || StringUtils.isEmpty(methodName)) {
            return null;
        }
        Class<?> cls;
        // special handing for enum object
        if (object instanceof Class && ((Class<?>) object).isEnum()) {
            cls = (Class<?>) object;
        } else {
            cls = object.getClass();
        }
        Method method = findBeanMethod(cls, methodName);
        if (method != null) {
            try {
                method = extractMethodInheritanceChain(cls, method);
                return method.invoke(object);
            } catch (Exception e) {
                LOGGER.error("Cannot access method " + methodName + " on object " + object.toString(), e);
            }
        }
        return null;
    }

    /**
     * Given an {@code object}, this method will return the value of the enum value identified by {@code valueName}.
     *
     * @param object    the target object
     * @param valueName the name of the enum value
     * @return the value of the enum or {@code null} if the enum was not found
     */
    public static Object getEnumValue(Object object, String valueName) {
        if (object == null || StringUtils.isEmpty(valueName)) {
            return null;
        }
        if (object instanceof Class && ((Class<?>) object).isEnum()) {
            try {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Enum<?> value = Enum.valueOf((Class<Enum>) object, valueName);
                return value;
            } catch (IllegalArgumentException e) {
                // not a valid enum value?
                return null;
            }
        }
        return null;
    }

    /**
     * Given a bean class and a base method name, this method will try to find a public method without parameters that is named:
     * <ol>
     *      <li>{@code baseName}</li>
     *      <li>get + {@code BaseName}</li>
     *      <li>is + {@code BaseName}</li>
     * </ol>
     *
     * @param cls      the class into which to search for the method
     * @param baseName the base method name
     * @return a method that matches the criteria or {@code null}
     */
    public static Method findBeanMethod(Class<?> cls, String baseName) {
        if (cls == null || StringUtils.isEmpty(baseName)) {
            return null;
        }
        Method[] publicMethods = cls.getMethods();
        String capitalized = StringUtils.capitalize(baseName);
        for (Method method : publicMethods) {
            if (method.getParameterTypes().length == 0) {
                String methodName = method.getName();
                if (baseName.equals(methodName)
                        || ("get" + capitalized).equals(methodName)
                        || ("is" + capitalized).equals(methodName)) {
                    if (isMethodAllowed(method)) {
                        return method;
                    }
                    break;
                }
            }
        }
        return null;
    }

    /**
     * Returns {@code true} if the method is not one of the {@link Object}'s class declared methods, with the exception of
     * {@link Object#toString()}.
     *
     * @param method the method to check
     * @return {@code true} if the method is not one of the {@link Object}'s class declared methods, with the exception of
     * {@link Object#toString()}, {@code false} otherwise
     */
    public static boolean isMethodAllowed(Method method) {
        if (method == null) {
            return false;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        return declaringClass != Object.class || TO_STRING_METHOD.equals(method.getName());
    }

    private static Method extractMethodInheritanceChain(Class type, Method method) {
        if (method == null || Modifier.isPublic(type.getModifiers())) {
            return method;
        }
        Class[] interfaces = type.getInterfaces();
        Method parentMethod;
        for (Class<?> iface : interfaces) {
            parentMethod = getClassMethod(iface, method);
            if (parentMethod != null) {
                return parentMethod;
            }
        }
        return getClassMethod(type.getSuperclass(), method);
    }

    private static Method getClassMethod(Class<?> type, Method method) {
        try {
            Method parentMethod = type.getMethod(method.getName(), method.getParameterTypes());
            parentMethod = extractMethodInheritanceChain(parentMethod.getDeclaringClass(), parentMethod);
            if (parentMethod != null) {
                return parentMethod;
            }
        } catch (NoSuchMethodException e) {
            // ignore - maybe we don't have access to that method or the method does not belong to the current type
        }
        return null;
    }
}
