package com.naver.naverspabackend.service.wifi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * TUGE 상품의 MCC 목록 → 나라(ISO, 한국어 이름). 표: resources/wifi/mcc_countries.tsv (ITU E.212).
 * 괌·사이판처럼 미국 MCC 를 같이 쓰는 곳은 MCC 로 구분이 안 돼서, 관리자가 상품별로 나라를 직접 지정(country_override).
 * 운영에서는 서버 시작·관리자 수정 때 DB(TB_WIFI_MCC)로 표를 통째로 바꿈(reload). TSV 는 DB 가 비었거나 못 읽을 때의 기본값.
 */
@Slf4j
public final class WifiCountries {

    /** mcc → [{iso, name}] (310 = 미국·괌·사이판... 처럼 한 번호를 여러 나라가 같이 씀) */
    static volatile Map<String, List<String[]>> BY_MCC = new HashMap<>();
    /** iso → name (MCC 없는 괌 등 포함) */
    static volatile Map<String, String> BY_ISO = new HashMap<>();

    static {
        load();
    }

    private WifiCountries() {
    }

    /** 기본 표 (TSV) 행들: [mcc, iso, name] */
    public static List<String[]> tsvRows() {
        List<String[]> rows = new ArrayList<>();
        try (InputStream in = WifiCountries.class.getResourceAsStream("/wifi/mcc_countries.tsv")) {
            if (in == null) return rows;
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                String[] c = line.split("\t");
                if (c.length >= 3) rows.add(new String[] {c[0].trim(), c[1].trim(), c[2].trim()});
            }
        } catch (Exception e) {
            log.error("[WIFI] MCC 표 읽기 실패", e);
        }
        return rows;
    }

    private static void load() {
        reload(tsvRows());
    }

    /** 표 통째로 교체: 행 [mcc('-' 이면 MCC 없음), iso, name]. 비어 있으면 그대로 둠 */
    public static synchronized void reload(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) return;
        Map<String, List<String[]>> m = new HashMap<>();
        Map<String, String> iso = new HashMap<>();
        for (String[] c : rows) {
            iso.putIfAbsent(c[1], c[2]);
            if (!"-".equals(c[0])) m.computeIfAbsent(c[0], k -> new ArrayList<>()).add(new String[] {c[1], c[2]});
        }
        BY_ISO = iso;
        BY_MCC = m;
    }

    /**
     * 나라 판단 순서: 관리자 직접 지정 → TUGE countryCodeList(ISO) → MCC.
     * MCC 는 310 처럼 여러 나라가 같이 써서 괌 상품이 미국·버뮤다로도 잡히므로 마지막 수단.
     */
    public static List<Map<String, String>> of(String mcc, String override, String countryCodeList) {
        if (override != null && !override.trim().isEmpty()) return of(mcc, override);
        List<Map<String, String>> byTuge = of(null, alpha2(countryCodeList));
        return byTuge.isEmpty() ? of(mcc) : byTuge;
    }

    /** 관리자가 직접 지정한 나라(ISO 콤마)가 있으면 그것, 없으면 MCC 로 자동 */
    public static List<Map<String, String>> of(String mcc, String override) {
        if (override == null || override.trim().isEmpty()) return of(mcc);
        Map<String, Map<String, String>> byIso = new LinkedHashMap<>();
        for (String iso : override.toUpperCase().split("[^A-Z]+")) {
            if (iso.length() != 2 || !BY_ISO.containsKey(iso)) continue;
            Map<String, String> v = new LinkedHashMap<>();
            v.put("iso", iso);
            v.put("name", BY_ISO.get(iso));
            byIso.putIfAbsent(iso, v);
        }
        List<Map<String, String>> list = new ArrayList<>(byIso.values());
        list.sort((a, b) -> a.get("name").compareTo(b.get("name")));
        return list;
    }

    /** 관리자 나라 선택용 전체 목록 [{iso, name}] */
    public static List<Map<String, String>> all() {
        List<Map<String, String>> list = new ArrayList<>();
        for (Map.Entry<String, String> e : BY_ISO.entrySet()) {
            Map<String, String> v = new LinkedHashMap<>();
            v.put("iso", e.getKey());
            v.put("name", e.getValue());
            list.add(v);
        }
        list.sort((a, b) -> a.get("name").compareTo(b.get("name")));
        return list;
    }

    /** 저장 전 검증: 표에 있는 ISO 만 남겨 콤마로. 비면 null (= 자동) */
    public static String normalizeOverride(String override) {
        if (override == null) return null;
        List<String> out = new ArrayList<>();
        for (String iso : override.toUpperCase().split("[^A-Z]+")) {
            if (iso.length() == 2 && BY_ISO.containsKey(iso) && !out.contains(iso)) out.add(iso);
        }
        return out.isEmpty() ? null : String.join(",", out);
    }

    /** ISO 3자리 → 2자리 (Java 내장 국가표) */
    private static final Map<String, String> ISO3_TO_2 = new HashMap<>();
    static {
        for (String a2 : java.util.Locale.getISOCountries()) {
            try {
                ISO3_TO_2.put(new java.util.Locale("", a2).getISO3Country(), a2);
            } catch (java.util.MissingResourceException ignore) {
            }
        }
    }

    /** TUGE countryCodeList 정리: 대부분 "CN" 처럼 2자리인데 키르기스스탄은 "KGZ" 로 옴 → 2자리로 바꿈. 모르는 3자리는 그대로(경고로 보임) */
    static String alpha2(String codes) {
        if (codes == null) return null;
        List<String> out = new ArrayList<>();
        for (String c : codes.toUpperCase().split("[^A-Z]+")) {
            if (c.isEmpty()) continue;
            out.add(c.length() == 3 && ISO3_TO_2.containsKey(c) ? ISO3_TO_2.get(c) : c);
        }
        return String.join(",", out);
    }

    /** 관리자 경고용: countryCodeList 가 있으면 표에 없는 ISO, 없으면 표에 없는 MCC */
    public static List<String> unknown(String mcc, String countryCodeList) {
        if (countryCodeList == null || countryCodeList.trim().isEmpty()) return unknown(mcc);
        List<String> out = new ArrayList<>();
        for (String iso : alpha2(countryCodeList).split("[^A-Z]+")) {
            if (!iso.isEmpty() && !BY_ISO.containsKey(iso) && !out.contains(iso)) out.add(iso);
        }
        return out;
    }

    /** 표에 없는 MCC 번호들 (관리자 화면 경고용). 없으면 빈 목록 */
    public static List<String> unknown(String mcc) {
        List<String> out = new ArrayList<>();
        if (mcc == null) return out;
        for (String code : mcc.split("[^0-9]+")) {
            if (!code.isEmpty() && !BY_MCC.containsKey(code) && !out.contains(code)) out.add(code);
        }
        return out;
    }

    /** "208,262 / 214" 같은 MCC 문자열 → 나라 목록 [{iso, name}] (중복 제거, 이름순). 표에 없는 번호는 건너뛰고 로그 */
    public static List<Map<String, String>> of(String mcc) {
        if (mcc == null) return Collections.emptyList();
        Map<String, Map<String, String>> byIso = new LinkedHashMap<>();
        for (String code : mcc.split("[^0-9]+")) {
            if (code.isEmpty()) continue;
            List<String[]> cs = BY_MCC.get(code);
            if (cs == null) {
                log.warn("[WIFI] MCC 표에 없는 번호: {}", code);
                continue;
            }
            for (String[] c : cs) {
                Map<String, String> v = new LinkedHashMap<>();
                v.put("iso", c[0]);
                v.put("name", c[1]);
                byIso.putIfAbsent(c[0], v);
            }
        }
        List<Map<String, String>> list = new ArrayList<>(byIso.values());
        list.sort((a, b) -> a.get("name").compareTo(b.get("name")));
        return list;
    }
}
