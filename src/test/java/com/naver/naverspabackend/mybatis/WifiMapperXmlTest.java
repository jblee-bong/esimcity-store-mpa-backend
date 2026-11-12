package com.naver.naverspabackend.mybatis;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import java.io.InputStream;
import java.lang.reflect.Method;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/** DB 없이 wifi_SQL.xml 파싱 + 매퍼 메서드가 모두 SQL 을 갖는지 확인 (XML 오류는 서버 기동 실패로 이어짐) */
class WifiMapperXmlTest {

    @Test
    void everyMapperMethodHasStatement() throws Exception {
        Configuration cfg = new Configuration();
        cfg.setMapUnderscoreToCamelCase(true);
        String res = "mybatis/sqlmap/wifi_SQL.xml";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(res)) {
            new XMLMapperBuilder(in, cfg, res, cfg.getSqlFragments()).parse();
        }
        for (Method m : WifiMapper.class.getDeclaredMethods()) {
            String id = WifiMapper.class.getName() + "." + m.getName();
            assertTrue(cfg.hasStatement(id), "SQL 없음: " + m.getName());
        }
    }
}
