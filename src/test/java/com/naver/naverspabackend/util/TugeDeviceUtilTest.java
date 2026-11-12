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
    void periodSelectableOnlyForOneDayDailyOrOneMonth() {
        WifiProductDto p = new WifiProductDto();
        p.setProductType("DAILY");
        p.setDays(1);
        assertTrue(p.isPeriodSelectable());
        p.setDays(2);
        assertFalse(p.isPeriodSelectable());
        p.setProductType("MONTH");
        p.setDays(1);
        assertTrue(p.isPeriodSelectable());
        p.setProductType("MULTIPLEMONTHS");
        assertFalse(p.isPeriodSelectable());
    }
}
