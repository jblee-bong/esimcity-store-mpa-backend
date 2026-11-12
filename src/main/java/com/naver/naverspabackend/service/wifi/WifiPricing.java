package com.naver.naverspabackend.service.wifi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.naver.naverspabackend.dto.CodeDto;
import com.naver.naverspabackend.dto.WifiCategoryDto;
import com.naver.naverspabackend.dto.WifiProductDto;
import com.naver.naverspabackend.mybatis.mapper.CodeMapper;
import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 판매가 = 원가(USD) × 1달러당 금액 × 일수 × (1 + 일수별 마진율), 100원 단위 올림.
 * 정책은 나라 → 상위 지역 → 코드관리(WIFI RATE / MARGIN) 순으로 비어 있지 않은 값을 사용.
 * 일수별 마진율은 1일 데일리에만 적용 (월간·고정 상품은 기본 마진율). 오래 쓸수록 총액이 줄어들지 않게 보정.
 */
@Component
public class WifiPricing {

    static final double DEFAULT_RATE = 1450;
    static final double DEFAULT_MARGIN = 30;

    @Autowired
    private CodeMapper codeMapper;
    @Autowired
    private WifiMapper wifiMapper;

    public static class Policy {
        public final double usdRate;
        public final double margin;
        public final TreeMap<Integer, Double> tiers; // from 일수 → 마진율

        public Policy(double usdRate, double margin, TreeMap<Integer, Double> tiers) {
            this.usdRate = usdRate;
            this.margin = margin;
            this.tiers = tiers;
        }

        double marginFor(int days) {
            Map.Entry<Integer, Double> e = tiers.floorEntry(days);
            return e == null ? margin : e.getValue();
        }
    }

    /** 카테고리 전체를 한 번 읽어 둔 맵 (목록 계산 시 재사용) */
    public Map<Long, WifiCategoryDto> categories() {
        Map<Long, WifiCategoryDto> m = new HashMap<>();
        for (WifiCategoryDto c : wifiMapper.adminCategoryList()) m.put(c.getId(), c);
        return m;
    }

    public Policy policy(Long categoryId, Map<Long, WifiCategoryDto> cats) {
        Double rate = null;
        Double margin = null;
        String rules = null;
        WifiCategoryDto c = categoryId == null ? null : cats.get(categoryId);
        for (int depth = 0; c != null && depth < 2; depth++) { // 나라 → 지역
            if (rate == null && c.getUsdRate() != null && c.getUsdRate() > 0) rate = c.getUsdRate().doubleValue();
            if (margin == null && c.getMarginRate() != null) margin = c.getMarginRate();
            if (rules == null && StringUtils.isNotBlank(c.getPeriodRules())) rules = c.getPeriodRules();
            c = c.getParentId() == null ? null : cats.get(c.getParentId());
        }
        return new Policy(rate != null ? rate : code("RATE", DEFAULT_RATE),
            margin != null ? margin : code("MARGIN", DEFAULT_MARGIN), parseRules(rules));
    }

    /** 목록용 단가(1일·1개월 또는 고정 가격)를 채움 */
    public void fillUnitPrice(WifiProductDto p, Map<Long, WifiCategoryDto> cats) {
        int[] t = priceTable(p, policy(p.getCategoryId(), cats), 1);
        p.setSaleKrw(t == null ? null : t[0]);
    }

    /** 상세용 기간별 총액 + 단가 */
    public void fillPrices(WifiProductDto p) {
        int[] t = priceTable(p, policy(p.getCategoryId(), categories()), maxPeriod(p));
        p.setPrices(t);
        p.setSaleKrw(t == null ? null : t[0]);
    }

    /** 주문 금액. 범위 밖이거나 원가가 없으면 null */
    public Integer price(WifiProductDto p, int period) {
        if (period < 1 || period > maxPeriod(p)) return null;
        int[] t = priceTable(p, policy(p.getCategoryId(), categories()), period);
        return t == null ? null : t[period - 1];
    }

    private static int maxPeriod(WifiProductDto p) {
        return p.isPeriodSelectable() ? p.getMaxPeriod() : 1;
    }

    /** 1..upTo 일(월)의 총액. 원가 없으면 null */
    public static int[] priceTable(WifiProductDto p, Policy pol, int upTo) {
        if (p.getSettlementUsd() == null || upTo < 1) return null;
        double unitCost = p.getSettlementUsd().doubleValue() * pol.usdRate;
        boolean tiered = p.isPeriodSelectable() && "DAILY".equals(p.getProductType());
        int[] t = new int[upTo];
        for (int n = 1; n <= upTo; n++) {
            double m = tiered ? pol.marginFor(n) : pol.margin;
            int v = (int) (Math.ceil(unitCost * n * (1 + m / 100) / 100 - 1e-9) * 100);
            t[n - 1] = n > 1 ? Math.max(v, t[n - 2]) : v; // 구간이 바뀌어도 더 긴 기간이 더 싸지지 않게
        }
        return t;
    }

    /** [{"from":7,"rate":25}] → {7:25}. 잘못된 값은 무시 */
    public static TreeMap<Integer, Double> parseRules(String json) {
        TreeMap<Integer, Double> m = new TreeMap<>();
        if (StringUtils.isBlank(json)) return m;
        try {
            List<Map<String, Object>> list = new Gson().fromJson(json, new TypeToken<List<Map<String, Object>>>() {}.getType());
            for (Map<String, Object> r : list) {
                int from = ((Number) r.get("from")).intValue();
                double rate = ((Number) r.get("rate")).doubleValue();
                if (from >= 2 && rate >= 0) m.put(from, rate);
            }
        } catch (Exception ignored) {
            // 저장 시 검증하므로 여기선 무시
        }
        return m;
    }

    static final double DEFAULT_EARN_RATE = 3;

    /** 결제 적립률(%) - 코드관리 WIFI EARN_RATE */
    public double earnRate() {
        return code("EARN_RATE", DEFAULT_EARN_RATE);
    }

    /** 적립 포인트 = 결제금액 x 적립률, 10P 단위 버림 */
    public static int earnAmount(int payKrw, double rate) {
        if (payKrw <= 0 || rate <= 0) return 0;
        return (int) (Math.floor(payKrw * rate / 100 / 10 + 1e-9) * 10);
    }

    private double code(String cd, double def) {
        Map<String, Object> q = new HashMap<>();
        q.put("codeGroup", "WIFI");
        q.put("codeCd", cd);
        CodeDto c = codeMapper.fetch(q);
        try {
            return c == null ? def : Double.parseDouble(c.getCodeValue());
        } catch (Exception e) {
            return def;
        }
    }
}
