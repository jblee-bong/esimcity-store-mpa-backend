-- TB_MATCH_INFO eSIM 메일 템플릿(e_mail_contents) 업데이트
-- 실행 전 SELECT로 대상 확인하세요.

SELECT id, store_id, origin_product_no, option_id,
       LEFT(e_mail_contents, 80) AS preview
FROM TB_MATCH_INFO
WHERE del_yn = 'N'
  AND e_mail_contents IS NOT NULL
  AND e_mail_contents <> '';

-- 백업(선택)
-- CREATE TABLE TB_MATCH_INFO_E_MAIL_BAK_20261108 AS
-- SELECT id, e_mail_contents FROM TB_MATCH_INFO WHERE del_yn='N';

UPDATE TB_MATCH_INFO
SET e_mail_contents = '<p><img style="display: block; width: 0px; height: 0px; visibility: hidden; opacity: 0; border: 0; margin: 0;" src="https://esputnik.com/repository/applications/commons/hidden.png?iid=9ae9d450-2404-11f0-87a0-6bfff370e2e7" alt="" width="1" height="1" /></p>
<table style="background-color: #f8fafc;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td style="padding: 40px 20px;" align="center">
<table style="max-width: 600px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);" role="presentation" border="0" width="600" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 30px; text-align: center;"><img style="display: block; margin: 0 auto; max-width: 200px; height: auto;" src="https://www.esimcity.co.kr/logo.png" alt="이심시티 로고" width="200" height="auto" /></td>
</tr>
<tr>
<td style="padding: 50px 40px 30px; text-align: center; background-color: #ffffff;">
<h1 style="margin: 0 0 15px; font-size: 28px; font-weight: bold; color: #1e293b; line-height: 1.3;">안녕하세요, ${ordererName}님! 👋</h1>
<p style="margin: 0; font-size: 16px; color: #64748b; line-height: 1.6;">이심시티 eSIM 구매가 완료되었습니다.<br />아래 정보로 바로 설치하실 수 있습니다.</p>
</td>
</tr>

<!-- 원클릭 설치 -->
<tr>
<td style="padding: 0 40px 30px; background-color: #ffffff;">
<table style="background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%); border-radius: 12px; padding: 25px;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td>
<h3 style="margin: 0 0 10px; font-size: 18px; font-weight: 600; color: #1e293b; text-align: center;">⚡ 원클릭 설치</h3>
<p style="margin: 0 0 18px; font-size: 13px; color: #64748b; text-align: center; line-height: 1.5;">Wi-Fi 등 인터넷 연결 상태에서만 가능합니다. 안 되면 아래 QR/수동 설치를 이용해 주세요.</p>
<table role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td style="padding-right: 6px;" width="50%"><a style="display: block; background-color: #111827; color: #ffffff; text-decoration: none; padding: 14px; border-radius: 8px; text-align: center; font-size: 15px; font-weight: 700;" href="${iosInstallUrl}">아이폰</a></td>
<td style="padding-left: 6px;" width="50%"><a style="display: block; background-color: #2563eb; color: #ffffff; text-decoration: none; padding: 14px; border-radius: 8px; text-align: center; font-size: 15px; font-weight: 700;" href="${androidInstallUrl}">안드로이드</a></td>
</tr>
</tbody>
</table>
</td>
</tr>
</tbody>
</table>
</td>
</tr>

