package com.example.demo.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/test")
class TestController {
    private val logger = LoggerFactory.getLogger(TestController::class.java)

    @GetMapping("/simple")
    fun simpleTest(): String {
        logger.info("간단한 테스트 API 호출됨")
        logger.debug("디버그 레벨 로그")
        logger.warn("경고 레벨 로그")
        return "테스트 완료"
    }

    @GetMapping("/error")
    fun errorTest(): Map<String, Any> {
        logger.info("에러 테스트 API 호출됨")

        try {
            logger.debug("의도적으로 예외를 발생시킵니다...")
            throw IllegalStateException("테스트 예외입니다!")
        } catch (e: Exception) {
            logger.error("예외가 발생했습니다", e)
            return mapOf(
                "success" to false,
                "error" to e.message.orEmpty()
            )
        }
    }

    @GetMapping("/slow")
    fun slowTest(): String {
        logger.info("느린 API 테스트 시작")

        for (i in 1..5) {
            logger.debug("처리 중... ($i/5)")
            Thread.sleep(500)
        }

        logger.info("느린 API 테스트 완료")
        return "완료"
    }

    @GetMapping("/multi-log")
    fun multiLogTest(): String {
        logger.info("=== 다중 로그 테스트 시작 ===")

        logger.debug("Step 1: 초기화")
        logger.debug("Step 2: 데이터 로드")
        logger.info("데이터 로드 완료 (100개 항목)")

        logger.debug("Step 3: 데이터 검증")
        logger.warn("일부 데이터에 경고 발생 (3개 항목)")

        logger.debug("Step 4: 데이터 처리")
        logger.info("데이터 처리 완료")

        logger.debug("Step 5: 결과 저장")
        logger.info("=== 다중 로그 테스트 완료 ===")

        return "다중 로그 테스트 완료"
    }
}
