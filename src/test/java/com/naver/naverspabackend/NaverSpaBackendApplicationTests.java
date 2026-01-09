package com.naver.naverspabackend;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.dto.MailDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.util.ApiUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@SpringBootTest
class NaverSpaBackendApplicationTests {
    @Value(value="${kakao.api.domain}")
    private String kakaoDomain;



    @Value(value="${kakao.api.messagesUrl}")
    private String kakaoMessagesUrl;
    static String TEL25_USER_ID = "aaaa_dev";
    static String TEL25_SECURITY_TOKEN = "8C3JqvpPof4OOiFQ";



    private static final String MAIL_DEBUG = "mail.debug";
    private static final String MAIL_SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

    // 2. 🚨 SSL 활성화 설정 추가 (465 포트 사용 시 필수)
    private static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
    // 서버 인증서의 이름과 호스트 이름이 일치하는지 확인하기 위해 trust 설정 추가
    private static final String MAIL_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";
    @Value("${testmode}")
    private String testMode;
    @Test
    void contextLoads() throws NoSuchAlgorithmException {
        makeXTel25AccessToken();
    }

    @Test
    void test(){
        System.out.println("testMode ================" + "true".equals(testMode));
        System.out.println("testMode ================" + "true".equals(testMode));
        if("true".equals(testMode)){
            System.out.println("qkrtkdrlf");
        }
    }


    @Test
    void mailTransTest() throws MessagingException {
        JavaMailSender emailSender = null;
        emailSender = mailSender();
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        //메일 제목 설정
        helper.setSubject("제목1111");

        //수신자 설정
        helper.setTo("jblee6110@gmail.com");
        helper.setFrom("이심시티<master@esimcity.co.kr>");

        //템플릿에 전달할 데이터 설정
        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("contents", "내용입니다111.");

/*        Context context = new Context();
        emailValues.forEach((key, value)->{
            context.setVariable(key, value);
        });*/

        //메일 내용 설정
        String html = "내용입니다.";
        helper.setText(html, true);

//        //템플릿에 들어가는 이미지 cid로 삽입
//        helper.addInline("image1", new ClassPathResource("static/images/image-1.jpeg"));

        //메일 보내기
        emailSender.send(message);
    }

    @Test
    void kakaoTransTest() throws MessagingException {
        List<Map<String, Object>> recipientList = new ArrayList<Map<String, Object>>();
        HashMap<String, Object> resendParameter = new HashMap<String, Object>();
        resendParameter.put("isResend",true);

        Map<String, Object> recipientInfoMap = new HashMap<String, Object>();
        recipientInfoMap.put("recipientNo", "01097716110");
        HashMap<String, Object> copyKakaoParameters = new HashMap<String, Object>();
        copyKakaoParameters.put("ordererName", "이재봉");
        copyKakaoParameters.put("orderRealName", "orderRealName");
        copyKakaoParameters.put("qrImageUrl", "qrImageUrl");
        copyKakaoParameters.put("qrImage", "qrImage");

        copyKakaoParameters.put("smdp", "smdp");
        copyKakaoParameters.put("ipActivityCode", "ipActivityCode");
        copyKakaoParameters.put("gaActivityCode", "gaActivityCode");
        copyKakaoParameters.put("iccid", "iccid");

        copyKakaoParameters.put("optionName1", "optionName1");
        copyKakaoParameters.put("optionName2", "optionName2");
        copyKakaoParameters.put("optionName3", "optionName3");
        recipientInfoMap.put("templateParameter", copyKakaoParameters);


       recipientInfoMap.put("resendParameter", resendParameter);

        recipientList.add(recipientInfoMap);

        StoreDto storeDto = new StoreDto();
        storeDto.setNhnKakaoSenderKey("0ffc81486253bd9f08eb5b18c0de6d0f3141e75a");
        storeDto.setNhnKakaoAppKey("WJv22OhYbVioBMxe");
        storeDto.setNhnKakaoSecretKey("0339i6lA6LpEAakB78xDnxzjB8NoZlCw");
        String templateKey = "esimcity_20250801001";
        Map<String, Object> body = buildRequestBody(recipientList, templateKey,storeDto);
        int cnt = 0;

        Map<String, Object> res = new HashMap<>();
        try {
            // 메세지 발송
            Map<String, String> paramHeader = new HashMap<>();
            paramHeader.put("X-Secret-Key", storeDto.getNhnKakaoSecretKey());

            Map<String, Object> pathParam = new HashMap<>();
            pathParam.put("appkey", storeDto.getNhnKakaoAppKey());

            String post = ApiUtil.post(kakaoDomain + kakaoMessagesUrl, paramHeader,  body, pathParam, okhttp3.MediaType.parse("application/json; charset=UTF-8"));
            ObjectMapper objectMapper = new ObjectMapper();
            res = objectMapper.readValue(post, new TypeReference<Map<String, Object>>() {
            });
        }catch (Exception e){
            System.out.println(e);
        }

    }

    public Map<String,Object> buildRequestBody(List<Map<String,Object>> recipientList, String templateKey,  StoreDto storeDto){
        Map<String, Object> requestBody = new HashMap<String, Object>();
        // 수신자 정보 list
        requestBody.put("recipientList", recipientList);
        // 발신 프로필 키
        requestBody.put("senderKey", storeDto.getNhnKakaoSenderKey());
        // 발신 템플릿 코드
        requestBody.put("templateCode", templateKey);

        return requestBody;
    }


    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.daum.net");
        mailSender.setProtocol("smtp");
        mailSender.setPort(465);
        mailSender.setUsername("lovejh672");
        mailSender.setPassword("nykrwyhwtdsrhyeo");
        mailSender.setDefaultEncoding("UTF-8");
        Properties properties = mailSender.getJavaMailProperties();
        properties.put(MAIL_SMTP_AUTH, true);
        properties.put(MAIL_DEBUG, true);

        if(465==465){
            properties.put(MAIL_SMTP_SSL_ENABLE, "true");
            properties.put(MAIL_SMTP_SSL_TRUST, "smtp.daum.net");
        }else{
            properties.put(MAIL_SMTP_STARTTLS_REQUIRED, true);
            properties.put(MAIL_SMTP_STARTTLS_ENABLE, true);
        }
        mailSender.setJavaMailProperties(properties);
        return mailSender;
    }


    public static void makeXTel25AccessToken() throws NoSuchAlgorithmException {
        Date date = new Date();
        long timestamp = date.getTime();
        String encodedToken = ComputeSHA256(timestamp + TEL25_SECURITY_TOKEN);
        String mergedToken = TEL25_USER_ID + "/" + timestamp + "/" + encodedToken;
        String accessToken = EncodeBase64(mergedToken);
        String xTel25AccessToken = "Bearer " + accessToken;
        System.out.println(xTel25AccessToken);
    }

    public static String ComputeSHA256(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(text.getBytes());

        return bytesToHex(md.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }


    public static String EncodeBase64(String plainText)
    {
        String encodedString = Base64.getEncoder().encodeToString(plainText.getBytes());
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }
    
    

}
