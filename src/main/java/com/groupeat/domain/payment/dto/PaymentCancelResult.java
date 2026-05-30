package com.groupeat.domain.payment.dto;

import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Comparator;

public record PaymentCancelResult(
        Integer refundedAmount,
        LocalDateTime canceledAt,
        String lastTransactionKey,
        Boolean canceled
) {
    public static PaymentCancelResult skipped() {
        return new PaymentCancelResult(0, null, null, false);
    }

    public static PaymentCancelResult from(TossPaymentConfirmResponse response, Integer refundedAmount) {
        LocalDateTime fallbackCanceledAt = LocalDateTime.now();

        if (response.cancels() == null || response.cancels().isEmpty()) {
            return new PaymentCancelResult(
                    refundedAmount,
                    fallbackCanceledAt,
                    response.lastTransactionKey(),
                    true
            );
        }

        TossPaymentConfirmResponse.Cancel lastCancel = response.cancels().stream()
                .max(Comparator.comparing(
                        TossPaymentConfirmResponse.Cancel::canceledAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ))
                .orElse(null);

        return new PaymentCancelResult(
                refundedAmount,
                lastCancel != null && lastCancel.canceledAt() != null
                        ? toLocalDateTime(lastCancel.canceledAt())
                        : fallbackCanceledAt,
                lastCancel != null ? lastCancel.transactionKey() : response.lastTransactionKey(),
                true
        );
    }

    private static LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}
