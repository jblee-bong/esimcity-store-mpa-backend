package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.order.OrderService;
import com.naver.naverspabackend.service.papal.PaypalService;
import com.naver.naverspabackend.service.payapp.PayAppService;
import com.naver.naverspabackend.service.payup.PayUpService;
import com.naver.naverspabackend.service.portone.PortOneService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.util.*;
import com.sun.org.apache.xpath.internal.operations.Mod;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/external")
@RequiredArgsConstructor
public class ExternalController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private StoreService storeService;

    @Autowired
    private ApiPurchaseItemService apiPurchaseItemService;

    @Autowired
    private PaypalService paypalService;


    @Autowired
    private PortOneService portOneService;


    @Autowired
    private PayUpService payUpService;

    @Value("${spring.profiles.active}")
    private String active;
    @Value("${paypal.comfirmUrl}")
    private String comfirmUrl;
    @Autowired
    private PayAppService payAppService;

    @GetMapping("/esimdata")
    public String  esimdata(Model model, @RequestParam (name = "id",required = true) String id,  @RequestParam (name = "orderId",required = false) String orderId, @RequestParam(name = "iccid",required = false) String iccid, @RequestParam(name = "type",required = false) String type, @RequestParam(name = "rcode",required = false) String rcode, HttpServletResponse response) throws Exception {

        id = CommonUtil.Base64EncodeToString(id);
        orderId = CommonUtil.Base64EncodeToString(orderId);
        iccid = CommonUtil.Base64EncodeToString(iccid);
        type = CommonUtil.Base64EncodeToString(type);
        rcode = CommonUtil.Base64EncodeToString(rcode);

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

        TsimUtil tsimUtil = EsimUtil.getTsimUtil(apiPurchaseItemService, storeDto, null);
        TugeUtil tugeUtil = EsimUtil.getTugeUtil(storeDto,null);
        WorldMoveUtil worldMoveUtil =  EsimUtil.getWorldMoveUtil(storeDto);
        NizUtil nizUtil =  EsimUtil.getNizUtil(storeDto);
        AirAloUtil airAloUtil  =  EsimUtil.getAirAloUtil(storeDto);

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
            model.addAttribute("esimDescription",orderDto.getEsimDescription());
        }

        String[] productOptions = orderDto.getProductOption().split(" / ");
        String productOption = "";
        if(productOptions.length>1){
            productOption = productOptions[0].replace("Esim 데이터 용량: ","") + " " + productOptions[1];
        }
        if(productOptions.length>2){
            productOption = productOption + " " + productOptions[2];
        }

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

        model.addAttribute("title",productOption);
        return active + "/" + type;
    }

    @GetMapping("/paypal/success")
    public String paypalSuccess(Model model, @RequestParam("token") String token,
                                @RequestParam("PayerID") String payerId, HttpServletResponse response) {

        model.addAttribute("token", token);
        return active + "/redirectHandler"; // confirm 페이지 렌더링
    }
    @PostMapping("/order/redirectHandler")
    public String redirectHandler( @RequestParam("token") String token, HttpServletResponse response) throws Exception {
        // 1. 페이팔에 최종 결제 확정(Capture) 요청
        String successType = paypalService.captureOrder(token);
        return "redirect:/external/order/complete?token=" + token;
    }

    @GetMapping("/order/complete")
    public String orderComplete(Model model, @RequestParam("token") String token, HttpServletResponse response) throws Exception {
        Map<String, String> result = new HashMap<>();
        paypalService.makeModel(token,model);
        return active  + "/paypalSuccess";
    }

    @GetMapping("/paypal/cancel")
    public String paypalCancel(Model model,@RequestParam("token") String orderId, HttpServletResponse response) {

        Map<String, String> result = new HashMap<>();
        return active + "/paypalSuccess";
    }


    @GetMapping("/portone/complete")
    public String portOneComplete(Model model, @RequestParam("paymentId") String token, HttpServletResponse response) throws Exception {
        // 1. 페이팔에 최종 결제 확정(Capture) 요청
        portOneService.captureOrder(model,token);
        return active + "/paypalSuccess";
    }


    @PostMapping("/payapp/completePage")
    public String payappCompletePage(Model model,  HttpServletResponse response) throws Exception {

        return active + "/payappSuccess";
    }

    @PostMapping("/payup/success")
    public String paypalSuccess(Model model, @RequestParam Map<String, String> data ,HttpServletResponse response) {
        payUpService.captureOrder(model,data.get("transactionId"),data.get("orderNumber"),data.get("amount"));
        if(model.getAttribute("topupType")!=null && model.getAttribute("topupType").equals(ApiType.TUGE.name())){
            return active + "/payappSuccess";
        }
        return active +  "/paypalSuccess"; // confirm 페이지 렌더링
    }
}
