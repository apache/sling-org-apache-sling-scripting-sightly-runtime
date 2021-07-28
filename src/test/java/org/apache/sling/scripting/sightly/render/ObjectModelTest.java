/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Licensed to the Apache Software Foundation (ASF) under one
 ~ or more contributor license agreements.  See the NOTICE file
 ~ distributed with this work for additional information
 ~ regarding copyright ownership.  The ASF licenses this file
 ~ to you under the Apache License, Version 2.0 (the
 ~ "License"); you may not use this file except in compliance
 ~ with the License.  You may obtain a copy of the License at
 ~
 ~   http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package org.apache.sling.scripting.sightly.render;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import org.apache.sling.scripting.sightly.render.testobjects.Person;
import org.apache.sling.scripting.sightly.render.testobjects.TestEnum;
import org.apache.sling.scripting.sightly.render.testobjects.internal.AdultFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ObjectModelTest {

    @Test
    public void testToBoolean() {
        assertFalse(ObjectModel.toBoolean(null));
        assertFalse(ObjectModel.toBoolean(0));
        assertTrue(ObjectModel.toBoolean(123456));
        assertFalse(ObjectModel.toBoolean(""));
        assertFalse(ObjectModel.toBoolean(false));
        assertFalse(ObjectModel.toBoolean(Boolean.FALSE));
        assertFalse(ObjectModel.toBoolean(new int[0]));
        assertTrue(ObjectModel.toBoolean("FalSe"));
        assertTrue(ObjectModel.toBoolean("false"));
        assertTrue(ObjectModel.toBoolean("FALSE"));
        assertTrue(ObjectModel.toBoolean("true"));
        assertTrue(ObjectModel.toBoolean("TRUE"));
        assertTrue(ObjectModel.toBoolean("TrUE"));
        Integer[] testArray = new Integer[] {1, 2, 3};
        int[] testPrimitiveArray = new int[] {1, 2, 3};
        List testList = Arrays.asList(testArray);
        assertTrue(ObjectModel.toBoolean(testArray));
        assertTrue(ObjectModel.toBoolean(testPrimitiveArray));
        assertFalse(ObjectModel.toBoolean(new Integer[]{}));
        assertTrue(ObjectModel.toBoolean(testList));
        assertFalse(ObjectModel.toBoolean(Collections.emptyList()));
        Map<String, Integer> map = new HashMap<String, Integer>() {{
            put("one", 1);
            put("two", 2);
        }};
        assertTrue(ObjectModel.toBoolean(map));
        assertFalse(ObjectModel.toBoolean(Collections.EMPTY_MAP));
        assertTrue(ObjectModel.toBoolean(testList.iterator()));
        assertFalse(ObjectModel.toBoolean(Collections.EMPTY_LIST.iterator()));
        assertTrue(ObjectModel.toBoolean(new Bag<>(testArray)));
        assertFalse(ObjectModel.toBoolean(new Bag<>(new Integer[]{})));
        assertTrue(ObjectModel.toBoolean(new Date()));

        assertFalse(ObjectModel.toBoolean(Optional.empty()));
        assertFalse(ObjectModel.toBoolean(Optional.of("")));
        assertFalse(ObjectModel.toBoolean(Optional.of(false)));
        assertFalse(ObjectModel.toBoolean(Optional.ofNullable(null)));
        assertTrue(ObjectModel.toBoolean(Optional.of(true)));
        assertTrue(ObjectModel.toBoolean(Optional.of("pass")));
        assertTrue(ObjectModel.toBoolean(Optional.of(1)));
        assertTrue(ObjectModel.toBoolean(new Object()));
        Map<String, String> map2 = new HashMap<String, String>() {
            @Override
            public String toString() {
                return null;
            }
        };
        assertFalse(ObjectModel.toBoolean(map2));
        map2.put("one", "entry");
        assertTrue(ObjectModel.toBoolean(map2));

    }

    @Test
    public void testToNumber() {
        assertEquals(1, ObjectModel.toNumber(1));
        assertEquals(1, ObjectModel.toNumber("1"));
        assertNull(ObjectModel.toNumber(null));
        assertNull(ObjectModel.toNumber("1-2"));

        assertNull(ObjectModel.toNumber(Optional.empty()));
        assertNull(ObjectModel.toNumber(Optional.of(false)));
        assertNull(ObjectModel.toNumber(Optional.ofNullable(null)));
        assertNull(ObjectModel.toNumber(Optional.of(true)));
        assertNull(ObjectModel.toNumber(Optional.of("pass")));
        assertEquals(1, ObjectModel.toNumber(Optional.of(1)));
        assertEquals(1, ObjectModel.toNumber(Optional.of("1")));
    }

    @Test
    public void testToString() throws URISyntaxException {
        assertEquals("", ObjectModel.toString(null));
        assertEquals("1", ObjectModel.toString("1"));
        assertEquals("1", ObjectModel.toString(1));
        assertEquals("CONSTANT", ObjectModel.toString(TestEnum.CONSTANT));
        Integer[] testArray = new Integer[] {1, 2, 3};
        int[] testPrimitiveArray = new int[] {1, 2, 3};
        List testList = Arrays.asList(testArray);
        assertEquals("1,2,3", ObjectModel.toString(testList));
        assertEquals("1,2,3", ObjectModel.toString(testArray));
        assertEquals("1,2,3", ObjectModel.toString(testPrimitiveArray));
        assertEquals("http://localhost/test", ObjectModel.toString(new URI("http://localhost/test")));

        assertEquals("", ObjectModel.toString(Optional.empty()));
        assertEquals("false", ObjectModel.toString(Optional.of(false)));
        assertEquals("", ObjectModel.toString(Optional.ofNullable(null)));
        assertEquals("true", ObjectModel.toString(Optional.of(true)));
        assertEquals("pass", ObjectModel.toString(Optional.of("pass")));
        assertEquals("1", ObjectModel.toString(Optional.of(1)));
        assertEquals("1", ObjectModel.toString(Optional.of("1")));
    }

    @Test
    public void testToCollection() {
        assertTrue(ObjectModel.toCollection(null).isEmpty());
        StringBuilder sb = new StringBuilder();
        assertEquals(Collections.singletonList(sb), ObjectModel.toCollection(sb));
        Integer[] testArray = new Integer[] {1, 2, 3};
        int[] testPrimitiveArray = new int[] {1, 2, 3};
        List<Integer> testList = Arrays.asList(testArray);
        Map<String, Integer> map = new HashMap<String, Integer>() {{
            put("one", 1);
            put("two", 2);
        }};
        assertEquals(testList, ObjectModel.toCollection(testArray));
        assertEquals(testList, ObjectModel.toCollection(testPrimitiveArray));
        assertEquals(testList, ObjectModel.toCollection(testList));
        MatcherAssert.assertThat(ObjectModel.toCollection(map), Matchers.contains(map.keySet().toArray()));
        Vector<Integer> vector = new Vector<>(testList);
        assertEquals(testList, ObjectModel.toCollection(vector.elements()));
        assertEquals(testList, ObjectModel.toCollection(testList.iterator()));
        assertEquals(testList, ObjectModel.toCollection(new Bag<>(testArray)));
        String stringObject = "test";
        Integer numberObject = 1;
        Collection stringCollection = ObjectModel.toCollection(stringObject);
        assertTrue(stringCollection.size() == 1 && stringCollection.contains(stringObject));
        Collection numberCollection = ObjectModel.toCollection(numberObject);
        assertTrue(numberCollection.size() == 1 && numberCollection.contains(numberObject));

        List<Object> emptyList = Collections.emptyList();
        assertEquals(emptyList, ObjectModel.toCollection(Optional.empty()));
        assertEquals(emptyList, ObjectModel.toCollection(Optional.of(Arrays.asList())));
        List<Integer> list = Arrays.asList(1, 2, 3);
        assertEquals(list, ObjectModel.toCollection(Optional.of(list)));
    }

    @Test
    public void testCollectionToString() {
        assertEquals("", ObjectModel.collectionToString(null));
        Integer[] testArray = new Integer[] {1, 2, 3};
        List testList = Arrays.asList(testArray);
        assertEquals("1,2,3", ObjectModel.collectionToString(testList));
    }

    @Test
    public void testFromIterator() {
        assertTrue(ObjectModel.fromIterator(null).isEmpty());
        Integer[] testArray = new Integer[] {1, 2, 3};
        List testList = Arrays.asList(testArray);
        assertEquals(testList, ObjectModel.fromIterator(testList.iterator()));
    }

    @Test
    public void testResolveProperty() {
        assertNull(ObjectModel.resolveProperty(null, 0));
        assertNull(ObjectModel.resolveProperty(this, null));
        assertNull(ObjectModel.resolveProperty(null, null));
        assertEquals(0, ObjectModel.resolveProperty(Collections.EMPTY_LIST, "size"));
        Integer[] testArray = new Integer[] {1, 2, 3};
        assertEquals(2, ObjectModel.resolveProperty(testArray, 1));
        assertNull(ObjectModel.resolveProperty(testArray, 3));
        assertNull(ObjectModel.resolveProperty(testArray, -1));
        List<Integer> testList = Arrays.asList(testArray);
        assertEquals(2, ObjectModel.resolveProperty(testList, 1));
        assertNull(ObjectModel.resolveProperty(testList, 3));
        assertNull(ObjectModel.resolveProperty(testList, -1));
        Map<String, Integer> map = new HashMap<String, Integer>() {{
            put("one", 1);
            put("two", 2);
        }};
        assertEquals(1, ObjectModel.resolveProperty(map, "one"));
        assertNull(ObjectModel.resolveProperty(map, null));
        assertNull(ObjectModel.resolveProperty(map, ""));
        Map<Integer, String> stringMap = new HashMap<Integer, String>(){{
            put(1, "one");
            put(2, "two");
        }};
        assertEquals("one", ObjectModel.resolveProperty(stringMap, 1));
        assertEquals("two", ObjectModel.resolveProperty(stringMap, 2));
        Person johnDoe = AdultFactory.createAdult("John", "Doe");
        assertEquals("Expected to be able to access public static final constants.", 1l, ObjectModel.resolveProperty(johnDoe, "CONSTANT"));
        assertNull("Did not expect to be able to access public fields from package protected classes.", ObjectModel.resolveProperty(johnDoe,
                "TODAY"));
        assertEquals("Expected to be able to access an array's length property.", 3, ObjectModel.resolveProperty(testArray, "length"));
        assertNotNull("Expected not null result for invocation of interface method on implementation class.",
                ObjectModel.resolveProperty(johnDoe, "lastName"));
        assertNull("Expected null result for public method available on implementation but not exposed by interface.", ObjectModel
                .resolveProperty(johnDoe, "fullName"));
        assertNull("Expected null result for inexistent method.", ObjectModel.resolveProperty(johnDoe, "nomethod"));

        OptionalTest optionalTest = new OptionalTest();
        assertEquals(Optional.of("string"), ObjectModel.resolveProperty(optionalTest, "string"));
        assertEquals(Optional.of(1), ObjectModel.resolveProperty(optionalTest, "int"));
        assertEquals(Optional.of(1), ObjectModel.resolveProperty(Optional.of(optionalTest), "int"));
        assertEquals(Optional.of(Integer.valueOf(1)), ObjectModel.resolveProperty(optionalTest, "integer"));
        assertEquals(Optional.of(Integer.valueOf(1)), ObjectModel.resolveProperty(Optional.of(optionalTest), "integer"));
        assertEquals(null, ObjectModel.resolveProperty(Optional.empty(), "integer"));

    }

    @Test
    public void testGetIndex() {
        assertNull(ObjectModel.getIndex(null, 0));
        Integer[] testArray = new Integer[] {1, 2, 3};
        assertEquals(2, ObjectModel.getIndex(testArray, 1));
        assertNull(ObjectModel.getIndex(testArray, 3));
        assertNull(ObjectModel.getIndex(testArray, -1));
        List<Integer> testList = Arrays.asList(testArray);
        assertEquals(2, ObjectModel.getIndex(testList, 1));
        assertNull(ObjectModel.getIndex(testList, 3));
        assertNull(ObjectModel.getIndex(testList, -1));
        Map<Integer, String> stringMap = new HashMap<Integer, String>(){{
            put(1, "one");
            put(2, "two");
        }};
        assertNull(ObjectModel.getIndex(stringMap, 1));
        assertNull(ObjectModel.getIndex(stringMap, 2));
    }

    @Test
    public void testClassBasedMethodsForNulls() {
        assertNull(ObjectModel.getField(null, null));
        assertNull(ObjectModel.getField("", null));
        assertNull(ObjectModel.getField(this, ""));
        assertNull(ObjectModel.findBeanMethod(null, null));
        assertNull(ObjectModel.findBeanMethod(this.getClass(), null));
        assertNull(ObjectModel.findBeanMethod(this.getClass(), ""));
        assertNull(ObjectModel.invokeBeanMethod(null, null));
        assertNull(ObjectModel.invokeBeanMethod(this, null));
        assertNull(ObjectModel.invokeBeanMethod(this, ""));
    }


    private class Bag<T> implements Iterable<T> {

        private T[] backingArray;

        public Bag(T[] array) {
            this.backingArray = array;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {

                int index = 0;

                @Override
                public boolean hasNext() {
                    return index < backingArray.length;
                }

                @Override
                public T next() {
                    return backingArray[index++];
                }

                @Override
                public void remove() {

                }
            };
        }
    }

    public class OptionalTest {
        public Optional<String> getEmpty() {
            return Optional.empty();
        }
        public Optional<String> getString() {
            return Optional.of("string");
        }
        public Optional<Integer> getInt() {
            return Optional.of(1);
        }
        public Optional<Integer> getInteger() {
            return Optional.of(Integer.valueOf(1));
        }
    }
}

