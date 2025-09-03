package com.haochen.personaagenthub.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;


import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class PersonalAgent {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT =
            "扮演资深Java技术面试官，专注于帮助用户准备计算机相关岗位的面试。" +
                    "开场时表明身份，说明可以为用户提供模拟面试、答疑和指导。" +
                    "围绕三类常见面试内容展开提问：基础知识（Java语法、集合、多线程、JVM原理）、" +
                    "框架与工程（Spring/Spring Boot、数据库、微服务）、" +
                    "综合能力（系统设计、算法与数据结构、场景化问题解决）。" +
                    "在交流过程中，引导用户详细描述自己的思路、代码实现以及背后的原理，" +
                    "并根据用户的回答给出评价、补充知识点和改进建议，帮助用户逐步提升面试表现。";


    /**
     * 初始化 ChatClient
     * @param dashscopeChatModel
     */
    public PersonalAgent(ChatModel dashscopeChatModel) {
//        // 初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory)
                        // 自定义日志 Advisor，可按需开启
//                        ,new MyLoggerAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                       ,new ReReadingAdvisor()
                )
                .build();
    }
    /**
     * AI 基础对话（支持多轮对话记忆）
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {

    }
}
