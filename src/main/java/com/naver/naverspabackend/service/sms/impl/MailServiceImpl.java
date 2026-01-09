package com.naver.naverspabackend.service.sms.impl;

import com.naver.naverspabackend.NaverSpaBackendApplication;
import com.naver.naverspabackend.dto.MailContentsDto;
import com.naver.naverspabackend.dto.MailDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.MailMapper;
import com.naver.naverspabackend.service.sms.MailService;
import com.naver.naverspabackend.util.MailUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private MailMapper mailMapper;


    @Value("${testmode}")
    private String testMode;

    @Value("${testEmail}")
    private String testEmail;

    public static void main(String[] args) throws Exception {
        formatStringToEmail2("chswo_12@naver.com핸드폰 976");
    }

    @Override
    public int sendEmail(Map searchMap, StoreDto storeDto, String email) throws Exception {
        int cnt = 0;

        MailDto mailDto = new MailDto();

        mailDto.setFromEmail(storeDto.getSendEmail());
        mailDto.setFromEmailPw(storeDto.getSendEmailPw());
        mailDto.setFromUsername(storeDto.getSendEmailUsername());
        mailDto.setSmtpUrl(storeDto.getSendEmailSmtpUrl());
        mailDto.setPort(storeDto.getSendEmailSmtpPort());
        try {
            if("true".equals(testMode)){
                email = testEmail;
            }

            mailDto.setToEmail(email);

            mailDto.setContents(Objects.toString(searchMap.get("emailContents"), ""));
            mailDto.setSubject(Objects.toString(searchMap.get("emailSubject"), ""));
            mailDto.setStoreId(Long.parseLong(Objects.toString(searchMap.get("storeId"),"0")));
            mailDto.setOriginProductNo(Long.parseLong(Objects.toString(searchMap.get("originProductNo"),"0")));
            mailDto.setOptionId(Long.parseLong(Objects.toString(searchMap.get("optionId"),"0")));

            mailUtil.sendTemplateMessage(mailDto);
            mailMapper.insertMailResponse(mailDto);
            cnt = 1;
        }catch (Exception e){
            throw e;
        }

        return cnt;
    }

    @Override
    public String formatStringToEmail(String shippingMemo) throws Exception{
        //한글
        String regExp = "[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]";
        // 공백
        String regExp2 = "\\s";

        shippingMemo = shippingMemo.replaceAll(regExp2, "").replaceAll(regExp, "");

        // 패턴 지정 이메일
        String patternEmail = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
        // 패턴 객체 생성
        Pattern e = Pattern.compile(patternEmail);
        // 문자열에서 패턴 검색
        Matcher m2 = e.matcher(shippingMemo);
        // 검색된 패턴 추출
        String email= null;
        while (m2.find()){
            email=m2.group();
        }
        if(email == null){
            throw new Exception("이메일이 유효하지않음");
        }
        return email;
    }

    @Override
    public void insertMailContents(MailContentsDto mailContentsDto) {
        mailMapper.insertMailContents(mailContentsDto);
    }

    @Override
    public List<MailContentsDto> selectMailContentsList(MailContentsDto mailContentsDto) {
        return mailMapper.selectMailContentsList(mailContentsDto);
    }


    public static String formatStringToEmail2(String shippingMemo) throws Exception{
        //한글
        String regExp = "[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]";
        // 공백
        String regExp2 = "\\s";

        shippingMemo = shippingMemo.replaceAll(regExp2, "").replaceAll(regExp, "");

        // 패턴 지정 이메일
        String patternEmail = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
        // 패턴 객체 생성
        Pattern e = Pattern.compile(patternEmail);
        // 문자열에서 패턴 검색
        Matcher m2 = e.matcher(shippingMemo);
        // 검색된 패턴 추출
        String email= null;
        while (m2.find()){
            email=m2.group();
        }
        if(email == null){
            throw new Exception("이메일이 유효하지않음");
        }
        return email;
    }

}
