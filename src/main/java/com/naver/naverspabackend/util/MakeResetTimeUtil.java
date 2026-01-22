package com.naver.naverspabackend.util;

import com.naver.naverspabackend.dto.ApiCardTypeDto;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.enums.ApiType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class MakeResetTimeUtil {

    public static final String[] tsimResetBasing2359 = new String[]{"vmobile.jp", "cmhk", "3gnet", "mobile.three.com.hk", "mobiledata", "mobile", "ctm-mobile","ctexcel"};
    public static final String[] tsimResetActive24 = new String[]{"plus","e-ideas","iij"};
    public static final String[] tsimResetChinaTimeAm8 = new String[]{"sl2sfr"};
    public static final String[] tsimResetVetimeTime2359 = new String[]{"m3-world"};

    public static final String[] worldMoveResetDiffRegion09 = new String[]{"Sri Lanka","Oman","Bangladesh"};

    public static final String[] worldMoveResetDiffEurope = new String[]{"Europe"};

    public static final String[] worldMoveResetDiffRegionJapan = new String[]{"Japan"};

    public static final String[] worldMoveResetDiffRegionUsa = new String[]{"USA"};

    public static String makeResetInfoText(ApiPurchaseItemDto apiPurchaseItem, ApiCardTypeDto apiCardTypeDto) {
        String result = "";
        try{
            if(apiPurchaseItem.isApiPurchaseItemIsDaily()){
                if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.TSIM.name())){
                    String apn = apiPurchaseItem.getApiPurchaseApn();
                    if(apn!=null){
                        apn = apn.toLowerCase();
                        if(Arrays.asList(tsimResetBasing2359).indexOf(apn)>-1){
                            result = " 01:00(한국 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetActive24).indexOf(apn)>-1){
                            result = " 활성화 시점부터 24시간 단위로 충전 되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetChinaTimeAm8).indexOf(apn)>-1){
                            result = " 09:00(한국 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetVetimeTime2359).indexOf(apn)>-1){
                            result = " 02:00(한국 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.WORLDMOVE.name())){
                    if(Arrays.asList(worldMoveResetDiffRegion09).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 09:00(한국 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else if(Arrays.asList(worldMoveResetDiffRegionJapan).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 23:00(한국 시간) 충전되며, 이용 일수는 매일 01:00(한국 시간) 단위로 계산됩니다.";
                    }else if(Arrays.asList(worldMoveResetDiffEurope).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 01:00(한국 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else{
                        result = " 01:00(한국 시간) 충전되며, 이용 일수는 매일 01:00(한국 시간) 단위로 계산됩니다.";
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.TUGE.name())){
                    if(apiPurchaseItem.getApiPurchaseItemPeriodType()==0){
                        result = " 활성화 시점부터 24시간 단위로 충전 되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else {
                        if (apiPurchaseItem.getApiPurchaseItemCardType() != null) {
                            if(apiCardTypeDto!=null && apiCardTypeDto.getTimeZone()!=null){
                                ZonedDateTime sourceTime = LocalDate.of(2024, 1, 1)
                                        .atTime(LocalTime.MIN) // 00:00
                                        .atZone(ZoneId.of(apiCardTypeDto.getTimeZone()));

                                // 2. 한국 시간(UTC+9)으로 변환
                                ZonedDateTime kstTime = sourceTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));

                                // 3. 결과에서 시간만 추출
                                LocalTime resultTime = kstTime.toLocalTime();
                                String resetTime = resultTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                                result = " " + resetTime + "(한국 시간) 충전되며, 이용 일수는 매일 "+resetTime+"(한국 시간) 단위로 계산됩니다.";

                            }
                        }
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.ESIMACCESS.name())){
                    result = " 00:00(현지 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                }
            }else{

                if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.TSIM.name())){
                    String apn = apiPurchaseItem.getApiPurchaseApn();
                    if(apn!=null){
                        apn = apn.toLowerCase();
                        if(Arrays.asList(tsimResetBasing2359).indexOf(apn)>-1){
                            result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetActive24).indexOf(apn)>-1){
                            result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetChinaTimeAm8).indexOf(apn)>-1){
                            result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetVetimeTime2359).indexOf(apn)>-1){
                            result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.WORLDMOVE.name())){
                    if(Arrays.asList(worldMoveResetDiffRegion09).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else if(Arrays.asList(worldMoveResetDiffRegionJapan).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 매일 01:00(한국 시간) 단위로 계산됩니다.";
                    }else if(Arrays.asList(worldMoveResetDiffEurope).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else{
                        result = " 매일 01:00(한국 시간) 단위로 계산됩니다.";
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.TUGE.name())){
                    if(apiPurchaseItem.getApiPurchaseItemPeriodType()==0){
                        result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else {
                        if (apiPurchaseItem.getApiPurchaseItemCardType() != null) {
                            if(apiCardTypeDto!=null && apiCardTypeDto.getTimeZone()!=null){
                                ZonedDateTime sourceTime = LocalDate.of(2024, 1, 1)
                                        .atTime(LocalTime.MIN) // 00:00
                                        .atZone(ZoneId.of(apiCardTypeDto.getTimeZone()));

                                // 2. 한국 시간(UTC+9)으로 변환
                                ZonedDateTime kstTime = sourceTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));

                                // 3. 결과에서 시간만 추출
                                LocalTime resultTime = kstTime.toLocalTime();
                                String resetTime = resultTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                                result = " 매일 "+resetTime+"(한국 시간) 단위로 계산됩니다.";

                            }
                        }
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.ESIMACCESS.name())){
                    result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                }
            }
        }catch (Exception e){}

        if(result.equals("")){
            return result;
        }else{
            if(apiPurchaseItem.isApiPurchaseItemIsDaily()){
                return "<li style=\"margin-bottom: 8px;\"><strong style=\"color: #dc2626;\">데이터는 매일" + result + " 사용량조회에서도 확인 가능합니다.</strong></li>";
            }else{
                return "<li style=\"margin-bottom: 8px;\"><strong style=\"color: #dc2626;\">이용일수는" + result + " 사용량조회에서도 확인 가능합니다.</strong></li>";

            }
        }
    }


    public static String makeMResetInfoText(ApiPurchaseItemDto apiPurchaseItem, ApiCardTypeDto apiCardTypeDto) {
        String result = "";
        try{
            if(apiPurchaseItem.isApiPurchaseItemIsDaily()){
                if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.TSIM.name())){
                    String apn = apiPurchaseItem.getApiPurchaseApn();
                    if(apn!=null){
                        apn = apn.toLowerCase();
                        if(Arrays.asList(tsimResetBasing2359).indexOf(apn)>-1){
                            result = " 01:00(한국 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetActive24).indexOf(apn)>-1){
                            result = " 활성화 시점부터 24시간 단위 충전로 되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetChinaTimeAm8).indexOf(apn)>-1){
                            result = " 09:00(한국 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetVetimeTime2359).indexOf(apn)>-1){
                            result = " 02:00(한국 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.WORLDMOVE.name())){
                    if(Arrays.asList(worldMoveResetDiffRegion09).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 09:00(한국 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else if(Arrays.asList(worldMoveResetDiffRegionJapan).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 23:00(한국 시간) 충전되며, 이용 일수는 매일 01:00(한국 시간) 단위로 계산됩니다.";
                    }else if(Arrays.asList(worldMoveResetDiffEurope).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 01:00(한국 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else{
                        result = " 01:00(한국 시간) 충전되며, 이용 일수는 매일 01:00(한국 시간) 단위로 계산됩니다.";
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.TUGE.name())){
                    if(apiPurchaseItem.getApiPurchaseItemPeriodType()==0){
                        result = " 활성화 시점부터 24시간 단위 충전로 되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else {
                        if (apiPurchaseItem.getApiPurchaseItemCardType() != null) {
                            if(apiCardTypeDto!=null && apiCardTypeDto.getTimeZone()!=null){
                                ZonedDateTime sourceTime = LocalDate.of(2024, 1, 1)
                                        .atTime(LocalTime.MIN) // 00:00
                                        .atZone(ZoneId.of(apiCardTypeDto.getTimeZone()));
                                // 2. 한국 시간(UTC+9)으로 변환
                                ZonedDateTime kstTime = sourceTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                                // 3. 결과에서 시간만 추출
                                LocalTime resultTime = kstTime.toLocalTime();
                                String resetTime = resultTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                                result = " "+resetTime+"(한국 시간) 충전되며, 이용 일수는 매일 "+resetTime+"(한국 시간) 단위로 계산됩니다.";

                            }
                        }
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.ESIMACCESS.name())){
                    result = " 00:00(현지 시간) 충전되며, 이용 일수는 활성화 시점부터 24시간 단위로 계산됩니다.";
                }
            }else{
                if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.TSIM.name())){
                    String apn = apiPurchaseItem.getApiPurchaseApn();
                    if(apn!=null){
                        apn = apn.toLowerCase();
                        if(Arrays.asList(tsimResetBasing2359).indexOf(apn)>-1){
                            result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetActive24).indexOf(apn)>-1){
                            result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetChinaTimeAm8).indexOf(apn)>-1){
                            result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }else if(Arrays.asList(tsimResetVetimeTime2359).indexOf(apn)>-1){
                            result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                        }
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.WORLDMOVE.name())){
                    if(Arrays.asList(worldMoveResetDiffRegion09).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else if(Arrays.asList(worldMoveResetDiffRegionJapan).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 매일 01:00(한국 시간) 단위로 계산됩니다.";
                    }else if(Arrays.asList(worldMoveResetDiffEurope).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else{
                        result = " 매일 01:00(한국 시간) 단위로 계산됩니다.";
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.TUGE.name())){
                    if(apiPurchaseItem.getApiPurchaseItemPeriodType()==0){
                        result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                    }else {
                        if (apiPurchaseItem.getApiPurchaseItemCardType() != null) {
                            if(apiCardTypeDto!=null && apiCardTypeDto.getTimeZone()!=null){
                                ZonedDateTime sourceTime = LocalDate.of(2024, 1, 1)
                                        .atTime(LocalTime.MIN) // 00:00
                                        .atZone(ZoneId.of(apiCardTypeDto.getTimeZone()));
                                // 2. 한국 시간(UTC+9)으로 변환
                                ZonedDateTime kstTime = sourceTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                                // 3. 결과에서 시간만 추출
                                LocalTime resultTime = kstTime.toLocalTime();
                                String resetTime = resultTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                                result = " 매일 "+resetTime+"(한국 시간) 단위로 계산됩니다.";

                            }
                        }
                    }
                }else if(apiPurchaseItem.getApiPurchaseItemType().equals(ApiType.ESIMACCESS.name())){
                    result = " 활성화 시점부터 24시간 단위로 계산됩니다.";
                }

            }
        }catch (Exception e){}
        if(result.equals("")){
            return result;
        }else {
            if(apiPurchaseItem.isApiPurchaseItemIsDaily()){
                return "* 데이터는 매일" +result + " 사용량조회에서도 확인 가능합니다.";
            }else{
                return "* 이용 일수는" +result + " 사용량조회에서도 확인 가능합니다.";

            }
        }
    }
    public static String makeTsimResetText(ApiPurchaseItemDto apiPurchaseItem) {
        String result = "";
        try{
            if(apiPurchaseItem!=null && apiPurchaseItem.isApiPurchaseItemIsDaily()){
                String apn = apiPurchaseItem.getApiPurchaseApn();
                if(apn!=null){
                    apn = apn.toLowerCase();
                    if(Arrays.asList(tsimResetBasing2359).indexOf(apn)>-1){
                        result = "데이터 충전: 01:00(한국 시간)<br/>이용 일수: 활성화 시점부터 24시간";
                    }else if(Arrays.asList(tsimResetActive24).indexOf(apn)>-1){
                        result = "데이터 충전: 활성화 시점부터 24시간<br/>이용 일수: 활성화 시점부터 24시간";
                    }else if(Arrays.asList(tsimResetChinaTimeAm8).indexOf(apn)>-1){
                        result = "데이터 충전: 09:00(한국 시간)<br/>이용 일수: 활성화 시점부터 24시간";
                    }else if(Arrays.asList(tsimResetVetimeTime2359).indexOf(apn)>-1){
                        result = "데이터 충전: 02:00(한국 시간)<br/>이용 일수: 활성화 시점부터 24시간";
                    }
                }
            }else{
                if(apiPurchaseItem!=null && apiPurchaseItem.isApiPurchaseItemIsDaily()){
                    String apn = apiPurchaseItem.getApiPurchaseApn();
                    if(apn!=null){
                        apn = apn.toLowerCase();
                        if(Arrays.asList(tsimResetBasing2359).indexOf(apn)>-1){
                            result = "이용 일수: 활성화 시점부터 24시간";
                        }else if(Arrays.asList(tsimResetActive24).indexOf(apn)>-1){
                            result = "이용 일수: 활성화 시점부터 24시간";
                        }else if(Arrays.asList(tsimResetChinaTimeAm8).indexOf(apn)>-1){
                            result = "이용 일수: 활성화 시점부터 24시간";
                        }else if(Arrays.asList(tsimResetVetimeTime2359).indexOf(apn)>-1){
                            result = "이용 일수: 활성화 시점부터 24시간";
                        }
                    }
                }

            }
        }catch (Exception e){}
        return result;
    }

    public static String makeTsimEsimAccessText(ApiPurchaseItemDto apiPurchaseItem) {
        String result = "";
        try{
            if(apiPurchaseItem!=null && apiPurchaseItem.isApiPurchaseItemIsDaily()){
                result = "데이터 충전: 00:00(현지 시간)<br/>이용 일수: 활성화 시점부터 24시간";
            }else{
                result = "이용 일수: 활성화 시점부터 24시간";
            }
        }catch (Exception e){}
        return result;
    }
    public static String makeWorldMoveResetText(ApiPurchaseItemDto apiPurchaseItem) {
        String result = "";
        try{
            if(apiPurchaseItem.isApiPurchaseItemIsDaily()){
                    if(Arrays.asList(worldMoveResetDiffRegion09).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = "데이터 충전: 09:00(한국 시간)<br/>이용 일수: 활성화 시점부터 24시간";
                    }else if(Arrays.asList(worldMoveResetDiffRegionJapan).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = "데이터 충전: 23:00(한국 시간)<br/>이용 일수: 01:00(한국 시간)";
                    }else if(Arrays.asList(worldMoveResetDiffEurope).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                        result = "데이터 충전: 01:00(한국 시간)<br/>이용 일수: 활성화 시점부터 24시간";
                    }else{
                        result = "데이터 충전: 01:00(한국 시간)<br/>이용 일수: 01:00(한국 시간)";
                    }
            }else{
                if(Arrays.asList(worldMoveResetDiffRegion09).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                    result = "이용 일수: 활성화 시점부터 24시간";
                }else if(Arrays.asList(worldMoveResetDiffRegionJapan).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                    result = "이용 일수: 01:00(한국 시간)";
                }else if(Arrays.asList(worldMoveResetDiffEurope).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                    result = "이용 일수: 활성화 시점부터 24시간";
                }else{
                    result = "이용 일수: 01:00(한국 시간)";
                }
            }
        }catch (Exception e){}
        return result;
    }


    public static String makeWorldMoveResetTime(ApiPurchaseItemDto apiPurchaseItem) {
        String result = "";
        try{
            if(apiPurchaseItem.isApiPurchaseItemIsDaily()){
                if(Arrays.asList(worldMoveResetDiffRegion09).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                    result = "09:00";
                }else if(Arrays.asList(worldMoveResetDiffRegionJapan).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                    result = "23:00";
                }else if(Arrays.asList(worldMoveResetDiffEurope).indexOf(apiPurchaseItem.getApiPurchaseCoverDomainCode())>-1){
                    result = "01:00";
                }else{
                    result = "01:00";
                }
            }
        }catch (Exception e){}
        return result;
    }
}