<tr>
<td style="padding: 0 40px 40px; background-color: #ffffff;">
<table style="background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%); border-radius: 12px; padding: 30px; text-align: center;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td style="padding-bottom: 20px;">
<h2 style="margin: 0; font-size: 20px; font-weight: 600; color: #1e293b;">📱 eSIM QR 코드</h2>
</td>
</tr>
<tr>
<td style="padding: 20px 0;">${qrImage}</td>
</tr>
<tr>
<td style="padding-top: 20px; border-top: 1px solid #e2e8f0;">
<p style="margin: 0; font-size: 14px; color: #64748b; line-height: 1.6;"><strong style="color: #1e293b;">ICCID:</strong> ${iccid}</p>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
<tr>
<td style="padding: 0 40px 30px;">
<table style="background-color: #ffffff; border: 2px solid #e2e8f0; border-radius: 12px; overflow: hidden;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td style="background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); padding: 20px 25px;">
<h3 style="margin: 0; font-size: 18px; font-weight: 600; color: #ffffff;">📦 구매 정보</h3>
</td>
</tr>
<tr>
<td style="padding: 25px;">
<table role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td style="padding-bottom: 15px; border-bottom: 1px solid #e2e8f0;">
<p style="margin: 0 0 8px; font-size: 13px; color: #94a3b8; font-weight: 500;">고객명</p>
<p style="margin: 0; font-size: 16px; color: #1e293b; font-weight: 600;">${ordererName}</p>
</td>
</tr>
<tr>
<td style="padding-bottom:15px;padding-top: 15px;border-bottom: 1px solid #e2e8f0;">
<p style="margin: 0 0 8px; font-size: 13px; color: #94a3b8; font-weight: 500;">상품명</p>
<p style="margin: 0; font-size: 16px; color: #1e293b; font-weight: 600; line-height: 1.5;">${orderTitle}</p>
</td>
</tr>
<tr>
<td style="padding-top: 15px;">
<p style="margin: 0 0 8px; font-size: 13px; color: #94a3b8; font-weight: 500;">옵션명</p>
<p style="margin: 0; font-size: 16px; color: #1e293b; font-weight: 600; line-height: 1.5;">${orderRealName}</p>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
<tr>
<td style="padding: 0 40px 30px;">
<table style="background-color: #f8fafc; border-radius: 12px; padding: 25px;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td>
<h3 style="margin: 0 0 15px; font-size: 16px; font-weight: 600; color: #1e293b;">🔧 QR코드 스캔이 어려우신가요?</h3>
<p style="margin: 0 0 20px; font-size: 14px; color: #64748b; line-height: 1.6;">수동 설치 정보를 사용하여 직접 입력하실 수 있습니다.</p>
<table style="margin-bottom: 20px; background-color: #ffffff; border-radius: 8px; padding: 15px;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td>
<p style="margin: 0 0 10px; font-size: 14px; font-weight: 600; color: #1e293b;">📱 아이폰용</p>
<p style="margin: 0 0 5px; font-size: 13px; color: #64748b;">SM-DP+ 주소:</p>
<p style="margin: 0 0 10px; font-size: 14px; color: #2563eb; word-break: break-all;"><strong>${smdp}</strong></p>
<p style="margin: 0; font-size: 13px; color: #64748b;">활성화 코드:</p>
<p style="margin: 5px 0 0; font-size: 14px; color: #1e293b; word-break: break-all;"><strong>${ipActivityCode}</strong></p>
</td>
</tr>
</tbody>
</table>
<table style="background-color: #ffffff; border-radius: 8px; padding: 15px;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td>
<p style="margin: 0 0 10px; font-size: 14px; font-weight: 600; color: #1e293b;">🤖 갤럭시용</p>
<p style="margin: 0; font-size: 13px; color: #64748b;">개통 코드:</p>
<p style="margin: 5px 0 0; font-size: 14px; color: #1e293b; word-break: break-all;"><strong>${gaActivityCode}</strong></p>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
<tr>
<td style="padding: 0 40px 30px;">
<table style="background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%); border-radius: 12px; padding: 25px;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td>
<h3 style="margin: 0 0 20px; font-size: 18px; font-weight: 600; color: #1e293b;">⚡ 설치 방법</h3>
<p style="margin: 0 0 15px; font-size: 14px; color: #0c4a6e; font-weight: 600;">⚠️ 온라인 네트워크 상태(Wi-Fi 등 인터넷 연결)에서만 진행 가능합니다.</p>
<table style="margin-bottom: 20px; background-color: #ffffff; border-radius: 8px; padding: 15px;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td>
<p style="margin: 0 0 8px; font-size: 14px; font-weight: 600; color: #2563eb;">🤖 갤럭시</p>
<p style="margin: 0; font-size: 13px; color: #64748b; line-height: 1.6;">설정 → 연결 → SIM 카드 관리자 → 모바일요금제 추가 → QR코드 스캔 → 데이터로밍 ON</p>
</td>
</tr>
</tbody>
</table>
<table style="background-color: #ffffff; border-radius: 8px; padding: 15px;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td>
<p style="margin: 0 0 8px; font-size: 14px; font-weight: 600; color: #2563eb;">📱 아이폰</p>
<p style="margin: 0; font-size: 13px; color: #64748b; line-height: 1.6;">설정 → 셀룰러 → eSIM 추가 → QR코드 스캔 → 데이터로밍 ON</p>
</td>
</tr>
</tbody>
</table>
<p style="margin: 20px 0 0; font-size: 13px; color: #dc2626; font-weight: 600; line-height: 1.6;">⏱️ QR코드 스캔 후 로딩까지 다소 시간이 걸릴 수 있으니 잠시만 기다려주세요!</p>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
<tr>
<td style="padding: 0 40px 30px;">
<table style="background-color: #fff7ed; border-left: 4px solid #f59e0b; border-radius: 8px; padding: 20px;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td>
<h3 style="margin: 0 0 15px; font-size: 16px; font-weight: 600; color: #92400e;">⚠️ 중요 안내사항</h3>
<ul style="margin: 0; padding-left: 20px; font-size: 13px; color: #78350f; line-height: 1.8;">
<li style="margin-bottom: 8px;">eSIM 사용 가능한 휴대폰 단말기에서만 사용 가능합니다.</li>
<li style="margin-bottom: 8px;"><strong style="color: #dc2626;">eSIM 설치 후 인터넷에 접속해 사용이 시작되면 취소/환불이 불가합니다.</strong></li>
<li style="margin-bottom: 8px;">사용일은 사용하실 국가 현지 네트워크 접속 시점부터 시작됩니다.</li>
<li style="margin-bottom: 8px;">eSIM 설치 후 데이터가 사용되지 않는 경우, 전원을 여러 번 재부팅해주세요.</li>
<li style="margin-bottom: 8px;"><strong style="color: #dc2626;">설치한 eSIM은 절대로 삭제하지 마세요!</strong></li>
<li>QR코드 받은 시점부터 30일 이내에 설치해주세요.</li>
${eSimResetInfo}
${eSimApnInfo}
${eSimChargeInfo}
</ul>
</td>
</tr>
</tbody>
</table>
</td>
</tr>

