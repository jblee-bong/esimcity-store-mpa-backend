package com.naver.naverspabackend.batch.tasklet;


import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.mybatis.mapper.MatchInfoMapper;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.service.sms.MailService;
import com.naver.naverspabackend.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * 스마트 스토어 item select 상품 목록 조회
 *
 * @author gil
 */
@Slf4j
@Component
@StepScope
public class NaverCancelIccidTasklet implements Tasklet {


    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MatchInfoMapper matchInfoMapper;


    @Autowired
    private MailService mailService;

    @Autowired
    private FileUtil fileUtil;


    @Value("${testmode}")
    private String testMode;
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {


        if(testMode.equals("true"))
        {
            return null;
        }
        log.error("*************************************************** 취소 오더중 iccid 없는값(esim 결제됬으나 취소) iccid 있으면 추가 5***************************************************");

        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeMapper.selectStoreList(param);

        for (StoreDto storeDto : storeDtos) {

            List<OrderDto> orderDtoList = orderMapper.selectOrderForCancelIccidUpdate(storeDto);

            for (OrderDto orderDto : orderDtoList) {
                try {
                    iccidUpdate(orderDto,storeDto);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 실패
                }
            }

        }
        return RepeatStatus.FINISHED;
    }

    public void iccidUpdate(OrderDto orderDto, StoreDto storeDto) throws Exception {
        TsimUtil tsimUtil = EsimUtil.getTsimUtil(null, storeDto, null);
        TugeUtil tugeUtil = EsimUtil.getTugeUtil(storeDto,null);
        WorldMoveUtil worldMoveUtil =  EsimUtil.getWorldMoveUtil(storeDto);
        NizUtil nizUtil =  EsimUtil.getNizUtil(storeDto);
        AirAloUtil airAloUtil  =  EsimUtil.getAirAloUtil(storeDto);


        MatchInfoDto matchInfoDto = matchInfoMapper.selectMatchInfoByOrder(orderDto);

        if (matchInfoDto == null) {
            log.info("matchInfo 없음");
            return;
        }
        if(orderDto.getAllQuantity()!=null)
        for(int i=0;i<orderDto.getAllQuantity();i++){
            if ("Y".equals(matchInfoDto.getEsimFlag())) {


                HashMap esimMap = new HashMap();
                if(matchInfoDto.getEsimType().equals("02")){
                    try {
                        if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){ //이심 요청 전
                            return;
                        }else{
                            //여기서 상태 체크해서 개통 완료일 경우 다음 스탭 진행
                            //0 : 개통 요청 상태
                            //1 : 개통 완료
                            //2 : 개통 오류
                            //3 : 개통 보류
                            //4, 5, 6 : 개통 처리 중

                            String[] esimApiRequestIds = orderDto.getEsimApiRequestId().split(",");
                            esimMap =  nizUtil.contextLoads3(esimApiRequestIds[i],orderDto.getId());

                            if(esimMap.get("status").toString().equals("1")){
                            }else{
                                return;
                            }
                            try{
                                orderDto.setEsimIccid(esimMap.get("simno")!=null?esimMap.get("simno").toString():null);
                                log.error("cancel ICCID : " + esimMap.get("simno"));
                                orderMapper.updateOrderSmsForEsimIccid(orderDto);
                            }catch (Exception e){
                                e.printStackTrace();
                            }


                            orderDto.setEsimCorp("니즈");

                        }




                    } catch (NoSuchAlgorithmException e) {
                        matchInfoDto.setEsimFlag("N");
                    }
                }else if(matchInfoDto.getEsimType().equals("03")){
                    try {
                        if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){
                            return;
                        }else{
                            //여기서 상태 체크해서 개통 완료일 경우 다음 스탭 진행
                            String[] esimApiRequestIds = orderDto.getEsimApiRequestId().split(",");
                            Map<String,Object> result =  tsimUtil.contextLoads3(esimApiRequestIds[i],orderDto.getId());

                            if(result.get("msg").toString().equals("Success")){
                                esimMap =(HashMap) result.get("result");
                                fileUtil.makeQrCodeTsim(esimMap);
                            }else{
                                return;
                            }
                            try{
                                orderDto.setEsimIccid(esimMap.get("iccid")!=null?esimMap.get("iccid").toString():null);
                                orderMapper.updateOrderSmsForEsimIccid(orderDto);
                            }catch (Exception e){
                                e.printStackTrace();
                            }


                            orderDto.setEsimCorp("Tsim");

                        }

                    } catch (NoSuchAlgorithmException e) {
                        matchInfoDto.setEsimFlag("N");
                    }
                }else if(matchInfoDto.getEsimType().equals("04")){
                 //AirAlo  구매시, ICCID 등록

                }
                else if(matchInfoDto.getEsimType().equals("05")){
                    //Tuge
                    try {
                        Map<String,Object> param = new HashMap<>();
                        param.put("orderId", orderDto.getId());
                        List<OrderTugeEsimDto> orderTugeEsimDtoList = orderMapper.selectListOrderTugeEsim(param);


                        if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){ //이심 요청 전
                            return;
                        }else if(orderDto.getEsimApiRequestId().split(",").length == orderTugeEsimDtoList.size() ){ // 3건구매시 3건이 다들어왔다.

                            //여기서 상태 체크해서 개통 완료일 경우 다음 스탭 진행
                            String[] esimApiRequestIds = orderDto.getEsimApiRequestId().split(",");

                            OrderTugeEsimDto orderTugeEsimDto = orderTugeEsimDtoList.get(i);
                            orderDto.setEsimIccid(orderTugeEsimDto.getIccid());
                            orderMapper.updateOrderSmsForEsimIccid(orderDto);

                        }else{
                            return;
                        }




                    } catch (Exception e) {
                        matchInfoDto.setEsimFlag("N");
                    }
                }else if(matchInfoDto.getEsimType().equals("06")){
                    // WorldMoveUItil
                    try {
                        Map<String,Object> param = new HashMap<>();
                        param.put("orderId", orderDto.getId());
                        int countOrderWorldMoveEsim = orderMapper.selectCountOrderWorldMoveEsim(param);
                        if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){ //이심 요청 전

                            return;
                        }else if(orderDto.getEsimIccid()==null ||
                                (orderDto.getEsimIccid().split(",").length != orderDto.getEsimApiRequestId().split(",").length) ||
                                (countOrderWorldMoveEsim < orderDto.getEsimIccid().split(",").length )
                        ){

                            String iccid = "";
                            String rcode = "";
                            for(String orderId : orderDto.getEsimApiRequestId().split(",")){

                                HashMap result = worldMoveUtil.contextLoads3(orderId,orderDto.getId());
                                if(result==null){
                                    return;
                                }
                                if(iccid.equals("")){
                                    iccid = result.get("iccid").toString();
                                }else{
                                    iccid = iccid + "," + result.get("iccid").toString();
                                }

                                if(rcode.equals("")){
                                    rcode = result.get("rcode").toString();
                                }else{
                                    rcode = rcode + "," + result.get("rcode").toString();
                                }
                            }
                            orderDto.setEsimIccid(iccid);
                            orderDto.setEsimRcode(rcode);
                            orderMapper.updateOrderSmsForEsimIccidWordMove(orderDto);
                            return;
                        }
                    } catch (NoSuchAlgorithmException e) {
                        matchInfoDto.setEsimFlag("N");
                    }
                }
            }
        }



    }


}