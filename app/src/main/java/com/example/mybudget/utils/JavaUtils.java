package com.example.mybudget.utils;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JavaUtils {
    private final static Map<Integer, Integer> mapOfIds = new HashMap<>();

    /**
     * Adding <Key, Value> pairs into map.
     * To get a value use @getId(int Key)
     * <p>
     * Note: Substitution to Java's Map.of(K, V, K, V...)
     *
     * @param values Unlimited number of key, value pairs
     */
    public static void addToMapIds(int... values) {
        int key = 0;
        for (int i = 0; i < values.length; i++)
        {
            if (i % 2 == 0)
            {
                key = values[i];
            }
            else
            {
                mapOfIds.put(key, values[i]);
            }
        }
    }

    /**
     * Get a value of Id that was stored via {@link #addToMapIds} method
     *
     * @param key key to get a value with
     * @return value of corresponding key
     */
    public static Integer getId(int key) {
        return mapOfIds.get(key);
    }

    /**
     * Creates a map of Integers.
     * <p>
     * Note: Substitution to Java's Map.of(K, V, K, V...)
     *
     * @param values any number of <Key, Value> pairs.
     * @return an T map.
     */
    @SafeVarargs
    public static <T> Map<T, T> mapOf(T... values) {
        Map<T, T> map = new HashMap<>();
        T key = values[0];
        for (int i = 0; i < values.length; i++)
        {
            if (i % 2 == 0)
            {
                key = values[i];
            }
            else
            {
                map.put(key, values[i]);
            }
        }
        return map;
    }

    /**
     * Creates a map.
     *
     * @param keys a list of keys
     * @param values a list of values
     * @return a map.
     */
    public static <K, V> Map<K, V> mapOf(List<K> keys, List<V> values) {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keys.size() ; i++)
        {
            map.put(keys.get(i), values.get(i));
        }
        return map;
    }

    /**
     * create a map of Fragment while generating integer keys.
     * The user should pass any number of fragments only.
     *
     * @param values any number of fragments
     * @return a map with generated keys with corresponding fragments
     */
    public static Map<Integer, Fragment> mapOf(Fragment... values) {
        return IntStream.range(0, values.length)
                .boxed()
                .collect(Collectors.toMap(i -> i, (Integer i) -> values[i]));
    }

    public static <T> String objectToString(T object) {
        Gson gson = new Gson();
        return gson.toJson(object, object.getClass());
    }

    public static <T> T stringToObject(String object, Class<T> classs) {
        Gson gson = new Gson();
        return gson.fromJson(object, classs);
    }
}
