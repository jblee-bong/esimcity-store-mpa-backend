package com.naver.naverspabackend.common;

import java.util.HashMap;
import java.util.Map;

public class RequestHolder {

    private static final ThreadLocal<Map<RequestHolderKey, Object>> requestHolder = new ThreadLocal<>();

    public static void initialize() {
        requestHolder.set(new HashMap<>());
    }

    public static void put(RequestHolderKey key, String value) {
        requestHolder.get().put(key, value);
    }

    public static Object get(RequestHolderKey key) {
        return requestHolder.get().get(key);
    }

}
