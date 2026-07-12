package com.groupeat.domain.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.groupeat.domain.notification.dto.FcmSendRequest;
import com.groupeat.domain.notification.dto.FcmSendResult;
import com.groupeat.domain.notification.entity.FcmRegistration;
import com.groupeat.domain.notification.enums.FcmPlatform;
import com.groupeat.domain.notification.repository.FcmRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(FirebaseMessaging.class)
@Transactional(readOnly = true)
public class FirebaseFcmMessageSender implements FcmMessageSender {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmRegistrationRepository fcmRegistrationRepository;

    // 회원의 활성 웹 FCM 등록값을 조회해 Firebase로 메시지를 발송
    @Override
    @Transactional
    public FcmSendResult sendToMember(FcmSendRequest request) {
        List<FcmRegistration> registrations = fcmRegistrationRepository
                .findAllByMemberIdAndPlatformAndActiveTrue(request.memberId(), FcmPlatform.WEB);

        if (registrations.isEmpty()) {
            return FcmSendResult.empty();
        }

        List<Message> messages = registrations.stream()
                .map(registration -> toMessage(request, registration.getRegistrationToken()))
                .toList();

        try {
            BatchResponse response = firebaseMessaging.sendEach(messages);
            deactivateInvalidRegistrations(registrations, response.getResponses());
            return new FcmSendResult(registrations.size(), response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.warn(
                    "FCM batch send failed. memberId={}, targetCount={}, errorCode={}, messagingErrorCode={}",
                    request.memberId(),
                    registrations.size(),
                    e.getErrorCode(),
                    e.getMessagingErrorCode(),
                    e
            );
            return new FcmSendResult(registrations.size(), 0, registrations.size());
        }
    }

    private Message toMessage(FcmSendRequest request, String registrationToken) {
        Message.Builder builder = Message.builder()
                .setToken(registrationToken)
                .setNotification(Notification.builder()
                        .setTitle(request.title())
                        .setBody(request.body())
                        .build());

        Map<String, String> data = request.data();
        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        return builder.build();
    }

    // Firebase가 무효하다고 응답한 등록값은 이후 발송 대상에서 제외
    private void deactivateInvalidRegistrations(
            List<FcmRegistration> registrations,
            List<SendResponse> responses
    ) {
        for (int i = 0; i < responses.size(); i++) {
            SendResponse response = responses.get(i);
            if (response.isSuccessful()) {
                continue;
            }

            FirebaseMessagingException exception = response.getException();
            MessagingErrorCode errorCode = exception == null ? null : exception.getMessagingErrorCode();
            if (isInvalidRegistration(errorCode)) {
                registrations.get(i).deactivate();
            }
        }
    }

    // 재시도해도 성공 가능성이 낮은 등록 오류인지 판별
    private boolean isInvalidRegistration(MessagingErrorCode errorCode) {
        return errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
