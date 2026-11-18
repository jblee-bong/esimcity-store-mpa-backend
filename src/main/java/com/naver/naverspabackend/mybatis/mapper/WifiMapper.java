package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.WifiCategoryDto;
import com.naver.naverspabackend.dto.WifiDeviceDto;
import com.naver.naverspabackend.dto.WifiMemberDto;
import com.naver.naverspabackend.dto.WifiOrderDto;
import com.naver.naverspabackend.dto.WifiPointLogDto;
import com.naver.naverspabackend.dto.WifiProductDto;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WifiMapper {

    /* ---------- 회원 ---------- */
    WifiMemberDto findMemberById(Long id);
    WifiMemberDto findMemberByTel(String tel);
    int insertMember(WifiMemberDto dto);
    int activateMember(@Param("id") Long id, @Param("marketingAgreeYn") String marketingAgreeYn);
    int updateLastLogin(Long id);
    int incrementTokenVersion(Long id);
    int addPoint(@Param("id") Long id, @Param("amount") int amount);
    int usePoint(@Param("id") Long id, @Param("amount") int amount);
    int extendPointExpire(@Param("id") Long id, @Param("expireDt") Date expireDt);
    int withdrawMember(Long id);
    List<WifiMemberDto> selectExpiredPointMembers();
    int expirePoint(Long id);
    List<WifiMemberDto> adminMemberList(Map<String, Object> param);
    int adminMemberCount(Map<String, Object> param);

    int insertPointLog(WifiPointLogDto dto);
    List<WifiPointLogDto> pointLogList(Long memberId);

    /* ---------- 장비 ---------- */
    WifiDeviceDto findDevice(String sn);
    int upsertDevice(@Param("sn") String sn, @Param("deviceType") String deviceType, @Param("tugeStatus") String tugeStatus);
    int registerDevice(@Param("sn") String sn, @Param("memberId") Long memberId, @Param("nickname") String nickname);
    int updateNickname(@Param("sn") String sn, @Param("memberId") Long memberId, @Param("nickname") String nickname);
    int releaseDevice(@Param("sn") String sn, @Param("memberId") Long memberId);
    int releaseAllDevices(Long memberId);
    List<WifiDeviceDto> myDevices(Long memberId);
    int insertDeviceLog(@Param("sn") String sn, @Param("memberId") Long memberId, @Param("action") String action, @Param("adminId") String adminId);
    List<WifiDeviceDto> adminDeviceList(Map<String, Object> param);
    int adminDeviceCount(Map<String, Object> param);

    /* ---------- 상품 ---------- */
    WifiProductDto findProduct(String productCode);
    int upsertProductFromTuge(WifiProductDto dto);
    int markMissingProductsInactive(@Param("codes") List<String> codes);
    List<WifiProductDto> adminProductList(Map<String, Object> param);
    int adminProductCount(Map<String, Object> param);
    List<WifiProductDto> activeProductMcc();

    /* ---------- MCC 나라 표 ---------- */
    List<Map<String, Object>> mccList();
    int countMccDup(@Param("id") Long id, @Param("mcc") String mcc, @Param("iso") String iso);
    int insertMcc(@Param("mcc") String mcc, @Param("iso") String iso, @Param("name") String name);
    int updateMcc(@Param("id") Long id, @Param("mcc") String mcc, @Param("iso") String iso, @Param("name") String name);
    int renameIso(@Param("iso") String iso, @Param("name") String name);
    int deleteMcc(Long id);
    int updateProductSale(WifiProductDto dto);
    List<WifiProductDto> userProductList(@Param("categoryId") Long categoryId, @Param("deviceType") String deviceType);

    /* ---------- 카테고리 ---------- */
    List<WifiCategoryDto> adminCategoryList();
    List<WifiCategoryDto> userCategoryList(@Param("deviceType") String deviceType);
    int insertCategory(WifiCategoryDto dto);
    int updateCategory(WifiCategoryDto dto);
    int deleteCategory(Long id);
    int countChildCategories(Long id);
    WifiCategoryDto findCategory(Long id);
    int deleteCategoryMappings(Long categoryId);

    /* ---------- 주문 ---------- */
    int insertOrder(WifiOrderDto dto);
    WifiOrderDto findOrderByNo(String orderNo);
    WifiOrderDto findOrderById(Long id);
    int changePaymentStatus(@Param("id") Long id, @Param("from") String from, @Param("to") String to,
                            @Param("pgTransactionId") String pgTransactionId);
    int updateOrderTopup(WifiOrderDto dto);
    /** earn_point 가 0 일 때만 기록 (중복 적립 방지). 0 rows 면 이미 적립됨 */
    int setOrderEarnPoint(@Param("id") Long id, @Param("earnPoint") int earnPoint);
    List<WifiOrderDto> retryTargetOrders();
    List<WifiOrderDto> myOrders(Long memberId);
    int requestRefund(@Param("id") Long id, @Param("memberId") Long memberId, @Param("reason") String reason);
    /** 누적 환불액 갱신. prev 값이 그대로일 때만 (동시 처리 방지) */
    int markTugeCanceled(@Param("id") Long id);

    List<WifiOrderDto> planCheckTargets();

    int updatePlan(@Param("id") Long id, @Param("status") String status, @Param("start") String start, @Param("end") String end);

    int markExpireNotified(@Param("id") Long id);

    int completeRefund(@Param("id") Long id, @Param("from") String from, @Param("to") String to,
                       @Param("prevKrw") int prevKrw, @Param("prevPoint") int prevPoint,
                       @Param("refundKrw") int refundKrw, @Param("refundPoint") int refundPoint, @Param("memo") String memo);
    int rejectRefund(@Param("id") Long id, @Param("memo") String memo);
    List<WifiOrderDto> adminOrderList(Map<String, Object> param);
    int adminOrderCount(Map<String, Object> param);
}
