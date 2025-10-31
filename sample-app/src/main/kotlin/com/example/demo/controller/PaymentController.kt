package com.example.demo.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payment")
class PaymentController {
    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    @PostMapping("/process")
    fun processPayment(@RequestBody request: PaymentRequest): Map<String, Any> {
        logger.info("결제 처리 시작 - 금액: ${request.amount}원, 결제수단: ${request.method}")

        try {
            // 1. 결제 정보 검증
            logger.debug("결제 정보 검증 중...")
            if (request.amount <= 0) {
                logger.warn("유효하지 않은 결제 금액: ${request.amount}")
                return mapOf("success" to false, "message" to "유효하지 않은 금액입니다.")
            }
            logger.info("결제 정보 검증 완료")

            // 2. 결제 수단 확인
            logger.debug("결제 수단 확인 중: ${request.method}")
            when (request.method) {
                "card" -> logger.info("신용카드 결제 선택")
                "bank" -> logger.info("계좌이체 선택")
                else -> {
                    logger.error("지원하지 않는 결제 수단: ${request.method}")
                    return mapOf("success" to false, "message" to "지원하지 않는 결제 수단입니다.")
                }
            }

            // 3. PG사 API 호출 시뮬레이션
            logger.info("PG사 API 호출 중...")
            Thread.sleep(300)  // API 호출 지연 시뮬레이션

            if (request.cardNumber?.contains("0000") == true) {
                logger.error("카드 승인 거절 - 잔액 부족")
                return mapOf(
                    "success" to false,
                    "message" to "카드 승인이 거절되었습니다. (잔액 부족)"
                )
            }

            logger.info("PG사 승인 완료 - 승인번호: ${generateApprovalNumber()}")

            // 4. 데이터베이스 저장 시뮬레이션
            logger.debug("결제 내역 DB 저장 중...")
            Thread.sleep(100)
            logger.info("결제 내역 DB 저장 완료")

            // 5. 영수증 발행
            logger.info("영수증 발행 중...")
            val receiptNumber = generateReceiptNumber()
            logger.info("영수증 발행 완료 - 번호: $receiptNumber")

            logger.info("결제 처리 완료!")
            return mapOf(
                "success" to true,
                "message" to "결제가 완료되었습니다.",
                "receiptNumber" to receiptNumber,
                "amount" to request.amount
            )

        } catch (e: Exception) {
            logger.error("결제 처리 중 오류 발생", e)
            return mapOf(
                "success" to false,
                "message" to "결제 처리 실패: ${e.message}"
            )
        }
    }

    private fun generateApprovalNumber(): String {
        return "AP${System.currentTimeMillis() % 1000000}"
    }

    private fun generateReceiptNumber(): String {
        return "RC${System.currentTimeMillis() % 1000000}"
    }

    data class PaymentRequest(
        val amount: Int,
        val method: String,
        val cardNumber: String? = null
    )
}
