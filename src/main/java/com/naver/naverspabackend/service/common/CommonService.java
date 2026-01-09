package com.naver.naverspabackend.service.common;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface CommonService {

    String insertFile(MultipartFile multipartFile) throws Exception;
}
