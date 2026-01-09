package com.naver.naverspabackend.util;

import com.naver.naverspabackend.dto.MailDto;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class MailUtil {
    private static final String MAIL_DEBUG = "mail.debug";
    private static final String MAIL_SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

    // 2. 🚨 SSL 활성화 설정 추가 (465 포트 사용 시 필수)
    private static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
    // 서버 인증서의 이름과 호스트 이름이 일치하는지 확인하기 위해 trust 설정 추가
    private static final String MAIL_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";

    public void sendTemplateMessage(MailDto mailDto) throws MessagingException {
        JavaMailSender emailSender = null;
        if(mailDto.getFromEmail().indexOf("gmail.com")>-1){
            emailSender = gmailMailSender(mailDto.getFromEmail(),mailDto.getFromEmailPw());
        }
        emailSender = mailSender(mailDto);
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        //메일 제목 설정
        helper.setSubject(mailDto.getSubject());

        //수신자 설정
        helper.setTo(mailDto.getToEmail());
        helper.setFrom(mailDto.getFromEmail());

        //템플릿에 전달할 데이터 설정
        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("contents", mailDto.getContents());

/*        Context context = new Context();
        emailValues.forEach((key, value)->{
            context.setVariable(key, value);
        });*/

        //메일 내용 설정
        String html = mailDto.getContents();
        helper.setText(html, true);

//        //템플릿에 들어가는 이미지 cid로 삽입
//        helper.addInline("image1", new ClassPathResource("static/images/image-1.jpeg"));

        //메일 보내기
        emailSender.send(message);
    }

    public JavaMailSender gmailMailSender(String fromEmail, String fromEmailPw) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setProtocol("smtp");
        mailSender.setPort(587);
        mailSender.setUsername(fromEmail);
        mailSender.setPassword(fromEmailPw);
        mailSender.setDefaultEncoding("UTF-8");
        Properties properties = mailSender.getJavaMailProperties();
        properties.put(MAIL_SMTP_STARTTLS_REQUIRED, true);
        properties.put(MAIL_SMTP_STARTTLS_ENABLE, true);
        properties.put(MAIL_SMTP_AUTH, true);
        properties.put(MAIL_DEBUG, true);
        mailSender.setJavaMailProperties(properties);
        return mailSender;
    }


    public JavaMailSender mailSender(MailDto mailDto) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailDto.getSmtpUrl());
        mailSender.setProtocol("smtp");
        mailSender.setPort(mailDto.getPort());
        if(mailDto.getFromUsername()==null)
            mailSender.setUsername(mailDto.getFromEmail());
        else
            mailSender.setUsername(mailDto.getFromUsername());
        mailSender.setPassword(mailDto.getFromEmailPw());
        mailSender.setDefaultEncoding("UTF-8");
        Properties properties = mailSender.getJavaMailProperties();


        properties.put(MAIL_SMTP_AUTH, true);
        properties.put(MAIL_DEBUG, true);

        if(mailDto.getPort()==465){
            properties.put(MAIL_SMTP_SSL_ENABLE, "true");
            properties.put(MAIL_SMTP_SSL_TRUST, mailDto.getSmtpUrl());
        }else{
            properties.put(MAIL_SMTP_STARTTLS_REQUIRED, true);
            properties.put(MAIL_SMTP_STARTTLS_ENABLE, true);
        }

        mailSender.setJavaMailProperties(properties);
        return mailSender;
    }

}
