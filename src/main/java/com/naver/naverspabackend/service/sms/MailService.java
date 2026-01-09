package com.naver.naverspabackend.service.sms;

import com.naver.naverspabackend.dto.MailContentsDto;
import com.naver.naverspabackend.dto.StoreDto;

import java.util.List;
import java.util.Map;

public interface MailService {

    int sendEmail(Map searchMap, StoreDto storeDto, String email) throws Exception;

    String formatStringToEmail(String shippingMemo) throws Exception;

    void insertMailContents(MailContentsDto mailContentsDto);

    List<MailContentsDto> selectMailContentsList(MailContentsDto mailContentsDto);
}
