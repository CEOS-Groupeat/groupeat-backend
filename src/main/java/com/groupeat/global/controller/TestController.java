package com.groupeat.global.controller; // 패키지 경로는 승원님 프로젝트에 맞게 수정하세요!

import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test API", description = "프론트엔드 배포 및 연동 테스트용 API")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Operation(summary = "서버 헬스 체크", description = "서버가 정상적으로 실행 중이며, 통신이 가능한 상태인지 확인합니다.")
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.onSuccess("Groupeat 백엔드 서버가 정상적으로 작동 중입니다! 🚀 (연동 테스트 성공)");
    }
}
