package com.naver.naverspabackend.service.wifi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.naver.naverspabackend.dto.WifiDeviceDto;
import com.naver.naverspabackend.dto.WifiMemberDto;
import com.naver.naverspabackend.dto.WifiProductDto;
import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import com.naver.naverspabackend.service.wifi.impl.WifiServiceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** MCC → 나라, 여러 나라 검색 (모두 되는 요금제 / 없으면 가장 많이 되는 요금제) */
class WifiCountriesTest {

    @Test
    void reloadReplacesTableAndEmptyKeepsIt() {
        try {
            WifiCountries.reload(Arrays.asList(new String[] {"999", "XX", "테스트나라"}, new String[] {"-", "GU", "괌"}));
            assertEquals("테스트나라", WifiCountries.of("999").get(0).get("name"));
            assertTrue(WifiCountries.of("440").isEmpty());           // 새 표에 없는 번호
            assertEquals("GU", WifiCountries.normalizeOverride("gu")); // MCC 없는 줄도 나라로
            WifiCountries.reload(new ArrayList<>());                  // 빈 표는 무시
            assertEquals(1, WifiCountries.of("999").size());
        } finally {
            WifiCountries.reload(WifiCountries.tsvRows());            // 다른 테스트를 위해 기본 표로
        }
    }

    @Test
    void unknownMccListed() {
        assertEquals(Arrays.asList("999"), WifiCountries.unknown("440, 999 / 999;450"));
        assertTrue(WifiCountries.unknown(null).isEmpty());
    }

    @Test
    void mccToCountriesDedupedAcrossSeparators() {
        List<Map<String, String>> c = WifiCountries.of("440,441 / 450;234,235,999");
        assertEquals(3, c.size()); // 일본(440·441)·한국·영국(234·235), 999 는 표에 없음
        List<String> isos = new ArrayList<>();
        for (Map<String, String> m : c) isos.add(m.get("iso"));
        assertTrue(isos.containsAll(Arrays.asList("JP", "KR", "GB")));
        assertEquals(0, WifiCountries.of(null).size());
    }

    @Test
    void overrideReplacesMccForGuamSaipan() {
        List<Map<String, String>> c = WifiCountries.of("310", "gu, MP, XX");
        assertEquals(2, c.size()); // 미국 MCC 대신 괌·사이판, 표에 없는 XX 는 버림
        assertEquals("US", WifiCountries.of("310", "  ").get(0).get("iso")); // 비우면 자동
        assertEquals("GU,MP", WifiCountries.normalizeOverride("gu,mp,gu,zz"));
        assertEquals(null, WifiCountries.normalizeOverride(" , "));
        assertTrue(WifiCountries.all().size() > 200);
    }

    @Test
    void tugeCountryCodeListBeatsSharedMcc() {
        List<String[]> rows = Arrays.asList(new String[] {"310", "US", "미국"}, new String[] {"310", "GU", "괌"},
                new String[] {"535", "GU", "괌"});
        List<String[]> before = WifiCountries.tsvRows();
        try {
            WifiCountries.reload(rows);
            assertEquals(2, WifiCountries.of("310").size());                       // 같이 쓰는 번호는 둘 다
            List<Map<String, String>> c = WifiCountries.of("310,535", null, "GU");  // TUGE 가 괌이라고 하면 괌만
            assertEquals(1, c.size());
            assertEquals("GU", c.get(0).get("iso"));
            assertEquals("US", WifiCountries.of("310", "us", "GU").get(0).get("iso")); // 관리자 지정이 최우선
            assertEquals(2, WifiCountries.of("310", null, "").size());              // countryCodeList 없으면 MCC
            assertEquals("KG,CN,ABC", WifiCountries.alpha2("KGZ, cn,ABC"));        // TUGE 가 3자리로 주는 경우
        } finally {
            WifiCountries.reload(before);
        }
    }

    private static WifiProductDto product(String code, String mcc, int price) {
        WifiProductDto p = new WifiProductDto();
        p.setProductCode(code);
        p.setMcc(mcc);
        p.setSaleKrw(price);
        return p;
    }

    private WifiServiceImpl service(List<WifiProductDto> products) {
        WifiMapper mapper = mock(WifiMapper.class);
        WifiPricing pricing = mock(WifiPricing.class);
        when(pricing.categories()).thenReturn(new HashMap<>());
        WifiDeviceDto d = new WifiDeviceDto();
        d.setSn("SN123456");
        d.setMemberId(7L);
        when(mapper.findDevice("SN123456")).thenReturn(d);
        when(mapper.userProductList(isNull(), any())).thenReturn(products);
        WifiServiceImpl s = new WifiServiceImpl();
        ReflectionTestUtils.setField(s, "wifiMapper", mapper);
        ReflectionTestUtils.setField(s, "pricing", pricing);
        return s;
    }

    private static WifiMemberDto member() {
        WifiMemberDto m = new WifiMemberDto();
        m.setId(7L);
        return m;
    }

    @Test
    void fullMatchesOnlyPlansCoveringAllCountries() {
        WifiServiceImpl s = service(new ArrayList<>(Arrays.asList(
            product("EU", "208,214,234", 9000), product("EU-CHEAP", "208,214,234,262", 8000), product("FR", "208", 3000))));
        Map<String, Object> r = s.productsByCountries(member(), "SN123456", Arrays.asList("fr", "GB", "ES"));
        List<?> full = (List<?>) r.get("full");
        assertEquals(2, full.size());
        assertEquals("EU-CHEAP", ((WifiProductDto) full.get(0)).getProductCode()); // 싼 순
        assertTrue(((List<?>) r.get("partial")).isEmpty());
    }

    @Test
    void partialWhenNothingCoversAllSortedByCoverage() {
        WifiServiceImpl s = service(new ArrayList<>(Arrays.asList(
            product("EU", "208,214", 9000), product("FR", "208", 3000), product("JP", "440", 2000))));
        Map<String, Object> r = s.productsByCountries(member(), "SN123456", Arrays.asList("FR", "GB", "ES"));
        assertTrue(((List<?>) r.get("full")).isEmpty());
        List<?> partial = (List<?>) r.get("partial");
        assertEquals(2, partial.size()); // 일본 요금제는 하나도 안 겹쳐서 제외
        WifiProductDto best = (WifiProductDto) partial.get(0);
        assertEquals("EU", best.getProductCode());
        assertEquals(Arrays.asList("GB"), best.getMissing());
    }
}
