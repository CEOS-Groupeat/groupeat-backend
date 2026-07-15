package com.groupeat.domain.notification.service.fcm;

import com.groupeat.domain.notification.dto.FcmSendRequest;
import com.groupeat.domain.notification.dto.FcmSendResult;

public interface FcmMessageSender {

    FcmSendResult sendToMember(FcmSendRequest request);
}
