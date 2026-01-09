package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
@Slf4j
public class ApiUtil {

    @Autowired
    private OkHttpClient okHttpClient;

    private static OkHttpClient staticOkHttpClient;


    @PostConstruct
    public void postConstruct(){
        this.staticOkHttpClient = okHttpClient;
    }

    /**
     * post 호출
     * @param url
     * @param body
     * @param mediaType
     * @return
     */
    public static String post(String url, Map<String, Object> body, MediaType mediaType) {
        RequestBody requestBody = RequestBody.create(new Gson().toJson(body), mediaType);
        Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(response.code() != HttpStatus.OK.value()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }
            ResponseBody body1 = response.body();
            return body1.string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * post 호출
     * @param url
     * @param body
     * @param mediaType
     * @return
     */
    public static String post(String url, Map<String, String> paramHeader, Map<String, Object> body,Map<String, Object> pathParameter, MediaType mediaType) {
        if (ObjectUtils.isEmpty(paramHeader)) {
            throw new IllegalArgumentException("paramHeader is null");
        }
        if (pathParameter != null){
            url = buildPathParameter(url, pathParameter);
        }

        RequestBody requestBody = RequestBody.create(new Gson().toJson(body), mediaType);
        Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .headers(Headers.of(paramHeader))
            .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(response.code() != HttpStatus.OK.value()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }

            return response.body().string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * post 호출
     * @param url
     * @param body
     * @param mediaType
     * @return
     */
    public static String post(String url, Map<String, String> paramHeader, String body,Map<String, Object> pathParameter, MediaType mediaType) {
        if (ObjectUtils.isEmpty(paramHeader)) {
            throw new IllegalArgumentException("paramHeader is null");
        }
        if (pathParameter != null){
            url = buildPathParameter(url, pathParameter);
        }

        RequestBody requestBody = RequestBody.create(mediaType, body);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .headers(Headers.of(paramHeader))
                .addHeader("content-type", "application/json")
                .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(response.code() != HttpStatus.OK.value()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }

            return response.body().string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * post query parameter
     * @param url
     * @param param
     * @return
     */
    public static String postWithQueryParam(String url,  Map<String, Object> param, Map<String, String> paramHeader, boolean encoding) {

        url = buildQueryParameter(url, param, encoding);

        RequestBody requestBody = RequestBody.create(null, new byte[0]);
        Request request = new Request.Builder()
            .url(url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(requestBody)
            .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(response.code() != HttpStatus.OK.value()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }

            return response.body().string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * GET METHOD
     * @param url
     * @return
     */
    public static String get(String url) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(response.code() != HttpStatus.OK.value()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }

            return response.body().string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * GET METHOD with header
     * @param url
     * @return
     */
    public static String get(String url, Map<String, Object> param, Map<String, String> headerMap, boolean encoding) {

        url = buildQueryParameter(url, param, encoding);

        Request request = new Request.Builder()
            .url(url)
            .get()
            .headers(Headers.of(headerMap))
            .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(response.code() != HttpStatus.OK.value()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }

            return response.body().string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * GET METHOD with header, param
     * @param url
     * @return
     */
    public static String get(String url, Map<String, String> headerMap) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .headers(Headers.of(headerMap))
            .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(response.code() != HttpStatus.OK.value()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }

            return response.body().string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * PUT 호출
     * @param url
     * @param body
     * @param mediaType
     * @return
     */
    public static String put(String url, Map<String, Object> body, MediaType mediaType) {
        RequestBody requestBody = RequestBody.create(new Gson().toJson(body), mediaType);
        Request request = new Request.Builder()
            .url(url)
            .put(requestBody)
            .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(!HttpStatus.valueOf(response.code()).is2xxSuccessful()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }
            ResponseBody body1 = response.body();
            return body1.string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * PUT 호출 with header
     * @param url
     * @param paramHeader
     * @param body
     * @param pathParameter
     * @param mediaType
     * @return
     */
    public static String put(String url, Map<String, String> paramHeader, Map<String, Object> body, Map<String, Object> pathParameter, MediaType mediaType) {
        if (ObjectUtils.isEmpty(paramHeader)) {
            throw new IllegalArgumentException("paramHeader is null");
        }
        if (pathParameter != null){
            url = buildPathParameter(url, pathParameter);
        }

        RequestBody requestBody = RequestBody.create(new Gson().toJson(body), mediaType);
        Request request = new Request.Builder()
            .url(url)
            .put(requestBody)
            .headers(Headers.of(paramHeader))
            .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(response.code() != HttpStatus.OK.value()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }

            return response.body().string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * PUT 호출 with header and string body
     * @param url
     * @param paramHeader
     * @param body
     * @param pathParameter
     * @param mediaType
     * @return
     */
    public static String put(String url, Map<String, String> paramHeader, String body, Map<String, Object> pathParameter, MediaType mediaType) {
        if (ObjectUtils.isEmpty(paramHeader)) {
            throw new IllegalArgumentException("paramHeader is null");
        }
        if (pathParameter != null){
            url = buildPathParameter(url, pathParameter);
        }

        RequestBody requestBody = RequestBody.create(mediaType, body);
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .headers(Headers.of(paramHeader))
                .addHeader("content-type", "application/json")
                .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(response.code() != HttpStatus.OK.value()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }

            return response.body().string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * PUT query parameter
     * @param url
     * @param param
     * @param paramHeader
     * @param encoding
     * @return
     */
    public static String putWithQueryParam(String url, Map<String, Object> param, Map<String, String> paramHeader, boolean encoding) {

        url = buildQueryParameter(url, param, encoding);

        RequestBody requestBody = RequestBody.create(null, new byte[0]);
        Request request = new Request.Builder()
            .url(url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .put(requestBody)
            .build();
        try (Response response = staticOkHttpClient.newCall(request).execute()) {
            if(response.code() != HttpStatus.OK.value()){
                throw new RestClientResponseException(response.body().string(), response.code(), HttpStatus.valueOf(response.code()).name(), null, null,null);
            }

            return response.body().string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static String buildQueryParameter(String url, Map<String, Object> parameter, boolean encoding){

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        for (String key : parameter.keySet()) {
            builder.queryParam(key, parameter.get(key));
        }

        return builder.build(encoding).encode().toUriString();
    }

    public static Map<String, Object> postWithRestTemplate(String url, Map<String, String> paramHeader, Map<String, Object> param,Map<String, Object> pathParameter, MediaType mediaType) {
        if (ObjectUtils.isEmpty(paramHeader)) {
            throw new IllegalArgumentException("paramHeader is null");
        }
        if (pathParameter != null){
            url = buildPathParameter(url, pathParameter);
        }
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, String> entry : paramHeader.entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(param, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST,entity,Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = response.getBody();
            return  body;
        }else{
            return null ;
        }

    }

    public static String buildPathParameter(String url, Map<String, Object> pathParameter){

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        return builder.buildAndExpand(pathParameter).toUriString();
    }

}
