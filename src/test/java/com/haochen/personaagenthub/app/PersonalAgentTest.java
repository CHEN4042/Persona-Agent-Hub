package com.haochen.personaagenthub.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersonalAgentTest {

    @Resource
    private PersonalAgent personalAgent;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message1 = "你好，我是程序员陈某";
        String answer = personalAgent.doChat(message1,chatId);
        // 第二轮
        String message2 = "我正在准备java面试，我需要准备什么";
        answer = personalAgent.doChat(message2,chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        String message3 = "你能和我解释一下Spring boot吗";
        answer = personalAgent.doChat(message3,chatId);
        Assertions.assertNotNull(answer);
    }
}