package com.naver.naverspabackend.service.common.impl;

import com.naver.naverspabackend.service.common.CommonService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CommonServiceImpl implements CommonService {

    @Value("${fileSavePath}")
    private String fileSavePath;

    @Override
    public String insertFile(MultipartFile multipartFile) throws Exception {


        int position = multipartFile.getOriginalFilename().lastIndexOf(".");
        String ext = multipartFile.getOriginalFilename().substring(position, multipartFile.getOriginalFilename().length());


        String fileName = UUID.randomUUID() + new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date()) + ext;


        File file = stream2file(multipartFile.getInputStream(),fileSavePath + fileName);

        return fileName;
    }


    public static File stream2file(InputStream in, String filePath) throws IOException {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            IOUtils.copy(in, out);
        }
        return new File(filePath);
    }
}
