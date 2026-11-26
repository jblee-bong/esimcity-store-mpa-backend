package com.naver.naverspabackend.service.wifi.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** 데일리 하루 고속 용량: TUGE ruleDesc → 상품 이름 순으로 읽기 */
class WifiDailyMbTest {
    @Test
    void 규칙과_이름에서_용량_읽기() {
        assertEquals(1024, WifiAdminServiceImpl.dailyMbOf("1GB of high-speed data per day", "x"));
        assertEquals(500, WifiAdminServiceImpl.dailyMbOf("500MB high-speed daily, then 128kbps", null));
        assertEquals(1536, WifiAdminServiceImpl.dailyMbOf(null, "Japan 1.5GB/Day"));
        assertEquals(1024, WifiAdminServiceImpl.dailyMbOf("", "D-003-S-D-1G(ZD)"));
        assertNull(WifiAdminServiceImpl.dailyMbOf("unlimited", "Daily plan"));
        assertEquals(2048, WifiAdminServiceImpl.dailyMbOf(null, "Japan 5G 2GB/day")); // 5G 는 통신 세대
        assertNull(WifiAdminServiceImpl.dailyMbOf("4G LTE unlimited", "Korea 5G daily"));
    }

    @Test
    void 운영_highSpeed_필드_읽기() {
        assertEquals(2048, WifiAdminServiceImpl.highSpeedMb("2048(M)"));
        assertEquals(3072, WifiAdminServiceImpl.highSpeedMb("3(G)"));
        assertEquals(500, WifiAdminServiceImpl.highSpeedMb("512000(K)"));
        assertNull(WifiAdminServiceImpl.highSpeedMb(""));
        assertNull(WifiAdminServiceImpl.highSpeedMb("unlimited"));
    }
}
