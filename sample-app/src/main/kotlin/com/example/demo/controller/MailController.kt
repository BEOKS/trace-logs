package com.example.demo.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/mail")
class MailController {
    private val logger = LoggerFactory.getLogger(MailController::class.java)

    @PostMapping("/send")
    fun sendMail(@RequestBody request: MailRequest): Map<String, Any> {
        logger.info("메일 발송 요청 - 수신자: ${request.to}, 제목: ${request.subject}")

        try {
            // 1. 이메일 주소 검증
            logger.debug("이메일 주소 검증 중...")
            if (!request.to.contains("@")) {
                logger.error("유효하지 않은 이메일 주소: ${request.to}")
                return mapOf(
                    "success" to false,
                    "message" to "유효하지 않은 이메일 주소입니다."
                )
            }
            logger.info("이메일 주소 검증 완료")

            // 2. 메일 서버 연결 시뮬레이션
            logger.debug("SMTP 서버 연결 중...")
            Thread.sleep(100)  // 네트워크 지연 시뮬레이션

            if (request.to.contains("fail")) {
                logger.error("SMTP 서버 연결 실패: Connection timeout")
                throw RuntimeException("SMTP 서버 연결 실패")
            }
            logger.info("SMTP 서버 연결 성공")

            // 3. 메일 내용 생성
            logger.debug("메일 내용 생성 중...")
            val emailContent = """
                수신자: ${request.to}
                제목: ${request.subject}
                내용: ${request.body}
            """.trimIndent()
            logger.debug("메일 내용 생성 완료")

            // 4. 메일 발송
            logger.info("메일 발송 중...")
            Thread.sleep(200)  // 발송 시간 시뮬레이션

            if (request.subject.contains("error")) {
                logger.error("메일 발송 실패: Quota exceeded")
                return mapOf(
                    "success" to false,
                    "message" to "일일 발송 한도를 초과했습니다."
                )
            }

            logger.info("메일 발송 성공!")
            return mapOf(
                "success" to true,
                "message" to "메일이 성공적으로 발송되었습니다.",
                "recipient" to request.to
            )

        } catch (e: Exception) {
            logger.error("메일 발송 중 오류 발생", e)
            return mapOf(
                "success" to false,
                "message" to "메일 발송 실패: ${e.message}"
            )
        }
    }

    data class MailRequest(
        val to: String,
        val subject: String,
        val body: String
    )
}
