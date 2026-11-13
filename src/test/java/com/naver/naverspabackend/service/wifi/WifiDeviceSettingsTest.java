package com.naver.naverspabackend.service.wifi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naver.naverspabackend.dto.WifiDeviceDto;
import com.naver.naverspabackend.dto.WifiMemberDto;
import com.naver.naverspabackend.exception.WifiException;
import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import com.naver.naverspabackend.service.wifi.impl.WifiServiceImpl;
import com.naver.naverspabackend.util.TugeDeviceUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 장비 설정 변경: 바꿀 값만 덮어쓰고 나머지는 현재 값 유지, 입력 오류면 인증번호를 쓰지 않음 */
class WifiDeviceSettingsTest {

    private final WifiMapper mapper = mock(WifiMapper.class);
    private final TugeDeviceUtil tuge = mock(TugeDeviceUtil.class);
    private final WifiAuthService auth = mock(WifiAuthService.class);
    private final WifiServiceImpl svc = new WifiServiceImpl();
    private final WifiMemberDto member = new WifiMemberDto();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(svc, "wifiMapper", mapper);
        ReflectionTestUtils.setField(svc, "tuge", tuge);
        ReflectionTestUtils.setField(svc, "authService", auth);
        ReflectionTestUtils.setField(svc, "redisTemplate", mock(org.springframework.data.redis.core.RedisTemplate.class));
        member.setId(7L);
        member.setTel("01012345678");
        WifiDeviceDto d = new WifiDeviceDto();
        d.setSn("SN123456");
        d.setMemberId(7L);
        when(mapper.findDevice("SN123456")).thenReturn(d);
        Map<String, Object> props = new HashMap<>();
        props.put("deviceNo", "SN123456");
        props.put("ssid", "T8-5F2A");
        props.put("wifiPwd", "oldpass12");
        props.put("deviceLang", "JP");
        props.put("blackList", "Y");
        props.put("cardLabel", "A");
        Map<String, Object> res = new HashMap<>();
        res.put("code", "0000");
        res.put("data", Collections.singletonList(props));
        when(tuge.getIndividualDeviceInfo("SN123456")).thenReturn(res);
        Map<String, Object> ok = new HashMap<>();
        ok.put("code", "0000");
        when(tuge.updateIndividualDeviceInfo(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(ok);
    }

    @Test
    void passwordOnlyKeepsOtherSettings() {
        svc.updateDeviceSettings(member, "SN123456", "newpass!9", null, null, "123456");
        verify(auth).verifyActionOtp(member, "123456");
        verify(tuge).updateIndividualDeviceInfo("SN123456", "newpass!9", "JP", "A", "Y");
    }

    @Test
    void languageOnlyResendsCurrentPassword() {
        svc.updateDeviceSettings(member, "SN123456", "", "KR", null, "123456");
        verify(tuge).updateIndividualDeviceInfo("SN123456", "oldpass12", "KR", "A", "Y");
    }

    @Test
    void badPasswordRejectedBeforeOtpIsUsed() {
        assertThrows(WifiException.class, () -> svc.updateDeviceSettings(member, "SN123456", "short", null, null, "123456"));
        assertThrows(WifiException.class, () -> svc.updateDeviceSettings(member, "SN123456", "has space1", null, null, "123456"));
        verify(auth, never()).verifyActionOtp(any(), any());
        verify(tuge, never()).updateIndividualDeviceInfo(anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void otherMembersDeviceRejected() {
        WifiMemberDto other = new WifiMemberDto();
        other.setId(8L);
        other.setTel("01099998888");
        assertThrows(WifiException.class, () -> svc.updateDeviceSettings(other, "SN123456", "newpass!9", null, null, "123456"));
        assertThrows(WifiException.class, () -> svc.deviceSettings(other, "SN123456"));
    }

    @Test
    void nicknameRenameAndBlankMeansSn() {
        svc.renameDevice(member, "SN123456", "  일본   여행용 ");
        verify(mapper).updateNickname("SN123456", 7L, "일본 여행용");
        svc.renameDevice(member, "SN123456", "   ");
        verify(mapper).updateNickname("SN123456", 7L, null);
        assertThrows(WifiException.class, () -> svc.renameDevice(member, "SN123456", "가나다라마바사아자차카타파하가나다라마바사"));
    }

    @Test
    void passwordRule() {
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(WifiServiceImpl.class, "validWifiPwd", "abcd1234"));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(WifiServiceImpl.class, "validWifiPwd", "A!b@c#d$e%f^g&h*"));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(WifiServiceImpl.class, "validWifiPwd", "abcd123"));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(WifiServiceImpl.class, "validWifiPwd", "abcd1234abcd12345"));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(WifiServiceImpl.class, "validWifiPwd", "비밀번호12345"));
    }
}
