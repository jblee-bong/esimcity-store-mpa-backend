package com.naver.naverspabackend.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.OrderWorldmoveEsimDto;
import com.naver.naverspabackend.mybatis.mapper.FileMapper;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileUtil {

    @Autowired
    private FileMapper fileMapper;

    public static final int BUFFER_SIZE = 8192;

    public static final String SEPERATOR = File.separator;

    @Value("${spring.profiles.active}")
    private String env;

    @Value("${server-origin}")
    private String serverOrigin;


    public String uploadQrCodeFile( Map<String, Object> esimMap) throws IOException, WriterException {
        String mainPath = env.equals("cityProd")?"/city/":"";
        String path = "";
        if (!"dev".equals(env)) {
            path = "/home/apache-tomcat-9.0.82/webapps" + mainPath;
        }else{
            path = "C:\\geneuin\\developer\\esimcity\\esimcity-store-mpa-backend\\build\\libs";
        }

        String extension = ".png";
        // order_id + product_no + option_id + UUID.randomUUID()

        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HHmmss");
        String timeStamp = formatter.format(now);

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            // 문자열 길이 범위 내에서 무작위 인덱스 선택
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        String randomCode = sb.toString();

        String filename = env.equals("cityProd")?(UUID.randomUUID().toString().substring(0,4)+randomCode+extension):(timeStamp + UUID.randomUUID().toString().substring(0,5) + extension);

        Map<String, Object> fileMap = new HashMap<>();
        fileMap.put("fileExtention", extension);
        fileMap.put("fileSaveName", filename);
        fileMap.put("fileOriginalName", filename);
        fileMap.put("filePath", path);

        String fileSavePath = path + filename;

        fileMap.put("fileSavePath", fileSavePath );
        fileMap.put("fileType", "IMAGE");

        File saveFolder = new File(path);

        if(!saveFolder.exists()){
            saveFolder.mkdirs();
        }
            //QR 생성
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(Objects.toString(esimMap.get("lpaCode")), BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            //파일 생성
            File saveFile = new File(saveFolder, filename);

            //ImageIO를 사용하여 파일쓰기
            ImageIO.write(bufferedImage, "png", saveFile);
            fileMapper.insertFileInfo(fileMap);


        return mainPath + filename;
    }

    /**
     * 파일을 Download 처리한다.
     *
     * @param response
     * @param where
     * @param serverSubPath
     * @param physicalName
     * @param original
     * @throws Exception
     */
    public static void downloadFile(HttpServletResponse response, String where, String serverSubPath, String physicalName, String original) throws Exception {
        String downFileName = physicalName;

        if (serverSubPath != null && !"".equals(serverSubPath)) {
            downFileName = SEPERATOR + serverSubPath + SEPERATOR + downFileName;
        }

        if (where != null && !"".equals(where)) {
            downFileName = where + downFileName;
        }

        File file = new File(WebUtil.filePathBlackList(downFileName));

        if (!file.exists()) {
            throw new FileNotFoundException(downFileName);
        }

        if (!file.isFile()) {
            throw new FileNotFoundException(downFileName);
        }

        byte[] b = new byte[BUFFER_SIZE];

        original = original.replaceAll("\r", "").replaceAll("\n", "");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + original + "\";");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        BufferedInputStream fin = null;
        BufferedOutputStream outs = null;

        try {
            fin = new BufferedInputStream(new FileInputStream(file));
            outs = new BufferedOutputStream(response.getOutputStream());

            int read = 0;

            while ((read = fin.read(b)) != -1) {
                outs.write(b, 0, read);
            }
        } finally {
            if (outs != null) {
                try {	// 2012.11 KISA 보안조치
                    outs.close();
                } catch (Exception ignore) {
                    // no-op
                }
            }

            if (fin != null) {
                try {	// 2012.11 KISA 보안조치
                    fin.close();
                } catch (Exception ignore) {
                    // no-op
                }
            }
        }
    }

    public String makeQrCode(OrderDto orderDto, Map<String, Object> esimMap) throws IOException, WriterException {
        // qr코드 용
        String lpaCode = "LPA:1${SMDP}${ACTIVATION_CODE}";
        String activation_code = Objects.toString(esimMap.get("activation_code"), "");
        String smdp = Objects.toString(esimMap.get("smdp"), "");

        lpaCode = lpaCode.replace("{SMDP}", smdp).replace("{ACTIVATION_CODE}",activation_code);

        esimMap.put("lpaCode", lpaCode);

        String qrImageUrl = serverOrigin + uploadQrCodeFile( esimMap);

        esimMap.put("qrImageUrl", qrImageUrl);

        return qrImageUrl;
    }

    public String makeQrCodeNiz(HashMap esimMap) {
        esimMap.put("activation_code", esimMap.get("actReqESIMActivationCode").toString());
        esimMap.put("lpaCode", "LPA1:$" + esimMap.get("actReqESIMSMDPAddress").toString() + "$" + esimMap.get("actReqESIMActivationCode").toString());
        esimMap.put("qrImageUrl", esimMap.get("qrcodeURL").toString());
        esimMap.put("smdp",esimMap.get("actReqESIMSMDPAddress").toString());
        try{
            esimMap.put("iccid",esimMap.get("simno").toString());
        }catch (Exception e){
            e.printStackTrace();
        }

        return esimMap.get("qrcodeURL").toString();
    }
    public void makeQrCodeTsim(HashMap esimMap) throws IOException, WriterException {


        List<String> iccids = (List<String>) esimMap.get("operator_iccids");
        if(iccids.size()>0)
            esimMap.put("iccid", iccids.get(0));
        else
            esimMap.put("iccid", "");


        List<String> lpaCode = (List<String>) esimMap.get("lpa_str");
        if(lpaCode.size()>0){
            String[] lapSplits = lpaCode.get(0).split("\\$");
            esimMap.put("smdp", lapSplits[1]);
            esimMap.put("activation_code", lapSplits[2]);
            esimMap.put("lpaCode", lpaCode.get(0));
        }
        else
            esimMap.put("lpaCode", "");

        List<String> qrImgeList = (List<String>) esimMap.get("qrcode");

        String qrImageUrl = serverOrigin + uploadQrCodeFile( esimMap);
        esimMap.put("qrImageUrl", qrImageUrl);
    }


    public void makeQrCodeWorldMove(HashMap esimMap, OrderWorldmoveEsimDto orderWroldMoveEsimUrl, OrderWorldmoveEsimDto orderWroldMoveEsimText, String esimIccid, OrderDto orderDto) throws Exception {

        esimMap.put("iccid", esimIccid);


        String[] lapSplits = orderWroldMoveEsimText.getEsimQrText().split("\\$");
        esimMap.put("smdp", lapSplits[1]);
        esimMap.put("activation_code", lapSplits[2]);
        esimMap.put("lpaCode", orderWroldMoveEsimText.getEsimQrText());


        String qrImageUrl = serverOrigin + uploadQrCodeFile( esimMap);

        esimMap.put("qrImageUrl", qrImageUrl);
    }

    public void makeQrCodeTuge(HashMap esimMap, OrderDto orderDto) throws Exception {
        esimMap.put("iccid", esimMap.get("iccid"));


        String[] lapSplits = esimMap.get("downloadUrl").toString().split("\\$");
        esimMap.put("smdp", lapSplits[1]);
        esimMap.put("activation_code", lapSplits[2]);
        esimMap.put("lpaCode", esimMap.get("downloadUrl").toString());


        String qrImageUrl = serverOrigin + uploadQrCodeFile( esimMap);

        esimMap.put("qrImageUrl", qrImageUrl);

    }

    public void makeQrCodeAirAlo(Map<String, Object> esimMap, OrderDto orderDto) throws IOException, WriterException {
        try{
            HashMap sharingMap = (HashMap) esimMap.get("sharing");
            esimMap.put("cloudLink",sharingMap.get("link").toString());
            esimMap.put("accessCode",sharingMap.get("access_code").toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        esimMap.put("iccid", esimMap.get("iccid"));


        String[] lapSplits = esimMap.get("qrcode").toString().split("\\$");
        esimMap.put("smdp", lapSplits[1]);
        esimMap.put("activation_code", lapSplits[2]);
        esimMap.put("lpaCode", esimMap.get("qrcode").toString());

        String qrImageUrl = serverOrigin + uploadQrCodeFile( esimMap);
        esimMap.put("qrImageUrl",  qrImageUrl);
    }

    public void makeQrCodeBulk(HashMap esimMap, OrderDto orderDto) throws IOException, WriterException {

        esimMap.put("iccid", esimMap.get("bulkIccid"));

        // qr코드 용
        String lpaCode = "LPA:1${SMDP}${ACTIVATION_CODE}";
        esimMap.put("activation_code", esimMap.get("bulkActiveCode"));
        esimMap.put("smdp", esimMap.get("bulkSmdp"));
        String activation_code = Objects.toString(esimMap.get("bulkActiveCode"), "");
        String smdp = Objects.toString(esimMap.get("bulkSmdp"), "");

        lpaCode = lpaCode.replace("{SMDP}", smdp).replace("{ACTIVATION_CODE}",activation_code);

        esimMap.put("lpaCode", lpaCode);

        String qrImageUrl = serverOrigin + uploadQrCodeFile( esimMap);

        esimMap.put("qrImageUrl", qrImageUrl);
    }
}
