package com.github.ant.utils;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ParameterTool {

    protected static final String NO_VALUE_KEY = "__NO_VALUE_KEY";
    protected static final String DEFAULT_UNDEFINED = "<undefined>";

    public static ParameterTool fromArgs(String[] args) {
        final Map<String, String> map = new HashMap<>(args.length / 2);

        int i = 0;
        while (i < args.length) {
            final String key = getKeyFromArgs(args, i);

            if (key.isEmpty()) {
                throw new IllegalArgumentException(
                        "The input " + Arrays.toString(args) + " contains an empty argument");
            }

            i += 1; // try to find the value

            if (i >= args.length) {
                map.put(key, NO_VALUE_KEY);
            } else if (NumberUtils.isNumber(args[i])) {
                map.put(key, args[i]);
                i += 1;
            } else if (args[i].startsWith("--") || args[i].startsWith("-")) {
                // the argument cannot be a negative number because we checked earlier
                // -> the next argument is a parameter name
                map.put(key, NO_VALUE_KEY);
            } else {
                map.put(key, args[i]);
                i += 1;
            }
        }

        return fromMap(map);
    }

    public static ParameterTool fromMap(Map<String, String> map) {
        Preconditions.checkNotNull(map, "Unable to initialize from empty map");
        return new ParameterTool(map);
    }

    protected final Map<String, String> data;
    protected transient Map<String, String> defaultData;
    protected transient Set<String> unrequestedParameters;

    private ParameterTool(Map<String, String> data) {
        this.data = Collections.unmodifiableMap(new HashMap<>(data));

        this.defaultData = new ConcurrentHashMap<>(data.size());

        this.unrequestedParameters = Collections.newSetFromMap(new ConcurrentHashMap<>(data.size()));

        unrequestedParameters.addAll(data.keySet());
    }

    public static String getKeyFromArgs(String[] args, int index) {
        String key;
        if (args[index].startsWith("--")) {
            key = args[index].substring(2);
        } else if (args[index].startsWith("-")) {
            key = args[index].substring(1);
        } else {
            throw new IllegalArgumentException(
                    String.format("Error parsing arguments '%s' on '%s'. Please prefix keys with -- or -.",
                            Arrays.toString(args), args[index]));
        }

        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                    "The input " + Arrays.toString(args) + " contains an empty argument");
        }

        return key;
    }

    public String get(String key) {
        addToDefaults(key, null);
        unrequestedParameters.remove(key);
        return data.get(key);
    }

    public String get(String key, String defaultValue) {
        addToDefaults(key, defaultValue);
        String value = get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public int getInt(String key) {
        addToDefaults(key, null);
        String value = getRequired(key);
        return Integer.parseInt(value);
    }

    public String getRequired(String key) {
        addToDefaults(key, null);
        String value = get(key);
        if (value == null) {
            throw new RuntimeException("No data for required key '" + key + "'");
        }
        return value;
    }

    public int getInt(String key, int defaultValue) {
        addToDefaults(key, Integer.toString(defaultValue));
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public boolean has(String value) {
        addToDefaults(value, null);
        unrequestedParameters.remove(value);
        return data.containsKey(value);
    }

    public long getLong(String key) {
        addToDefaults(key, null);
        String value = getRequired(key);
        return Long.parseLong(value);
    }

    public long getLong(String key, long defaultValue) {
        addToDefaults(key, Long.toString(defaultValue));
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }

    public float getFloat(String key) {
        addToDefaults(key, null);
        String value = getRequired(key);
        return Float.parseFloat(value);
    }

    public float getFloat(String key, float defaultValue) {
        addToDefaults(key, Float.toString(defaultValue));
        String value = get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return Float.parseFloat(value);
        }
    }

    public double getDouble(String key) {
        addToDefaults(key, null);
        String value = getRequired(key);
        return Double.parseDouble(value);
    }

    public double getDouble(String key, double defaultValue) {
        addToDefaults(key, Double.toString(defaultValue));
        String value = get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return Double.parseDouble(value);
        }
    }

    public boolean getBoolean(String key) {
        addToDefaults(key, null);
        String value = getRequired(key);
        return Boolean.parseBoolean(value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        addToDefaults(key, Boolean.toString(defaultValue));
        String value = get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return Boolean.parseBoolean(value);
        }
    }



    protected void addToDefaults(String key, String value) {
        final String currentValue = defaultData.get(key);
        if (currentValue == null) {
            if (value == null) {
                value = DEFAULT_UNDEFINED;
            }
            defaultData.put(key, value);
        } else {
            // there is already an entry for this key. Check if the value is the undefined
            if (currentValue.equals(DEFAULT_UNDEFINED) && value != null) {
                // update key with better default value
                defaultData.put(key, value);
            }
        }
    }
}
