package com.naver.naverspabackend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.config.NaverSearchAdProperties;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverSearchAdClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=UTF-8");

    private final NaverSearchAdProperties properties;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getKeyword(String nccKeywordId) throws Exception {
        String uri = "/ncc/keywords/" + nccKeywordId;
        String body = execute("GET", uri, null, null);
        return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * fields=bidAmt 로 키워드 입찰가 갱신
     */
    public List<Map<String, Object>> updateKeywordBid(Map<String, Object> keywordPayload) throws Exception {
        String uri = "/ncc/keywords";
        Map<String, String> query = new HashMap<String, String>();
        query.put("fields", "bidAmt");
        String body = execute("PUT", uri, query, Collections.singletonList(keywordPayload));
        return objectMapper.readValue(body, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    /**
     * 목표 순위에 필요한 추정 입찰가 조회 (파워링크 키워드).
     * 네이버 API 제약: position 은 1~4 만 허용.
     */
    public Integer estimateAveragePositionBid(String keyword, int position, String device) throws Exception {
        int safePosition = Math.min(4, Math.max(1, position));
        String uri = "/estimate/average-position-bid/keyword";
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("key", keyword);
        item.put("position", safePosition);

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("device", device);
        payload.put("items", Collections.singletonList(item));

        String body = execute("POST", uri, null, payload);
        return parseEstimateBid(body, keyword);
    }

    public Map<String, Object> getAd(String nccAdId) throws Exception {
        String uri = "/ncc/ads/" + nccAdId;
        String body = execute("GET", uri, null, null);
        return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 쇼핑검색 소재 입찰 변경: PUT /ncc/ads/{id}?fields=adAttr
     */
    public Map<String, Object> updateAdBid(String nccAdId, int bidAmt) throws Exception {
        Map<String, Object> ad = getAd(nccAdId);
        Map<String, Object> adAttr = new HashMap<String, Object>();
        Object existingAttr = ad.get("adAttr");
        if (existingAttr instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> existingMap = (Map<String, Object>) existingAttr;
            adAttr.putAll(existingMap);
        }
        adAttr.put("bidAmt", bidAmt);
        adAttr.put("useGroupBidAmt", false);
        ad.put("adAttr", adAttr);

        String uri = "/ncc/ads/" + nccAdId;
        Map<String, String> query = new HashMap<String, String>();
        query.put("fields", "adAttr");
        String body = execute("PUT", uri, query, ad);
        return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 쇼핑검색(NPLA) 소재 목표순위 추정입찰가.
     * POST /npla-estimate/average-position-bid/id
     */
    public Integer estimateNplaAveragePositionBid(String nccAdId, int position, String device) throws Exception {
        String uri = "/npla-estimate/average-position-bid/id";
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("key", nccAdId);
        item.put("position", position);

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("device", device);
        payload.put("items", Collections.singletonList(item));

        String body = execute("POST", uri, null, payload);
        return parseNplaEstimateBid(body, nccAdId);
    }

    /**
     * 소재/키워드 통계. avgRnk=기간 평균노출순위.
     */
    public Map<String, Object> getStats(String id, String since, String until) throws Exception {
        Map<String, String> query = new HashMap<String, String>();
        query.put("ids", id);
        query.put("fields",
            "[\"impCnt\",\"clkCnt\",\"ctr\",\"cpc\",\"avgRnk\",\"recentAvgRnk\",\"salesAmt\"]");
        query.put("timeRange", "{\"since\":\"" + since + "\",\"until\":\"" + until + "\"}");
        return parseStatsResponse(execute("GET", "/stats", query, null));
    }

    /**
     * datePreset 통계 (today / yesterday 등). 당일 누적 avgRnk에 사용.
     */
    public Map<String, Object> getStatsByDatePreset(String id, String datePreset) throws Exception {
        Map<String, String> query = new HashMap<String, String>();
        query.put("ids", id);
        query.put("fields",
            "[\"impCnt\",\"clkCnt\",\"ctr\",\"cpc\",\"avgRnk\",\"recentAvgRnk\",\"salesAmt\"]");
        query.put("datePreset", datePreset);
        return parseStatsResponse(execute("GET", "/stats", query, null));
    }

    /**
     * 쇼핑검색 소재의 유입 검색어 통계.
     * GET /stats?id={nad-...}&statType=NPLA_SCH_KEYWORD
     */
    public List<Map<String, Object>> getShoppingKeywordStats(String nccAdId, String since, String until)
        throws Exception {
        Map<String, String> query = new HashMap<String, String>();
        query.put("id", nccAdId);
        query.put("statType", "NPLA_SCH_KEYWORD");
        query.put("fields",
            "[\"impCnt\",\"clkCnt\",\"ctr\",\"cpc\",\"avgRnk\",\"salesAmt\"]");
        query.put("timeRange", "{\"since\":\"" + since + "\",\"until\":\"" + until + "\"}");
        String body = execute("GET", "/stats", query, null);
        JsonNode root = objectMapper.readTree(body);
        if (root.isArray()) {
            return objectMapper.convertValue(root, new TypeReference<List<Map<String, Object>>>() {
            });
        }
        if (root.has("data") && root.get("data").isArray()) {
            return objectMapper.convertValue(root.get("data"), new TypeReference<List<Map<String, Object>>>() {
            });
        }
        List<Map<String, Object>> one = new java.util.ArrayList<Map<String, Object>>();
        one.add(objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
        }));
        return one;
    }

    /**
     * 대용량 보고서 생성. 예: reportTp=SHOPPINGKEYWORD_DETAIL, statDt=yyyy-MM-dd (전일까지)
     * POST /stat-reports
     */
    public Map<String, Object> createStatReport(String reportTp, String statDt) throws Exception {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("reportTp", reportTp);
        payload.put("statDt", statDt);
        String body = execute("POST", "/stat-reports", null, payload);
        return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
        });
    }

    /** GET /stat-reports — 최근 보고서 job 목록 */
    public List<Map<String, Object>> listStatReports() throws Exception {
        String body = execute("GET", "/stat-reports", null, null);
        JsonNode root = objectMapper.readTree(body);
        if (root.isArray()) {
            return objectMapper.convertValue(root, new TypeReference<List<Map<String, Object>>>() {
            });
        }
        return Collections.emptyList();
    }

    /** GET /stat-reports/{jobId} */
    public Map<String, Object> getStatReport(String reportJobId) throws Exception {
        String body = execute("GET", "/stat-reports/" + reportJobId, null, null);
        return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 보고서 파일이 준비될 때까지 폴링 후 downloadUrl 로 TSV/텍스트 일부 반환.
     */
    public String waitAndReadStatReport(String reportJobId, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + Math.max(30_000L, timeoutMs);
        Map<String, Object> job = null;
        while (System.currentTimeMillis() < deadline) {
            job = getStatReport(reportJobId);
            Object status = job.get("status");
            log.info("stat-report jobId={} status={} downloadUrl?={}",
                reportJobId, status, job.get("downloadUrl") != null);
            if (status != null) {
                String s = String.valueOf(status);
                if ("BUILT".equalsIgnoreCase(s) || "SUCCESS".equalsIgnoreCase(s)
                    || "COMPLETED".equalsIgnoreCase(s)) {
                    break;
                }
                if ("ERROR".equalsIgnoreCase(s) || "NONE".equalsIgnoreCase(s)
                    || "FAILED".equalsIgnoreCase(s)) {
                    throw new IllegalStateException("stat-report failed: " + job);
                }
            }
            Thread.sleep(5000L);
        }
        if (job == null) {
            throw new IllegalStateException("stat-report empty job");
        }
        Object urlObj = job.get("downloadUrl");
        if (urlObj == null) {
            throw new IllegalStateException("stat-report no downloadUrl yet: " + job);
        }
        return downloadReportAsText(String.valueOf(urlObj), 200);
    }

    /**
     * 보고서 다운로드. 서명 path 는 /report-download 고정.
     * TSV/텍스트면 앞 maxLines 줄, zip/gz 이면 압축 해제 후 앞줄.
     */
    public String downloadReportAsText(String downloadUrl, int maxLines) throws Exception {
        long ts = System.currentTimeMillis();
        String timestamp = String.valueOf(ts);
        String signature = NaverSearchAdSignatureHelper.generate(
            timestamp, "GET", "/report-download", properties.getSecretKey());

        Request request = new Request.Builder()
            .url(downloadUrl)
            .get()
            .header("X-Timestamp", timestamp)
            .header("X-API-KEY", properties.getAccessLicense())
            .header("X-Customer", properties.getCustomerId())
            .header("X-Signature", signature)
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IllegalStateException(
                    "report download failed status=" + response.code());
            }
            byte[] bytes = response.body().bytes();
            return extractTextPreview(bytes, maxLines);
        }
    }

    private static String extractTextPreview(byte[] bytes, int maxLines) throws Exception {
        java.io.InputStream in = new java.io.ByteArrayInputStream(bytes);
        // gzip
        if (bytes.length >= 2 && (bytes[0] == (byte) 0x1f) && (bytes[1] == (byte) 0x8b)) {
            in = new java.util.zip.GZIPInputStream(in);
        } else if (bytes.length >= 2 && bytes[0] == 'P' && bytes[1] == 'K') {
            java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(in);
            java.util.zip.ZipEntry entry;
            java.io.InputStream entryIn = null;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    entryIn = zis;
                    break;
                }
            }
            if (entryIn == null) {
                return "(zip empty)";
            }
            in = entryIn;
        }
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            int n = 0;
            while ((line = reader.readLine()) != null && n < maxLines) {
                sb.append(line).append('\n');
                n++;
            }
            sb.append("... (").append(n).append(" lines preview)");
        }
        return sb.toString();
    }

    private Map<String, Object> parseStatsResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        if (root.isArray() && root.size() > 0) {
            return objectMapper.convertValue(root.get(0), new TypeReference<Map<String, Object>>() {
            });
        }
        if (root.has("data") && root.get("data").isArray()) {
            if (root.get("data").size() == 0) {
                return new HashMap<String, Object>();
            }
            return objectMapper.convertValue(root.get("data").get(0), new TypeReference<Map<String, Object>>() {
            });
        }
        return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 통계 조회용 완료일 기준 구간 (어제 기준).
     */
    public static LocalDate[] statsDateRange(int statsDays) {
        int days = Math.max(1, statsDays);
        LocalDate until = LocalDate.now().minusDays(1);
        LocalDate since = until.minusDays(days - 1L);
        return new LocalDate[] {since, until};
    }

    public static int extractAdBidAmt(Map<String, Object> ad) {
        if (ad == null) {
            return 0;
        }
        Object adAttr = ad.get("adAttr");
        if (adAttr instanceof Map) {
            Object bid = ((Map<?, ?>) adAttr).get("bidAmt");
            if (bid instanceof Number) {
                return ((Number) bid).intValue();
            }
            if (bid != null) {
                try {
                    return Integer.parseInt(String.valueOf(bid));
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }
        }
        Object topBid = ad.get("bidAmt");
        if (topBid instanceof Number) {
            return ((Number) topBid).intValue();
        }
        return 0;
    }

    private Integer parseEstimateBid(String body, String key) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode estimate = root.path("estimate");
        if (estimate.isMissingNode() || !estimate.isArray() || estimate.size() == 0) {
            if (root.isArray() && root.size() > 0) {
                return readBidNode(root.get(0));
            }
            log.warn("average-position-bid empty response for key={}, body={}", key, body);
            return null;
        }
        Integer bid = readBidNode(estimate.get(0));
        if (bid == null) {
            log.warn("average-position-bid parse fail key={}, body={}", key, body);
        }
        return bid;
    }

    private Integer parseNplaEstimateBid(String body, String nccAdId) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode items = root.path("items");
        if (!items.isArray() || items.size() == 0) {
            items = root.path("estimate");
        }
        if (!items.isArray() || items.size() == 0) {
            log.warn("npla average-position-bid empty for ad={}, body={}", nccAdId, body);
            return null;
        }
        Integer maxBid = null;
        for (JsonNode node : items) {
            Integer bid = readBidNode(node);
            if (bid == null) {
                continue;
            }
            if (maxBid == null || bid > maxBid) {
                maxBid = bid;
            }
        }
        if (maxBid == null) {
            log.warn("npla average-position-bid parse fail ad={}, body={}", nccAdId, body);
        }
        return maxBid;
    }

    private static Integer readBidNode(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.has("bid") && node.get("bid").isNumber()) {
            return node.get("bid").asInt();
        }
        if (node.has("bidAmt") && node.get("bidAmt").isNumber()) {
            return node.get("bidAmt").asInt();
        }
        JsonNode found = node.findValue("bid");
        if (found != null && found.isNumber()) {
            return found.asInt();
        }
        return null;
    }

    private String execute(String method, String uri, Map<String, String> query, Object jsonBody) throws Exception {
        long ts = System.currentTimeMillis();
        String timestamp = String.valueOf(ts);
        String signature = NaverSearchAdSignatureHelper.generate(
            timestamp, method, uri, properties.getSecretKey());

        HttpUrl.Builder urlBuilder = HttpUrl.parse(properties.getBaseUrl() + uri).newBuilder();
        if (query != null) {
            for (Map.Entry<String, String> e : query.entrySet()) {
                urlBuilder.addQueryParameter(e.getKey(), e.getValue());
            }
        }

        Request.Builder reqBuilder = new Request.Builder()
            .url(urlBuilder.build())
            .header("Content-Type", "application/json; charset=UTF-8")
            .header("X-Timestamp", timestamp)
            .header("X-API-KEY", properties.getAccessLicense())
            .header("X-Customer", properties.getCustomerId())
            .header("X-Signature", signature);

        if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            reqBuilder.method(method, null);
        } else {
            String json = jsonBody == null ? "{}" : objectMapper.writeValueAsString(jsonBody);
            reqBuilder.method(method, RequestBody.create(json, JSON));
        }

        try (Response response = okHttpClient.newCall(reqBuilder.build()).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IllegalStateException(
                    "Naver Search Ad API failed status=" + response.code()
                        + " method=" + method + " uri=" + uri + " body=" + respBody);
            }
            return respBody;
        }
    }
}
