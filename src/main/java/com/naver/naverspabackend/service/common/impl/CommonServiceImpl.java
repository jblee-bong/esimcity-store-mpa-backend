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


        String ext = imageExt(multipartFile.getOriginalFilename(), multipartFile.getContentType());


        String fileName = UUID.randomUUID() + new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date()) + ext;


        File file = stream2file(multipartFile.getInputStream(),fileSavePath + fileName);

        return fileName;
    }


    private static final java.util.Set<String> IMAGE_EXTS = new java.util.HashSet<>(java.util.Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));

    /** 이미지(jpg, png, gif, webp)만 허용: html 등을 올려 우리 도메인에서 퍼뜨리지 못하게. 확장자는 소문자로 */
    static String imageExt(String originalName, String contentType) {
        int position = originalName == null ? -1 : originalName.lastIndexOf('.');
        String ext = position < 0 ? "" : originalName.substring(position).toLowerCase(java.util.Locale.ROOT);
        if (!IMAGE_EXTS.contains(ext) || contentType == null || !contentType.toLowerCase(java.util.Locale.ROOT).startsWith("image/")) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                "이미지 파일(jpg, png, gif, webp)만 올릴 수 있습니다.");
        }
        return ext;
    }

    public static File stream2file(InputStream in, String filePath) throws IOException {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            IOUtils.copy(in, out);
        }
        return new File(filePath);
    }
}
