package com.groupeat.domain.notification.service;

import com.groupeat.domain.notification.dto.FcmSendRequest;
import com.groupeat.domain.notification.dto.FcmSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpFcmMessageSender implements FcmMessageSender {

    // Firebase 비활성 환경에서는 메시지를 보내지 않고 호출 사실만 기록
    @Override
    public FcmSendResult sendToMember(FcmSendRequest request) {
        log.info("FCM send skipped because Firebase is disabled. memberId={}", request.memberId());
        return FcmSendResult.empty();
    }
}
