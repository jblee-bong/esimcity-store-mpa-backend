package com.naver.naverspabackend.service.common.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/** 업로드는 이미지(jpg, png, gif, webp)만: html 등으로 우리 도메인에서 피싱 페이지를 못 띄우게 */
class ImageUploadCheckTest {

    @Test
    void imagesAllowedWithLowercaseExt() {
        assertEquals(".png", CommonServiceImpl.imageExt("a.PNG", "image/png"));
        assertEquals(".jpeg", CommonServiceImpl.imageExt("photo.final.jpeg", "image/jpeg"));
        assertEquals(".webp", CommonServiceImpl.imageExt("t.webp", "image/webp"));
    }

    @Test
    void othersRejected() {
        assertThrows(ResponseStatusException.class, () -> CommonServiceImpl.imageExt("x.html", "text/html"));
        assertThrows(ResponseStatusException.class, () -> CommonServiceImpl.imageExt("x.html", "image/png"));
        assertThrows(ResponseStatusException.class, () -> CommonServiceImpl.imageExt("x.png", "text/html"));
        assertThrows(ResponseStatusException.class, () -> CommonServiceImpl.imageExt("noext", "image/png"));
        assertThrows(ResponseStatusException.class, () -> CommonServiceImpl.imageExt(null, null));
    }
}