<!-- FAQ -->
<tr>
<td style="padding: 0 40px 30px;">
<table style="background-color: #ffffff; border: 2px solid #e2e8f0; border-radius: 12px; overflow: hidden;" role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td style="background: linear-gradient(135deg, #0f766e 0%, #0d9488 100%); padding: 20px 25px;">
<h3 style="margin: 0; font-size: 18px; font-weight: 600; color: #ffffff;">❓ 자주 묻는 질문</h3>
</td>
</tr>
<tr>
<td style="padding: 22px 25px;">
<p style="margin: 0 0 6px; font-size: 14px; font-weight: 700; color: #1e293b;">Q. 한국에서 미리 등록해도 되나요?</p>
<p style="margin: 0 0 16px; font-size: 13px; color: #64748b; line-height: 1.6;">네. 출국 전 미리 QR을 스캔해 등록해 두시는 것을 권장합니다. 방문국 도착 후 eSIM 데이터를 켜고 데이터 로밍을 ON 하면 사용이 시작됩니다.</p>

<p style="margin: 0 0 6px; font-size: 14px; font-weight: 700; color: #1e293b;">Q. 현지에서 데이터가 안 터져요.</p>
<p style="margin: 0 0 16px; font-size: 13px; color: #64748b; line-height: 1.6;">데이터 로밍 ON → 주 데이터를 eSIM으로 선택 → 전원 1~2회 재부팅 → 필요 시 네트워크 사업자를 상품 안내의 현지 통신사로 수동 선택해 주세요. 그래도 안 되면 아래 고객센터로 문의해 주세요.</p>

<p style="margin: 0 0 6px; font-size: 14px; font-weight: 700; color: #1e293b;">Q. 이용 기간은 언제부터 계산되나요?</p>
<p style="margin: 0 0 16px; font-size: 13px; color: #64748b; line-height: 1.6;">현지 네트워크에 처음 연결되어 데이터가 되는 시점부터입니다. 한국에서 등록만 해둔 상태에서는 기간이 차감되지 않습니다.</p>

<p style="margin: 0 0 6px; font-size: 14px; font-weight: 700; color: #1e293b;">Q. 이미 사용된 코드라고 나와요.</p>
<p style="margin: 0 0 16px; font-size: 13px; color: #64748b; line-height: 1.6;">한 번 등록된 QR은 재스캔이 불가합니다. [설정] → [셀룰러/모바일]에서 요금제가 이미 추가되어 있는지 확인해 주세요.</p>

