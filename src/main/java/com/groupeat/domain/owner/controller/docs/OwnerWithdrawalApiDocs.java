package com.groupeat.domain.owner.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "사업자 회원 탈퇴",
        description = "진행 중인 주문과 정산 대기 건이 없는 사업자 회원을 탈퇴 처리합니다. 회원 개인정보와 소셜 연결은 제거하고, 가게는 운영 중단 상태로 soft delete 처리합니다."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "사업자 회원 탈퇴 성공",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(value = """
                                {
                                  "isSuccess": true,
                                  "code": "COMMON200",
                                  "message": "요청이 성공했습니다.",
                                  "data": {
                                    "message": "사업자 회원 탈퇴가 완료되었습니다."
                                  }
                                }
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 정보가 없거나 유효하지 않은 경우",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(value = """
                                {
                                  "isSuccess": false,
                                  "code": "COMMON401",
                                  "message": "인증이 필요합니다.",
                                  "data": null
                                }
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "사업자 회원이 아닌 경우",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(value = """
                                {
                                  "isSuccess": false,
                                  "code": "MEMBER4031",
                                  "message": "사업자 회원만 이용할 수 있습니다.",
                                  "data": null
                                }
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "진행 중인 주문 또는 정산 대기 건이 있어 탈퇴할 수 없는 경우",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "진행 중 주문 존재",
                                        value = """
                                                {
                                                  "isSuccess": false,
                                                  "code": "MEMBER4092",
                                                  "message": "진행 중인 주문이 있어 탈퇴할 수 없습니다.",
                                                  "data": null
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "정산 대기 건 존재",
                                        value = """
                                                {
                                                  "isSuccess": false,
                                                  "code": "MEMBER4093",
                                                  "message": "정산 대기 건이 있어 탈퇴할 수 없습니다.",
                                                  "data": null
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface OwnerWithdrawalApiDocs {
}
