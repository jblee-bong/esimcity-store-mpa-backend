package com.naver.naverspabackend.util;

import com.naver.naverspabackend.dto.OrderDto;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;

public class CommonUtil {

    public static void setPageIntoMap(Map<String, Object> paramMap, PagingUtil page, int totalCnt){
        if(page != null && page.getCurrentPageNo() > -1){
            page.setTotalRecordCount(totalCnt);
            paramMap.put("firstIndex", page.getFirstRecordIndex());
            paramMap.put("recordCountPerPage", page.getRecordCountPerPage());
        }
    }

    /**
     * 현재 시간 기준으로
     * 계산 단위 (-) 도 가능
     * 계산 범주 chronoUnit
     * @param diff
     * @param chronoUnit
     * @return
     */
    public static String naverFormatDate(int diff, ChronoUnit chronoUnit){

        LocalDateTime currentTime = LocalDateTime.now();
        // 현재 시간 가져오기
        currentTime = currentTime.plus(diff, chronoUnit);


        // 현재 날짜와 시간을 KST(UTC+09:00)로 설정
        ZonedDateTime now = ZonedDateTime.of(currentTime, java.time.ZoneId.of("Asia/Seoul"));

        // 네이버 포맷 형식으로 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return now.format(formatter);
    }

    public static Date parseNaverFormatDate(String dateString){

        // SimpleDateFormat 객체 생성
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date date = null;
        try {
            // TimeZone 설정 (원하는 TimeZone으로 변경)
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
            dateFormat.setTimeZone(timeZone);

            // 문자열을 Date로 변환
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public static Date parseCoupangFormatDate(String dateString){

        // SimpleDateFormat 객체 생성
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DDThh:mm:ss.ssssss±hh:mm");
        Date date = null;
        try {
            // TimeZone 설정 (원하는 TimeZone으로 변경)
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
            dateFormat.setTimeZone(timeZone);

            // 문자열을 Date로 변환
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String replaceVariableString(String contents, OrderDto orderDto, Map<String, Object> esimMap, boolean esimFlagInfo){

        contents = contents.replaceAll("\\$\\{ordererName\\}", orderDto.getOrdererName());
        contents = contents.replaceAll("\\$\\{ordererTel\\}", orderDto.getOrdererTel());

        String productName = "";
        if(orderDto.getOptionName2()!=null){
            productName = orderDto.getOptionName2();
        }
        if(orderDto.getOptionName3()!=null && !productName.equals("")){
            productName += " / " + orderDto.getOptionName3();
        }else{
            productName = orderDto.getOptionName3();
        }

        contents = contents.replaceAll("\\$\\{productName\\}", productName);


        String orderRealName = "";
        if(orderDto.getOptionName1()!=null){
            orderRealName = orderDto.getOptionName1();
            contents = contents.replaceAll("\\$\\{optionName1\\}", orderDto.getOptionName1());
        }
        if(orderDto.getOptionName2()!=null){
            if(!orderRealName.equals(""))
                orderRealName += " ";
            orderRealName += orderDto.getOptionName2();
            contents = contents.replaceAll("\\$\\{optionName2\\}", orderDto.getOptionName2());

        }
        if(orderDto.getOptionName3()!=null){
            contents = contents.replaceAll("\\$\\{optionName3\\}", orderDto.getOptionName3());

        }
        if(orderDto.getOptionName4()!=null){
            contents = contents.replaceAll("\\$\\{optionName4\\}", orderDto.getOptionName4());
        }
        contents = contents.replaceAll("\\$\\{orderRealName\\}", orderRealName);
        contents = contents.replaceAll("\\$\\{orderTitle\\}", orderDto.getProductName());



        if(esimFlagInfo){
            contents = contents.replaceAll("\\$\\{usageUrl\\}", Objects.toString(esimMap.get("usageUrl"), ""));
            contents = contents.replaceAll("\\$\\{eSimResetInfo\\}", Objects.toString(esimMap.get("eSimResetInfo"), ""));
            contents = contents.replaceAll("\\$\\{eSimMResetInfo\\}", Objects.toString(esimMap.get("eSimMResetInfo"), ""));
            contents = contents.replaceAll("\\$\\{eSimChargeInfo\\}", Objects.toString(esimMap.get("eSimChargeInfo"), ""));
            contents = contents.replaceAll("\\$\\{eSimMChargeInfo\\}", Objects.toString(esimMap.get("eSimMChargeInfo"), ""));
            contents = contents.replaceAll("\\$\\{eSimApnInfo\\}", Objects.toString(esimMap.get("eSimApnInfo"), ""));
            contents = contents.replaceAll("\\$\\{eSimMApnInfo\\}", Objects.toString(esimMap.get("eSimMApnInfo"), ""));



            contents = contents.replaceAll("\\$\\{usage2Url\\}", Objects.toString(esimMap.get("usage2Url"), ""));
            contents = contents.replaceAll("\\$\\{iccid\\}", Objects.toString(esimMap.get("iccid"), ""));
            contents = contents.replaceAll("\\$\\{ipActivityCode\\}", Objects.toString(esimMap.get("activation_code"), ""));
            contents = contents.replaceAll("\\$\\{gaActivityCode\\}", Matcher.quoteReplacement(Objects.toString(esimMap.get("lpaCode"), "")));
            contents = contents.replaceAll("\\$\\{smdp\\}", Objects.toString(esimMap.get("smdp"), ""));
            String qrImage = "<img class=\"CToWUd\" src=\"" + Objects.toString(esimMap.get("qrImageUrl"), "") + "\" alt=\"image.png\" data-image-whitelisted=\"\" data-bit=\"iit\" />";
            contents = contents.replaceAll("\\$\\{qrImage\\}", qrImage);
            contents = contents.replaceAll("\\$\\{qrImageUrl\\}", Objects.toString(esimMap.get("qrImageUrl"), ""));
            contents = contents.replaceAll("\\$\\{rentalNo\\}", Objects.toString(esimMap.get("rentalNo"), ""));
            /*airalo 용 전환 데이터 start*/
            contents = contents.replaceAll("\\$\\{cloudLink\\}", Objects.toString(esimMap.get("cloudLink"), ""));
            contents = contents.replaceAll("\\$\\{accessCode\\}", Objects.toString(esimMap.get("accessCode"), ""));
            /*airalo 용 전환 데이터 end*/
        }

        return contents;
    }


    public static String stringToBase64Encode(String text){
        // URL-safe Base64 인코더 가져오기
        Base64.Encoder urlEncoder = Base64.getUrlEncoder();
        // 바이트 배열로 변환 후 인코딩
        byte[] encodedBytes = urlEncoder.encode(text.getBytes(StandardCharsets.UTF_8));
        // 인코딩된 문자열 출력
        return new String(encodedBytes);
    }

    public static String Base64EncodeToString(String encodedParam){
        if(encodedParam!=null && !encodedParam.equals(""))
        try {
            // URL-safe Base64 디코더 가져오기
            Base64.Decoder urlDecoder = Base64.getUrlDecoder();

            // 디코딩
            byte[] decodedBytes = urlDecoder.decode(encodedParam);
            return new String(decodedBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return encodedParam;
    }

}