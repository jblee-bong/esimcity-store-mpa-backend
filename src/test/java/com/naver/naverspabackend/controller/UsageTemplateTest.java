package com.naver.naverspabackend.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/** 통합 사용량 페이지: 공급사(usageType)별로 켜고 끄는 부분이 맞게 그려지는지 */
class UsageTemplateTest {

    private static String render(String type, String chargeYN, Boolean isDaily) {
        ClassLoaderTemplateResolver r = new ClassLoaderTemplateResolver();
        r.setPrefix("template/cityProd/");
        r.setSuffix(".html");
        r.setTemplateMode(TemplateMode.HTML);
        r.setCharacterEncoding("UTF-8");
        SpringTemplateEngine e = new SpringTemplateEngine();
        e.setTemplateResolver(r);
        Context c = new Context();
        c.setVariable("usageType", type);
        c.setVariable("chargeYN", chargeYN);
        c.setVariable("chargeBtnTxt", "데이터 충전하기");
        c.setVariable("isDaily", isDaily);
        c.setVariable("totalUsage", "3072");
        c.setVariable("usage", "1024");
        return e.process("USAGE", c);
    }

    @Test
    void tsimHasWarningPopupAndCharge() {
        String h = render("TSIM", "Y", null);
        assertTrue(h.contains("id=\"warningPopup\""));
        assertTrue(h.contains("id=\"chargeBtn\""));
        assertTrue(h.contains("var usageType = \"TSIM\""));
    }

    @Test
    void tugeHasChargeWithoutWarning() {
        String h = render("TUGE", "Y", null);
        assertFalse(h.contains("id=\"warningPopup\""));
        assertTrue(h.contains("id=\"chargeBtn\""));
        assertFalse(h.contains("daily상품은 사용량을 제공하지 않습니다"));
    }

    @Test
    void worldmoveHasNoCharge() {
        String h = render("WORLDMOVE", null, null);
        assertFalse(h.contains("id=\"chargeBtn\""));
        assertFalse(h.contains("충전 불가 상품"));
        assertTrue(h.contains("id=\"usage-bar\""));
    }

    @Test
    void esimaccessDailyShowsNoticeInsteadOfBar() {
        String daily = render("ESIMACCESS", "Y", true);
        assertTrue(daily.contains("daily상품은 사용량을 제공하지 않습니다"));
        assertFalse(daily.contains("id=\"usage-bar\""));
        String total = render("ESIMACCESS", "Y", false);
        assertFalse(total.contains("daily상품은 사용량을 제공하지 않습니다"));
        assertTrue(total.contains("id=\"usage-bar\""));
    }
}
