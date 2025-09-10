package com.haochen.personaagenthub.app;

import com.haochen.personaagenthub.advisor.MyLoggerAdvisor;
import com.haochen.personaagenthub.chatmemory.FileBasedChatMemory;
import com.haochen.personaagenthub.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
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

    // AI 恋爱知识库问答功能

    @Resource
    private VectorStore personnalAgentVectorStore;

    @Resource
    private Advisor personalAgentRagCloudAdvisor;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 初始化 ChatClient
     * @param dashscopeChatModel
     */
    public PersonalAgent(ChatModel dashscopeChatModel) {
//        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(new InMemoryChatMemoryRepository())
//                .maxMessages(20)
//                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                        // 自定义日志 Advisor，可按需开启
                        ,new MyLoggerAdvisor()
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
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .chatResponse();
            String content = chatResponse.getResult().getOutput().getText();
            log.info("content: {}", content);
            return content;
        }
        /**
         * AI 基础对话（支持多轮对话记忆，SSE 流式传输）
         *
         * @param message
         * @param chatId
         * @return
         */
        public Flux<String> doChatByStream(String message, String chatId) {
            return chatClient
                    .prompt()
                    .user(message)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    .stream()
                    .content();
        }

        record PersonalAgentReport(String title, List<String> suggestions) {

        }

        /**
         * AI 面试报告功能（实战结构化输出）
         *
         * @param message
         * @param chatId
         * @return
         */
        public PersonalAgentReport doChatWithReport(String message, String chatId) {
            PersonalAgentReport personalAgentReport = chatClient
                    .prompt()
                    .system(SYSTEM_PROMPT + "每次对话后都要生成面试结果，标题为{用户名}的面试报告，内容为面试情况。如果内容和面试" +
                            "无关，是询问问题之类的则生成对话总结")
                    .user(message)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .entity(PersonalAgentReport.class);
            log.info("loveReport: {}", personalAgentReport);
            return personalAgentReport;
        }

    /**
     * 和 RAG 知识库进行对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用改写后的查询
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 知识库问答
                .advisors(new QuestionAnswerAdvisor(personnalAgentVectorStore))
                // 应用 RAG 检索增强服务（基于云知识库服务）
//                .advisors(personalAgentRagCloudAdvisor)
                // 应用 RAG 检索增强服务（基于 PgVector 向量存储）
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
//                .advisors(
//                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
//                                loveAppVectorStore, "单身"
//                        )
//                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }





}
