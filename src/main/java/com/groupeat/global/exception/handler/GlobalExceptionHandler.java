package com.groupeat.global.exception.handler;

import ch.qos.logback.core.status.ErrorStatus;
import com.groupeat.global.apiPayload.ApiResponse;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import com.groupeat.global.apiPayload.code.status.GlobalErrorStatus;
import com.groupeat.global.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================================
    // 1. 커스텀 비즈니스 예외 처리 (우리가 직접 throw new GeneralException 한 경우)
    // =========================================================================
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(GeneralException e, HttpServletRequest request) {
        ErrorReasonDTO errorReason = e.getErrorReasonHttpStatus();

        // 로그 기록 (필요 시 주석 해제하여 사용)
        // log.warn("비즈니스 예외 발생 - URI: {}, Code: {}", request.getRequestURI(), errorReason.getCode());

        return ResponseEntity
                .status(errorReason.getHttpStatus())
                .body(ApiResponse.onFailure(errorReason.getCode(), errorReason.getMessage(), null));
    }

    // =========================================================================
    // 2. DTO 파라미터 유효성 검사 실패 (@Valid, @RequestBody 사용 시)
    // =========================================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        // 어떤 필드에서 어떤 에러가 났는지 모두 수집 (예: "email" -> "이메일 형식이 아닙니다.")
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            String fieldName = fieldError.getField();
            String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("잘못된 입력입니다.");
            // 동일한 필드에 여러 에러가 있을 경우 콤마로 연결
            errors.merge(fieldName, errorMessage, (existing, newMsg) -> existing + ", " + newMsg);
        }

        ErrorReasonDTO errorReason = GlobalErrorStatus._BAD_REQUEST.getReasonHttpStatus();

        return ResponseEntity
                .status(errorReason.getHttpStatus())
                .body(ApiResponse.onFailure(errorReason.getCode(), errorReason.getMessage(), errors));
    }

    // =========================================================================
    // 3. URI 변수 / 쿼리 파라미터 유효성 검사 실패 (@Validated, @PathVariable, @RequestParam)
    // =========================================================================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {

        // 여러 에러 중 첫 번째 에러 메시지만 깔끔하게 추출 (예: "path variable id는 1 이상이어야 합니다.")
        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("유효하지 않은 파라미터입니다.");

        ErrorReasonDTO errorReason = GlobalErrorStatus._BAD_REQUEST.getReasonHttpStatus();

        return ResponseEntity
                .status(errorReason.getHttpStatus())
                .body(ApiResponse.onFailure(errorReason.getCode(), errorReason.getMessage(), errorMessage));
    }

    // =========================================================================
    // 4. 그 외 서버 내부에서 발생하는 모든 시스템 에러
    // =========================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e, HttpServletRequest request) {
        // 서버 콘솔에는 에러의 원인(StackTrace)을 상세히 출력하여 개발자가 버그를 잡을 수 있게 함
        log.error("서버 내부 오류 발생 - URI: {}", request.getRequestURI(), e);

        ErrorReasonDTO errorReason = GlobalErrorStatus._INTERNAL_SERVER_ERROR.getReasonHttpStatus();

        // 프론트엔드로는 내부 구조(DB명, 쿼리 등)가 노출되지 않도록 '안전한 공통 메시지'만 전달
        return ResponseEntity
                .status(errorReason.getHttpStatus())
                .body(ApiResponse.onFailure(errorReason.getCode(), errorReason.getMessage(), null));
    }
}