<p style="margin: 0 0 6px; font-size: 14px; font-weight: 700; color: #1e293b;">Q. eSIM 프로필을 삭제했어요.</p>
<p style="margin: 0; font-size: 13px; color: #64748b; line-height: 1.6;">삭제 시 재발급·복구가 어렵습니다. 문제 시 삭제하지 말고 고객센터로 문의해 주세요.</p>
</td>
</tr>
</tbody>
</table>
</td>
</tr>

<tr>
<td style="padding: 0 40px 40px;">
<table role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td style="padding-bottom: 20px;" align="center">
<h3 style="margin: 0; font-size: 16px; font-weight: 600; color: #1e293b;">도움이 필요하신가요?</h3>
</td>
</tr>
<tr>
<td>
<table role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td style="padding-right: 10px;" width="50%"><a style="display: block; background-color: #03c75a; color: #ffffff; text-decoration: none; padding: 15px; border-radius: 8px; text-align: center; font-size: 14px; font-weight: 600;" href="https://talk.naver.com/ct/wzzi0ib">네이버 톡톡</a></td>
<td style="padding-left: 10px;" width="50%"><a style="display: block; background-color: #fee500; color: #191919; text-decoration: none; padding: 15px; border-radius: 8px; text-align: center; font-size: 14px; font-weight: 600;" href="https://pf.kakao.com/_eUdxgn/chat">카카오톡 문의</a></td>
</tr>
<tr>
<td style="padding-top: 10px; padding-right: 10px;" width="50%"><a style="display: block; background-color: #667eea; color: #ffffff; text-decoration: none; padding: 15px; border-radius: 8px; text-align: center; font-size: 14px; font-weight: 600;" href="https://www.esimcity.co.kr/esim-installation-faq.html">❓ 자주 묻는 질문</a></td>
<td style="padding-top: 10px; padding-left: 10px;" width="50%"><a style="display: block; background-color: #2563eb; color: #ffffff; text-decoration: none; padding: 15px; border-radius: 8px; text-align: center; font-size: 14px; font-weight: 600;" href="https://www.esimcity.co.kr/#how-it-works">📖 사용 가이드</a></td>
</tr>
<tr>
<td style="padding-top: 10px;" colspan="2"><a style="display: block; background-color: #10b981; color: #ffffff; text-decoration: none; padding: 15px; border-radius: 8px; text-align: center; font-size: 14px; font-weight: 600;" href="${usage2Url}">📊 사용량 확인</a></td>
</tr>
</tbody>
</table>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
<tr>
<td style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 30px; text-align: center;">
<table role="presentation" border="0" width="100%" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td style="padding-bottom: 30px;" align="center"><img style="display: block; margin: 0 auto; max-width: 150px; height: auto; filter: brightness(0) invert(1);" src="https://www.esimcity.co.kr/logo.png" alt="이심시티 로고" width="150" height="auto" /></td>
</tr>
<tr>
<td style="padding-bottom: 20px;" align="center">
<h4 style="margin: 0 0 15px; font-size: 18px; font-weight: 600; color: #ffffff;">고객센터</h4>
<p style="margin: 0 0 8px; font-size: 15px; color: #ffffff; line-height: 1.6;">매일 09:00 - 22:00</p>
<p style="margin: 0 0 8px; font-size: 15px; color: #ffffff; line-height: 1.6;"><a style="color: #ffffff; text-decoration: none;" href="mailto:master@esimcity.com">master@esimcity.com</a></p>
<p style="margin: 0; font-size: 15px; color: #ffffff; line-height: 1.6;"><a style="color: #ffffff; text-decoration: none;" href="https://pf.kakao.com/_eUdxgn/chat">문의하기</a></p>
</td>
</tr>
<tr>
<td style="padding-top: 20px; border-top: 1px solid rgba(255, 255, 255, 0.2);" align="center">
<p style="margin: 0; font-size: 13px; color: rgba(255, 255, 255, 0.8);">&copy; 2026 이심시티. All rights reserved.</p>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
</tbody>
</table>
',
    modify_dt = NOW()
WHERE del_yn = 'N'
  AND e_mail_contents IS NOT NULL
  AND e_mail_contents <> '';

-- 특정 id만:
-- UPDATE TB_MATCH_INFO SET e_mail_contents = '...', modify_dt = NOW() WHERE id = 123;
