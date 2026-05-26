package io.github.popeye0618.springforge.error

enum class CommonErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int
) : ErrorCode {
    INVALID_INPUT("COMMON-4000", "잘못된 입력입니다.", 400),
    UNAUTHORIZED("COMMON-4010", "인증이 필요합니다.", 401),
    FORBIDDEN("COMMON-4030", "접근 권한이 없습니다.", 403),
    RESOURCE_NOT_FOUND("COMMON-4040", "리소스를 찾을 수 없습니다.", 404),
    METHOD_NOT_ALLOWED("COMMON-4050", "허용되지 않은 HTTP 메서드입니다.", 405),
    UNSUPPORTED_MEDIA_TYPE("COMMON-4150", "지원하지 않는 미디어 타입입니다.", 415),
    INTERNAL_SERVER_ERROR("COMMON-5000", "서버 내부 오류가 발생했습니다.", 500)
}