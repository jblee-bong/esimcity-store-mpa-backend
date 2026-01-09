package com.naver.naverspabackend.controller;

import com.google.gson.JsonObject;
import com.naver.naverspabackend.service.common.CommonService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/common")
@AllArgsConstructor
public class CommonController {


    @Autowired
    private CommonService commonService;



    static String serverOrigin;
    @Value("${server-origin}")
    public void setServerOrigin(String serverOrigin) {
        this.serverOrigin = serverOrigin;
    }
    /**
     * image 업로드 한다.
     *
     * @return 공통코드 목록 200 OK
     * @throws Exception the exception
     */
    @PostMapping(value = "/upload")
    public String insertFile(@RequestParam(name="file") MultipartFile multipartFile) throws Exception {

        String fileName = commonService.insertFile(multipartFile);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("location",serverOrigin + "/file/" + fileName);
        return jsonObject.toString();
    }


}
