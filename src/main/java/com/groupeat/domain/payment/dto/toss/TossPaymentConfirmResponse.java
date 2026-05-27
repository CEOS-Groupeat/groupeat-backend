package com.groupeat.domain.payment.dto.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentConfirmResponse(
        String mId,
        String version,
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        String lastTransactionKey,
        String method,
        String type,
        String currency,
        Integer totalAmount,
        Integer balanceAmount,
        Integer suppliedAmount,
        Integer vat,
        Integer taxFreeAmount,
        Boolean useEscrow,
        Boolean cultureExpense,
        Boolean isPartialCancelable,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt,
        Card card,
        EasyPay easyPay,
        Receipt receipt,
        Checkout checkout,
        Failure failure,
        List<Cancel> cancels,
        Map<String, Object> metadata
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Card(
            String issuerCode,
            String acquirerCode,
            String number,
            Integer installmentPlanMonths,
            Boolean isInterestFree,
            String interestPayer,
            String approveNo,
            Boolean useCardPoint,
            String cardType,
            String ownerType,
            String acquireStatus,
            Integer amount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EasyPay(
            String provider,
            Integer amount,
            Integer discountAmount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Receipt(
            String url
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Checkout(
            String url
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Failure(
            String code,
            String message
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cancel(
            String cancelReason,
            Integer cancelAmount,
            Integer taxFreeAmount,
            Integer refundableAmount,
            Integer transferDiscountAmount,
            Integer easyPayDiscountAmount,
            OffsetDateTime canceledAt,
            String transactionKey,
            String receiptKey,
            String cancelStatus,
            String cancelRequestId
    ) {
    }
}
