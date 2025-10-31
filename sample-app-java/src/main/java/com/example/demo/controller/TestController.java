package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 순수 Java로 작성된 테스트 컨트롤러
 * TraceLens가 Java 프로젝트에서도 정상 작동하는지 확인
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/simple")
    public String simpleTest() {
        logger.info("Simple test API called (Java)");
        logger.debug("Debug level log from Java");
        logger.warn("Warning level log from Java");
        return "Test completed (Java)";
    }

    @GetMapping("/error")
    public Map<String, Object> errorTest() {
        logger.info("Error test API called (Java)");

        try {
            logger.debug("Intentionally throwing exception (Java)...");
            throw new IllegalStateException("Test exception from Java!");
        } catch (Exception e) {
            logger.error("Exception occurred in Java controller", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    @PostMapping("/mail")
    public Map<String, Object> sendMail(@RequestBody MailRequest request) {
        logger.info("Mail send request (Java) - to: {}, subject: {}",
                    request.getTo(), request.getSubject());

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Email validation
            logger.debug("Validating email address (Java)...");
            if (!request.getTo().contains("@")) {
                logger.error("Invalid email address (Java): {}", request.getTo());
                response.put("success", false);
                response.put("message", "Invalid email address");
                return response;
            }
            logger.info("Email validation completed (Java)");

            // 2. SMTP connection simulation
            logger.debug("Connecting to SMTP server (Java)...");
            Thread.sleep(100);

            if (request.getTo().contains("fail")) {
                logger.error("SMTP connection failed (Java): Connection timeout");
                throw new RuntimeException("SMTP server connection failed");
            }
            logger.info("SMTP server connected (Java)");

            // 3. Send mail
            logger.info("Sending mail (Java)...");
            Thread.sleep(200);
            logger.info("Mail sent successfully (Java)!");

            response.put("success", true);
            response.put("message", "Mail sent successfully from Java");
            response.put("recipient", request.getTo());
            return response;

        } catch (Exception e) {
            logger.error("Error sending mail (Java)", e);
            response.put("success", false);
            response.put("message", "Mail send failed: " + e.getMessage());
            return response;
        }
    }

    // Inner class for request body
    public static class MailRequest {
        private String to;
        private String subject;
        private String body;

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
