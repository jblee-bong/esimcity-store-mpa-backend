package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.LongSerializationPolicy;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class JsonUtil {

    private static final Gson gson;

    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    private static JsonDeserializer<Date> dateJsonDeserializer = (json, typeOfT, context) -> {
        if (json == null) {
            return null;
        }
        Date result = null;
        try {
            result = Timestamp.valueOf(json.getAsString());
        } catch (IllegalArgumentException e) {
            result = new Timestamp(json.getAsLong());
        }
        return result;
    };

    static {
        gson = new GsonBuilder()
            .setLongSerializationPolicy(LongSerializationPolicy.DEFAULT)
            .registerTypeAdapter(Date.class, dateJsonDeserializer)
            .setDateFormat(dateFormat)
            .create();
    }

    public static Gson getGson() {
        return gson;
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws JsonSyntaxException {
        return gson.fromJson(json, clazz);
    }

    public static <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        return gson.fromJson(json, typeOfT);
    }


    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
