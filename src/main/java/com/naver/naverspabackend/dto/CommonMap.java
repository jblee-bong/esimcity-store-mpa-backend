package com.naver.naverspabackend.dto;

import com.naver.naverspabackend.util.CamelUtil;
import java.util.HashMap;

public class CommonMap extends HashMap {

    private static final long serialVersionUID = 6723434363565852261L;

    @Override
    public Object put(Object key, Object value) {
            return super.put(CamelUtil.convert2CamelCase((String) key), value);
        }

}
