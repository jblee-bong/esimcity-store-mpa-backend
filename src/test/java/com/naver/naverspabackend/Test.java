package com.naver.naverspabackend;

import static org.assertj.core.api.Assertions.*;

import com.google.gson.Gson;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.FileUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.Assertions;

public class Test {

    @org.junit.jupiter.api.Test
    public void regExpTest(){
        String regExp = "[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]";
        String regExp2 = "\\s";

        String testString = "test@naver.com ㅎㅇ요";

        String passRegExpEmail = "^[a-zA-Z0-9](?:[-_\\.]?[a-zA-Z0-9])*@[a-zA-Z0-9](?:[-_\\.]?[a-zA-Z0-9])*\\.[a-zA-Z]{2,3}$";

        String s = testString.replaceAll(regExp2, "").replaceAll(regExp, "");

        boolean matches = s.matches(passRegExpEmail);

        System.out.println(s);
        assertThat(matches).isTrue();

    }

    @org.junit.jupiter.api.Test
    public void rePath(){
        System.out.println(FileUtil.class.getResource("/qrcode/").getPath());

        String qrImage = "<img class=\"CToWUd\" src=\"" + "http://test.com" + "\" alt=\"image.png\" data-image-whitelisted=\"\" data-bit=\"iit\" />";

        System.out.println(qrImage);
    }


    @org.junit.jupiter.api.Test
    public void rePathTest(){
        Gson gson = new Gson();
        Map<String, String> exitem = new HashMap<>();
        exitem.put("id",1+"");
        exitem.put("type",ApiType.TSIM.name());
        exitem.put("orderId","fasfafssf");
        exitem.put("iccid","fasfasfsfasfa");
        System.out.println(CommonUtil.stringToBase64Encode(gson.toJson(exitem)));
    }



}
