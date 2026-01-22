package com.naver.naverspabackend.controller;

import com.google.gson.Gson;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.dto.TopupOrderDto;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.order.OrderService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.service.topupOrder.TopupOrderService;
import com.naver.naverspabackend.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/ex")
@RequiredArgsConstructor
public class ExController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private TopupOrderService topupOrderService;

    @Autowired
    private ApiPurchaseItemService apiPurchaseItemService;

    @Value("${spring.profiles.active}")
    private String active;

    @GetMapping("/data")
    public String  data(Model model, @RequestParam (name = "exitem",required = true) String exitem, HttpServletResponse response) throws Exception {

        Gson gson = new Gson();
        String deexitem = CommonUtil.Base64EncodeToString(exitem);
        Map<String, String> item = gson.fromJson(deexitem,HashMap.class);
        String id = Objects.toString(item.get("id"), null);
        String orderId = Objects.toString(item.get("orderId"), null);
        String iccid = Objects.toString(item.get("iccid"), null);
        String type = Objects.toString(item.get("type"), null);
        String rcode = Objects.toString(item.get("rcode"), null);
        String esimTranNo = Objects.toString(item.get("esimTranNo"), null);


        try{
            model.addAttribute("realOrderId",id);

            Map<String,Object> result = new HashMap<>();

            Map<String, Object> map = new HashMap<>();
            map.put("id",id);
            OrderDto orderDto = orderService.fetchOrderOnly(map);
            if(orderDto==null){
                throw new Exception();
            }
            TopupOrderDto topupOrderParam =new TopupOrderDto();
            topupOrderParam.setEsimIccid(iccid);
            List<TopupOrderDto> topupOrderDtoList = topupOrderService.findByEsimIccidSuccessCharge(topupOrderParam);
            Map<String, Object> data = new HashMap<>();
            data.put("id",orderDto.getStoreId());
            StoreDto storeDto = storeService.findById(data);

            TsimUtil tsimUtil = EsimUtil.getTsimUtil(apiPurchaseItemService,storeDto,active);
            TugeUtil tugeUtil = EsimUtil.getTugeUtil(storeDto,active);
            WorldMoveUtil worldMoveUtil =  EsimUtil.getWorldMoveUtil(storeDto);
            NizUtil nizUtil =  EsimUtil.getNizUtil(storeDto);
            AirAloUtil airAloUtil  =  EsimUtil.getAirAloUtil(storeDto);
            EsimAccessUtil esimAccessUtil  =  EsimUtil.getEsimAccess(storeDto);


            model.addAttribute("esimCopyWrite",storeDto.getEsimCopyWrite());
            model.addAttribute("esimQuestLink",storeDto.getEsimQuestLink());
            model.addAttribute("esimLogoLink",storeDto.getEsimLogoLink());
            model.addAttribute("esimLogoType",storeDto.getEsimLogoType());

            if(type.equals(ApiType.TUGE.name())){
                //tuge 완료
                ApiPurchaseItemDto param = new ApiPurchaseItemDto();
                param.setApiPurchaseItemProcutId(orderDto.getEsimProductId());
                param.setApiPurchaseItemType(ApiType.TUGE.name());


                ApiPurchaseItemDto apiPurchaseItemDto = apiPurchaseItemService.findById(param);

                if(apiPurchaseItemDto.getApiPurchaseProductType()!=null)
                    model.addAttribute("esimType",apiPurchaseItemDto.getApiPurchaseProductType().equals("DAILY_PACK"));
                else
                    model.addAttribute("esimType",false);
                tugeUtil.contextLoads4(orderId,iccid,model,apiPurchaseItemDto);
                model.addAttribute("esimDescription",orderDto.getEsimDescription());

            }
            else if(type.equals(ApiType.AIRALO.name())){
                airAloUtil.contextLoads4(iccid,model);
                model.addAttribute("esimDescription",orderDto.getEsimDescription());
            }
            else if(type.equals(ApiType.TSIM.name())){
                //TSIM 완료
                tsimUtil.contextLoads4(orderId.toString(),iccid,model);
            } else if(type.equals(ApiType.WORLDMOVE.name())){
                // WORLDMOVE 완료
                worldMoveUtil.contextLoads4(rcode.toString(),iccid, orderDto.getEsimProductId(),model);

                ApiPurchaseItemDto param = new ApiPurchaseItemDto();
                param.setApiPurchaseItemProcutId(orderDto.getEsimProductId());
                param.setApiPurchaseItemType(ApiType.WORLDMOVE.name());
                ApiPurchaseItemDto apiPurchaseItemDto = apiPurchaseItemService.findById(param);

                model.addAttribute("resetTxt",MakeResetTimeUtil.makeWorldMoveResetText(apiPurchaseItemDto));
                model.addAttribute("resetTime",MakeResetTimeUtil.makeWorldMoveResetTime(apiPurchaseItemDto));

                model.addAttribute("esimDescription",orderDto.getEsimDescription());
            }else if(type.equals(ApiType.ESIMACCESS.name())){
                //TSIM 완료
                esimAccessUtil.contextLoads4(esimTranNo,id,iccid,model);
            }

            String[] productOptions = orderDto.getProductOption().split(" / ");
            String productOption = productOptions[0].split(":")[1];
            productOption += productOptions[1].split(":")[1];;
            if(type.equals(ApiType.ESIMACCESS.name())){
                if(model.getAttribute("totalUsage")!=null && !model.getAttribute("totalUsage").equals("")){
                    String totalUsage  = model.getAttribute("totalUsage").toString();
                    if(totalUsage.equals("unlimited")){
                        model.addAttribute("totalUsageTxt",totalUsage);
                    }else{
                        Double totalUsageDouble = Double.parseDouble(totalUsage);
                        if(totalUsageDouble/1000>1){//보여줄떄는 1000으로 나눔 사용자에게 GB단위부터는, 값이 차감되기떄문 (컴퓨터처럼) 1024가 정확하긴하지만 사용자에게는 이렇게 제공
                            model.addAttribute("totalUsageTxt",(totalUsageDouble/1000) + "GB");
                        }else{
                            model.addAttribute("totalUsageTxt",(totalUsageDouble) + "MB");
                        }
                    }
                }else {
                    model.addAttribute("totalUsageTxt","");
                }
                if(model.getAttribute("usage")!=null && !model.getAttribute("usage").equals("")){
                    String usage  = model.getAttribute("usage").toString();
                    if(usage.equals("unlimited")){
                        model.addAttribute("usageTxt",usage);
                    }else{
                        Double usageDouble = Double.parseDouble(usage);
                        if(usageDouble/1000>1){
                            model.addAttribute("usageTxt",(usageDouble/1000) + "GB");
                        }else{
                            model.addAttribute("usageTxt",(usageDouble) + "MB");
                        }
                    }
                }else {
                    model.addAttribute("usageTxt","");
                }
            }else{
                if(model.getAttribute("totalUsage")!=null && !model.getAttribute("totalUsage").equals("")){
                    String totalUsage  = model.getAttribute("totalUsage").toString();
                    if(totalUsage.equals("unlimited")){
                        model.addAttribute("totalUsageTxt",totalUsage);
                    }else{
                        Double totalUsageDouble = Double.parseDouble(totalUsage);
                        if(totalUsageDouble/1024>1){
                            model.addAttribute("totalUsageTxt",(totalUsageDouble/1024) + "GB");
                        }else{
                            model.addAttribute("totalUsageTxt",(totalUsageDouble) + "MB");
                        }
                    }
                }else {
                    model.addAttribute("totalUsageTxt","");
                }
                if(model.getAttribute("usage")!=null && !model.getAttribute("usage").equals("")){
                    String usage  = model.getAttribute("usage").toString();
                    if(usage.equals("unlimited")){
                        model.addAttribute("usageTxt",usage);
                    }else{
                        Double usageDouble = Double.parseDouble(usage);
                        if(usageDouble/1024>1){

                            model.addAttribute("usageTxt",(Math.round((usageDouble/1024) * 100) / 100.0) + "GB");
                        }else{
                            model.addAttribute("usageTxt",(usageDouble) + "MB");
                        }
                    }
                }else {
                    model.addAttribute("usageTxt","");
                }
            }



            if(type.equals(ApiType.TUGE.name())){
                //TUGE일경우 활성화된, 충전내역을 표기해줘야함.
                if(!model.getAttribute("crrentActivity").equals("main")){
                    productOption =  "<p style=\"color: #b3b3b3;\">"+ productOption+"</p>";
                }else{
                    productOption =  "<p>"+ productOption+"</p>";
                }

                if(topupOrderDtoList.size()>0){
                    productOption += "<br/>(충전 내역)<br/>";
                    for(TopupOrderDto topupOrderDto:topupOrderDtoList){
                        if(topupOrderDto.getTopupOrderNo().equals(model.getAttribute("crrentActivity"))){
                            productOption += ("<p>"+topupOrderDto.getProductOption()+"</p>");
                        }else{
                            productOption += ("<p style=\"color: #b3b3b3;\">"+topupOrderDto.getProductOption()+"</p>");
                        }
                    }
                }

            }else{
                if(topupOrderDtoList.size()>0){
                    productOption += "<br/><br/>(충전 내역)";
                    for(TopupOrderDto topupOrderDto:topupOrderDtoList){
                        productOption += ("<br/>" + topupOrderDto.getProductOption());
                    }
                }
            }
            model.addAttribute("title",productOption);


            return active + "/" + type;
        }catch (Exception e){
            e.printStackTrace();
        }

        return active + "/" + "ERROR";
    }

    @GetMapping("/doMail")
    public String  doMail(Model model, @RequestParam (name = "exitem",required = false) String exitem, HttpServletResponse response) throws Exception {

        Gson gson = new Gson();
        String deexitem = CommonUtil.Base64EncodeToString(exitem);
        Map<String, String> item = gson.fromJson(deexitem,HashMap.class);
        String id = Objects.toString(item.get("id"), null);

        try{
            model.addAttribute("realOrderId",id);

            Map<String,Object> result = new HashMap<>();

            Map<String, Object> map = new HashMap<>();
            map.put("id",id);
            OrderDto orderDto = orderService.fetchOrderOnly(map);
            if(orderDto==null){
                throw new Exception();
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id",orderDto.getStoreId());
            StoreDto storeDto = storeService.findById(data);

            model.addAttribute("esimCopyWrite",storeDto.getEsimCopyWrite());
            model.addAttribute("esimQuestLink",storeDto.getEsimQuestLink());
            model.addAttribute("esimLogoLink",storeDto.getEsimLogoLink());
            model.addAttribute("esimLogoType",storeDto.getEsimLogoType());

            return active + "/emailInput";
        }catch (Exception e){
            e.printStackTrace();
        }

        return active + "/" + "accessError";
    }

}
