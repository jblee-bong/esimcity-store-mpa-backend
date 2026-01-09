package com.naver.naverspabackend.enums;

/**
 * 예약어 - 치환에 사용됨
 * reserved Word
 */
public enum ReWord {

    /**
     *
     * <img class="CToWUd" src="https://mail.google.com/mail/u/0?ui=2&amp;ik=a6c1febced&amp;attid=0.2&amp;permmsgid=msg-f:1781863209198098672&amp;th=18ba7325e69718f0&amp;view=fimg&amp;fur=ip&amp;sz=s0-l75-ft&amp;attbid=ANGjdJ9QMWvYtEt2m4x7k7KX2FfD0-zdR4kpnojTTYkxNYi6A24OQ_7UEbBY9N478EkwPFdQVCVUaMrdl0cXlvFNMbvVgG-Vko6_nl-uF6yqxLAemddSns9hp9sqqGg&amp;disp=emb&amp;realattid=ii_18ba73146b5ea6e48f82" alt="image.png" data-image-whitelisted="" data-bit="iit" />
     */
    ORDERER_NAME("ordererName", "고객명"),
    ORDERER_TEL("ordererTel", "고객번호"),
    PRODUCT_NAME("productName", "상품명"),
    IP_ACTIVITY_CODE("ipActivityCode", "아이폰활성화코드"),
    GA_ACTIVITY_CODE("gaActivityCode", "갤럭시활성화코드"),
    QR_IMAEG("qrImage", "큐알코드이미지");

    String name;

    String explain;

    ReWord(String name, String explain) {
        this.name = name;
        this.explain = explain;
    }
}
