package com.naver.naverspabackend.service.esiminstall;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.naver.naverspabackend.dto.EsimInstallTokenDto;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.mybatis.mapper.EsimInstallTokenMapper;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EsimInstallTokenService {

    private static final int EXPIRE_DAYS = 30;
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private final EsimInstallTokenMapper esimInstallTokenMapper;

    @Value("${server-origin}")
    private String serverOrigin;

    public boolean hasInstallPayload(Map<String, Object> params) {
        if (params == null) {
            return false;
        }
        String qr = Objects.toString(params.get("qrImageUrl"), "");
        String lpa = Objects.toString(params.get("lpaCode"), "");
        String activation = Objects.toString(params.get("activation_code"), "");
        return !qr.isEmpty() || !lpa.isEmpty() || !activation.isEmpty();
    }

    /** 알림톡/메일용 토큰 발급. installToken / installUrl 을 params에 넣는다. */
    public void attachInstallToken(Map<String, Object> params, OrderDto orderDto) {
        if (!hasInstallPayload(params)) {
            params.put("installToken", "");
            params.put("installUrl", "");
            return;
        }
        // 이미 발급된 경우 재생성하지 않음 (메일→알림톡 순서에서 공유)
        if (!Objects.toString(params.get("installToken"), "").isEmpty()
                && !Objects.toString(params.get("installUrl"), "").isEmpty()) {
            return;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> payload = buildPayload(params);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, EXPIRE_DAYS);

        Map<String, Object> insert = new HashMap<>();
        insert.put("token", token);
        insert.put("orderId", orderDto != null ? orderDto.getId() : null);
        insert.put("payloadJson", new Gson().toJson(payload));
        insert.put("expireDt", cal.getTime());
        esimInstallTokenMapper.insert(insert);

        params.put("installToken", token);
        // 운영 nginx 는 /api/* 만 백엔드로 넘김 (/external 직접은 404). 알림톡 버튼 링크와 같은 주소
        params.put("installUrl", serverOrigin + "/api/external/esim/install?token=" + token);
    }

    public Map<String, Object> findPayloadByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("token", token.trim());
        EsimInstallTokenDto dto = esimInstallTokenMapper.selectValidByToken(param);
        if (dto == null || dto.getPayloadJson() == null) {
            return null;
        }
        Map<String, Object> payload = new Gson().fromJson(dto.getPayloadJson(), MAP_TYPE);
        enrichInstallLinks(payload);
        return payload;
    }

    private Map<String, Object> buildPayload(Map<String, Object> params) {
        Map<String, Object> payload = new HashMap<>();
        put(payload, params, "ordererName");
        put(payload, params, "orderTitle");
        put(payload, params, "orderRealName");
        put(payload, params, "productName");
        put(payload, params, "qrImageUrl");
        put(payload, params, "smdp");
        put(payload, params, "iccid");
        put(payload, params, "lpaCode");
        put(payload, params, "activation_code");
        put(payload, params, "eSimMResetInfo");
        put(payload, params, "eSimMApnInfo");
        put(payload, params, "eSimMChargeInfo");
        put(payload, params, "eSimResetInfo");
        put(payload, params, "eSimApnInfo");
        put(payload, params, "eSimChargeInfo");
        put(payload, params, "installStartsPeriod");

        String activation = Objects.toString(params.get("activation_code"), "");
        String lpa = Objects.toString(params.get("lpaCode"), "");
        payload.put("ipActivityCode", activation);
        payload.put("gaActivityCode", lpa);
        enrichInstallLinks(payload);
        return payload;
    }

    private void enrichInstallLinks(Map<String, Object> payload) {
        String lpa = Objects.toString(payload.get("lpaCode"), "");
        if (lpa.isEmpty()) {
            String smdp = Objects.toString(payload.get("smdp"), "");
            String activation = Objects.toString(payload.get("activation_code"),
                    Objects.toString(payload.get("ipActivityCode"), ""));
            if (!smdp.isEmpty() && !activation.isEmpty()) {
                lpa = "LPA:1$" + smdp + "$" + activation;
                payload.put("lpaCode", lpa);
            }
        }
        if (lpa.isEmpty()) {
            payload.put("iosInstallUrl", "");
            payload.put("androidInstallUrl", "");
            return;
        }
        try {
            String encoded = URLEncoder.encode(lpa, StandardCharsets.UTF_8.name());
            payload.put("iosInstallUrl",
                    "https://esimsetup.apple.com/esim_qrcode_provisioning?carddata=" + encoded);
            payload.put("androidInstallUrl",
                    "https://esimsetup.android.com/esim_qrcode_provisioning?carddata=" + encoded);
        } catch (Exception e) {
            payload.put("iosInstallUrl", "");
            payload.put("androidInstallUrl", "");
        }
    }

    private void put(Map<String, Object> target, Map<String, Object> source, String key) {
        target.put(key, Objects.toString(source.get(key), ""));
    }
}
