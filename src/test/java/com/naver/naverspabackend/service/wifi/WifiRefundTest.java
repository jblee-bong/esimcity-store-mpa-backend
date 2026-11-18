package com.naver.naverspabackend.service.wifi;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naver.naverspabackend.dto.WifiMemberDto;
import com.naver.naverspabackend.dto.WifiOrderDto;
import com.naver.naverspabackend.exception.WifiException;
import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import com.naver.naverspabackend.service.payup.PayUpService;
import com.naver.naverspabackend.service.wifi.impl.WifiAdminServiceImpl;
import com.naver.naverspabackend.util.TugeDeviceUtil;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 관리자 환불 처리: 전액은 PayUp 취소, 일부는 PayUp 부분취소, 적립 포인트는 비율 회수 */
class WifiRefundTest {

    private final WifiMapper mapper = mock(WifiMapper.class);
    private final PayUpService payUp = mock(PayUpService.class);
    private final TugeDeviceUtil tuge = mock(TugeDeviceUtil.class);
    private final WifiAdminServiceImpl svc = new WifiAdminServiceImpl();
    private WifiOrderDto order;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(svc, "wifiMapper", mapper);
        ReflectionTestUtils.setField(svc, "payUpService", payUp);
        ReflectionTestUtils.setField(svc, "tuge", tuge);
        ReflectionTestUtils.setField(svc, "smsSender", mock(WifiSmsSender.class));
        order = new WifiOrderDto();
        order.setId(1L);
        order.setOrderNo("WF1");
        order.setMemberId(7L);
        order.setDeviceSn("SN1");
        order.setTugeOrderNo("T1");
        order.setPgTransactionId("PG1");
        order.setPaymentStatus("PAID");
        order.setTopupStatus("DONE");
        order.setPayKrw(10000);
        order.setPointUsed(2000);
        order.setEarnPoint(300);
        when(mapper.findOrderByNo("WF1")).thenReturn(order);
        WifiMemberDto m = new WifiMemberDto();
        m.setPointBalance(1000);
        when(mapper.findMemberById(7L)).thenReturn(m);
        when(mapper.completeRefund(anyLong(), anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString())).thenReturn(1);
        when(mapper.usePoint(anyLong(), anyInt())).thenReturn(1);
    }

    @Test
    void fullRefundCancelsPgReturnsPointsAndTakesBackEarn() {
        when(payUp.cancelPayment("PG1")).thenReturn("SUCCESS");
        svc.processRefund("WF1", 10000, 2000, "미사용", "admin");
        verify(payUp).cancelPayment("PG1");
        verify(mapper).completeRefund(eq(1L), eq("PAID"), eq("REFUNDED"), eq(0), eq(0), eq(10000), eq(2000), anyString());
        verify(mapper).addPoint(7L, 2000);
        verify(mapper).usePoint(7L, 300);
    }

    @Test
    void partialRefundFailureChangesNothing() {
        when(payUp.partCancelPayment("PG1", 4000, "포켓와이파이 부분환불")).thenReturn(null);
        assertThrows(WifiException.class, () -> svc.processRefund("WF1", 4000, 0, "100MB 사용", "admin"));
        verify(mapper, never()).completeRefund(anyLong(), anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    void partialRefundUsesPartCancelAndProportionalEarnTakeBack() {
        when(payUp.partCancelPayment("PG1", 4000, "포켓와이파이 부분환불")).thenReturn("P2");
        svc.processRefund("WF1", 4000, 0, "100MB 사용, TUGE 협의", "admin");
        verify(payUp, never()).cancelPayment(anyString());
        verify(mapper).completeRefund(eq(1L), eq("PAID"), eq("PARTIAL_REFUNDED"), eq(0), eq(0), eq(4000), eq(0), anyString());
        verify(mapper).usePoint(7L, 120); // 300 x 4000/10000
    }

    @Test
    void secondPartialRefundCoversTheRestWithPartCancel() {
        order.setPaymentStatus("PARTIAL_REFUNDED");
        order.setRefundKrw(4000);
        when(payUp.partCancelPayment("PG1", 6000, "포켓와이파이 부분환불")).thenReturn("P3");
        svc.processRefund("WF1", 6000, 2000, "나머지", "admin");
        verify(payUp, never()).cancelPayment(anyString()); // 이미 부분취소한 결제는 남은 금액도 부분취소
        verify(mapper).completeRefund(eq(1L), eq("PARTIAL_REFUNDED"), eq("REFUNDED"), eq(4000), eq(0), eq(10000), eq(2000), anyString());
    }

    @Test
    void refundOverRemainingRejected() {
        order.setPaymentStatus("PARTIAL_REFUNDED");
        order.setRefundKrw(4000);
        assertThrows(WifiException.class, () -> svc.processRefund("WF1", 6001, 0, "", "admin"));
        verify(payUp, never()).partCancelPayment(anyString(), anyInt(), anyString());
    }

    @Test
    void refundDoesNotTouchTuge() {
        when(payUp.cancelPayment("PG1")).thenReturn("SUCCESS");
        svc.processRefund("WF1", 10000, 2000, "미사용", "admin");
        verify(tuge, never()).cancelOrder(anyString(), anyString());
    }

    @Test
    void tugeCancelSeparateFromRefund() {
        ReflectionTestUtils.setField(svc, "redisTemplate", mock(org.springframework.data.redis.core.RedisTemplate.class));
        Map<String, Object> fail = new HashMap<>();
        fail.put("code", "2004");
        when(tuge.cancelOrder("T1", "SN1")).thenReturn(fail);
        assertThrows(WifiException.class, () -> svc.cancelTugeOrder("WF1", "admin"));
        verify(mapper, never()).markTugeCanceled(anyLong());

        Map<String, Object> ok = new HashMap<>();
        ok.put("code", "0000");
        when(tuge.cancelOrder("T1", "SN1")).thenReturn(ok);
        svc.cancelTugeOrder("WF1", "admin");
        verify(mapper).markTugeCanceled(1L);
        verify(payUp, never()).cancelPayment(anyString());

        order.setTugeCancelDt(new java.util.Date());
        assertThrows(WifiException.class, () -> svc.cancelTugeOrder("WF1", "admin")); // 두 번은 안 됨
    }

    @Test
    void refundOverPaidAmountRejected() {
        assertThrows(WifiException.class, () -> svc.processRefund("WF1", 10001, 0, "", "admin"));
        verify(mapper, never()).completeRefund(anyLong(), anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any());
    }
}
