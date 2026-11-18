package com.naver.naverspabackend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.naver.naverspabackend.dto.WifiProductDto;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TugeDeviceUtilTest {

    @Test
    void hexEncodingUsesLettersAToP() {
        byte[] bytes = {0x00, 0x0f, (byte) 0xf0, (byte) 0xff, 0x12};
        String enc = TugeDeviceUtil.encodeBytes(bytes);
        assertEquals("aaappappbc", enc); // 0x00→aa, 0x0f→ap, 0xf0→pa, 0xff→pp, 0x12→bc
        assertEquals(bytes.length, TugeDeviceUtil.decodeBytes(enc).length);
        for (int i = 0; i < bytes.length; i++) assertEquals(bytes[i], TugeDeviceUtil.decodeBytes(enc)[i]);
    }

    @Test
    void aesRoundTrip() throws Exception {
        TugeDeviceUtil u = new TugeDeviceUtil();
        ReflectionTestUtils.setField(u, "secretKey", "0123456789abcdef");
        ReflectionTestUtils.setField(u, "vector", "fedcba9876543210");
        String plain = "{\"deviceNo\":\"TGT2812050012\",\"lang\":\"en\"}";
        String enc = u.encrypt(plain);
        assertTrue(enc.matches("[a-p]+"));
        assertEquals(plain, u.decrypt(enc));
    }

    @Test
    void md5IsLowercaseHex() throws Exception {
        assertEquals("900150983cd24fb0d6963f7d28e17f72", TugeDeviceUtil.md5("abc"));
    }

    @Test
    void utcToKstAddsNineHours() {
        assertEquals("2025-02-28 09:00", TugeDeviceUtil.utcToKst("2025-02-28 00:00:00"));
    }

    @Test
    void slowAfterAutoFromDataLimitedUnlessAdminSets() {
        com.naver.naverspabackend.dto.WifiProductDto p = new com.naver.naverspabackend.dto.WifiProductDto();
        p.setDataLimited("N");
        assertTrue(p.isSlowAfter());       // 자동: 용량 제한 없음 → 저속 계속
        p.setDataLimited("Y");
        assertFalse(p.isSlowAfter());      // 자동: 정해진 용량 → 추가 충전
        p.setAfterUse("SLOW");
        assertTrue(p.isSlowAfter());       // 관리자 지정이 우선
        p.setDataLimited("N");
        p.setAfterUse("STOP");
        assertFalse(p.isSlowAfter());
    }

    @Test
    void periodSelectableOnlyForOneDayDaily() {
        WifiProductDto p = new WifiProductDto();
        p.setProductType("DAILY");
        p.setDays(1);
        assertTrue(p.isPeriodSelectable());
        p.setDays(2);
        assertFalse(p.isPeriodSelectable());
        p.setProductType("MONTH"); // 월간은 1개월 고정 (기간 선택 없음)
        p.setDays(1);
        assertFalse(p.isPeriodSelectable());
        p.setProductType("MULTIPLEMONTHS");
        assertFalse(p.isPeriodSelectable());
    }
}
