package com.pet.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pet.config.AIConfig;
import com.pet.mapper.AIKnowledgeMapper;
import com.pet.mapper.ChatHistoryMapper;
import com.pet.mapper.AIFaqMapper;
import com.pet.service.AIService;
import com.pet.util.ExcelUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private AIConfig aiConfig;

    @Autowired
    private AIKnowledgeMapper knowledgeMapper;

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Autowired
    private AIFaqMapper faqMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 系统提示词
    private static final String SYSTEM_PROMPT =
            "你是一个专业的宠物顾问，名字叫\"宠小伴\"。你的职责是：\n" +
                    "1. 回答关于宠物饲养、健康、训练、饮食等方面的问题\n" +
                    "2. 用友好、温暖、专业的语气回复\n" +
                    "3. 如果用户问的问题与宠物无关，礼貌地引导回宠物话题\n" +
                    "4. 回答要简洁明了，每段话不超过150字\n" +
                    "5. 可以适当使用表情符号增加亲和力，如🐶🐱🐾❤️";

    // 敏感词过滤
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(政治|色情|暴力|赌博|毒品|违禁|敏感)"
    );

    @Override
    public Map<String, Object> smartChat(String question, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        String sessionId = UUID.randomUUID().toString();
        String answer = null;
        Integer knowledgeId = null;
        Integer answerSource = 1;

        System.out.println("========== 智能问答开始 ==========");
        System.out.println("用户问题: " + question);
        System.out.println("用户ID: " + userId);

        // 1. 敏感词过滤
        if (SENSITIVE_PATTERN.matcher(question).find()) {
            System.out.println("检测到敏感词，使用默认回复");
            answer = "抱歉，您的问题涉及敏感内容，我无法回答。请咨询其他宠物相关问题 🐾";
            answerSource = 1;
        }
        // 2. 先查询本地知识库
        else if (aiConfig.isLocalEnabled()) {
            System.out.println("开始搜索本地知识库...");
            System.out.println("用户问题: " + question);

            Map<String, Object> knowledge = knowledgeMapper.searchBestMatch(question);
            System.out.println("知识库搜索结果: " + knowledge);

            if (knowledge != null && knowledge.get("score") != null) {
                double score = 0;
                Object scoreObj = knowledge.get("score");

                // 增强分数解析逻辑
                if (scoreObj instanceof Integer) {
                    score = ((Integer) scoreObj).doubleValue();
                } else if (scoreObj instanceof Long) {
                    score = ((Long) scoreObj).doubleValue();
                } else if (scoreObj instanceof Double) {
                    score = (Double) scoreObj;
                } else if (scoreObj instanceof Float) {
                    score = ((Float) scoreObj).doubleValue();
                } else if (scoreObj instanceof BigDecimal) {
                    score = ((BigDecimal) scoreObj).doubleValue();
                } else {
                    // 尝试字符串解析
                    try {
                        score = Double.parseDouble(scoreObj.toString());
                    } catch (NumberFormatException e) {
                        score = 0;
                    }
                }

                System.out.println("匹配分数: " + score + ", 阈值: " + aiConfig.getMatchThreshold());

                if (score >= aiConfig.getMatchThreshold()) {
                    answer = (String) knowledge.get("content");
                    knowledgeId = (Integer) knowledge.get("id");
                    answerSource = 1;
                    knowledgeMapper.incrementUseCount(knowledgeId);
                    System.out.println("使用知识库答案，知识库ID: " + knowledgeId);
                    System.out.println("知识库答案内容: " + answer);
                } else {
                    System.out.println("匹配分数低于阈值，将调用AI API");
                }
            } else {
                System.out.println("未找到匹配的知识库条目");
            }
        }

        // 3. 本地无匹配，调用AI API
        if (answer == null) {
            System.out.println("调用DeepSeek API...");
            answer = callDeepSeekAPI(question);
            answerSource = 2;
            System.out.println("AI返回答案: " + answer);
        }

        // 4. 保存对话记录
        Map<String, Object> history = new HashMap<>();
        history.put("userId", userId);
        history.put("sessionId", sessionId);
        history.put("userQuestion", question);
        history.put("answerSource", answerSource);
        history.put("aiAnswer", answer);
        history.put("knowledgeId", knowledgeId);
        chatHistoryMapper.insert(history);

        result.put("answer", answer);
        result.put("source", answerSource == 1 ? "知识库" : "AI智能");
        result.put("historyId", history.get("id"));

        System.out.println("答案来源: " + (answerSource == 1 ? "知识库" : "AI智能"));
        System.out.println("========== 智能问答结束 ==========");

        return result;
    }

    private String callDeepSeekAPI(String question) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(aiConfig.getBaseUrl() + "/v1/chat/completions");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + aiConfig.getApiKey());

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", aiConfig.getModel());

            JSONArray messages = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT);
            messages.add(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);

            httpPost.setEntity(new StringEntity(requestBody.toJSONString(), "UTF-8"));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonResponse = JSON.parseObject(responseBody);

                if (jsonResponse.containsKey("choices")) {
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        return message.getString("content");
                    }
                }
                return "抱歉，AI服务暂时不可用，请稍后再试 🐾";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "网络异常，请稍后再试 🐾";
        }
    }

    @Override
    public List<Map<String, Object>> getChatHistory(Integer userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> rawList = chatHistoryMapper.getByUserId(userId, offset, pageSize);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : rawList) {
            Map<String, Object> converted = new HashMap<>();
            converted.put("id", item.get("id"));
            converted.put("userQuestion", item.get("userQuestion"));
            converted.put("answerSource", item.get("answerSource"));
            converted.put("aiAnswer", item.get("aiAnswer"));
            converted.put("rating", item.get("rating"));
            converted.put("createTime", item.get("createTime") != null ? item.get("createTime").toString() : "");
            result.add(converted);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getFaqList() {
        return faqMapper.getEnabledList();
    }

    @Override
    public List<Map<String, Object>> searchKnowledge(String keyword, Integer limit) {
        return knowledgeMapper.searchByKeyword(keyword, limit);
    }

    @Override
    public boolean addKnowledge(Map<String, Object> knowledge) {
        // 确保必要字段有默认值
        if (!knowledge.containsKey("status") || knowledge.get("status") == null) {
            knowledge.put("status", 1);
        }
        if (!knowledge.containsKey("source") || knowledge.get("source") == null) {
            knowledge.put("source", 1);
        }
        if (!knowledge.containsKey("categoryName") || knowledge.get("categoryName") == null) {
            // 根据category设置分类名称
            String category = (String) knowledge.get("category");
            if ("diet".equals(category)) {
                knowledge.put("categoryName", "饮食");
            } else if ("health".equals(category)) {
                knowledge.put("categoryName", "健康");
            } else if ("training".equals(category)) {
                knowledge.put("categoryName", "训练");
            } else if ("behavior".equals(category)) {
                knowledge.put("categoryName", "行为");
            } else {
                knowledge.put("categoryName", "其他");
            }
        }
        return knowledgeMapper.insert(knowledge) > 0;
    }

    @Override
    public boolean updateKnowledge(Map<String, Object> knowledge) {
        return knowledgeMapper.update(knowledge) > 0;
    }

    @Override
    public boolean deleteKnowledge(Integer id) {
        return knowledgeMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getKnowledgeList(Integer page, Integer pageSize, String keyword,
                                                String category, Integer source, Integer status) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> list = knowledgeMapper.getList(offset, pageSize, keyword, category, source, status);
        int total = knowledgeMapper.countList(keyword, category, source, status);

        // 分类名称映射
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("diet", "饮食");
        categoryMap.put("health", "健康");
        categoryMap.put("training", "训练");
        categoryMap.put("behavior", "行为");
        categoryMap.put("other", "其他");

        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (Map<String, Object> item : list) {
            Map<String, Object> formatted = new LinkedHashMap<>();
            formatted.put("id", item.get("id"));
            formatted.put("title", item.get("title") != null ? item.get("title").toString() : "");
            formatted.put("content", item.get("content") != null ? item.get("content").toString() : "");
            formatted.put("keywords", item.get("keywords") != null ? item.get("keywords").toString() : "");

            String categoryCode = item.get("category") != null ? item.get("category").toString() : "";
            formatted.put("category", categoryCode);
            formatted.put("categoryName", categoryMap.getOrDefault(categoryCode, categoryCode));

            formatted.put("useCount", item.get("useCount") != null ? item.get("useCount") : 0);
            formatted.put("source", item.get("source") != null ? item.get("source") : 1);
            formatted.put("status", item.get("status") != null ? item.get("status") : 1);
            formatted.put("createTime", item.get("createTime") != null ? item.get("createTime").toString() : "");
            formatted.put("updateTime", item.get("updateTime") != null ? item.get("updateTime").toString() : "");
            formattedList.add(formatted);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", formattedList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean rateAnswer(Integer historyId, Integer rating) {
        return chatHistoryMapper.updateRating(historyId, rating) > 0;
    }

    @Override
    public boolean clearHistory(Integer userId) {
        return chatHistoryMapper.deleteByUserId(userId) > 0;
    }

    @Override
    public Map<String, Object> getFaqList(Integer page, Integer pageSize, String keyword) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> list = faqMapper.getList(offset, pageSize, keyword);
        int total = faqMapper.countList(keyword);

        // 格式化日期
        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (Map<String, Object> item : list) {
            Map<String, Object> formatted = new LinkedHashMap<>();
            formatted.put("id", item.get("id"));
            formatted.put("question", item.get("question"));
            formatted.put("answer", item.get("answer"));
            formatted.put("category", item.get("category"));
            formatted.put("sortOrder", item.get("sortOrder"));
            formatted.put("status", item.get("status"));
            formatted.put("createTime", item.get("createTime") != null ? item.get("createTime").toString() : "");
            formattedList.add(formatted);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", formattedList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean addFaq(Map<String, Object> faq) {
        return faqMapper.insert(faq) > 0;
    }

    @Override
    public boolean updateFaq(Map<String, Object> faq) {
        return faqMapper.update(faq) > 0;
    }

    @Override
    public boolean updateFaqStatus(Integer id, Integer status) {
        return faqMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean deleteFaq(Integer id) {
        return faqMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getChatHistoryList(Integer page, Integer pageSize, String keyword, Integer answerSource, Integer rating) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> list = chatHistoryMapper.getAdminList(offset, pageSize, keyword, answerSource, rating);
        int total = chatHistoryMapper.countAdminList(keyword, answerSource, rating);

        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (Map<String, Object> item : list) {
            Map<String, Object> formatted = new LinkedHashMap<>();
            formatted.put("id", item.get("id"));
            formatted.put("userId", item.get("userId"));
            formatted.put("userQuestion", item.get("userQuestion"));
            formatted.put("answerSource", item.get("answerSource"));
            formatted.put("aiAnswer", item.get("aiAnswer"));
            formatted.put("rating", item.get("rating"));
            formatted.put("createTime", item.get("createTime") != null ? item.get("createTime").toString() : "");
            formattedList.add(formatted);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", formattedList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean deleteChatHistory(Integer id) {
        return chatHistoryMapper.deleteById(id) > 0;
    }

    @Override
    public void exportChatHistory(String keyword, Integer answerSource, Integer rating, HttpServletResponse response) {
        try {
            // 获取所有数据（不分页）
            List<Map<String, Object>> list = chatHistoryMapper.getAdminList(0, 9999, keyword, answerSource, rating);

            // 准备导出数据
            List<Map<String, Object>> exportList = new ArrayList<>();
            for (Map<String, Object> item : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", item.get("id"));
                row.put("userName", item.get("userName") != null ? item.get("userName").toString() : "未知用户");
                row.put("userQuestion", item.get("userQuestion") != null ? item.get("userQuestion").toString() : "");
                row.put("aiAnswer", item.get("aiAnswer") != null ? item.get("aiAnswer").toString() : "");

                Integer source = (Integer) item.get("answerSource");
                row.put("answerSourceName", source != null && source == 1 ? "知识库" : "AI智能");

                Integer ratingValue = (Integer) item.get("rating");
                row.put("rating", ratingValue != null ? ratingValue.toString() : "未评分");

                row.put("createTime", item.get("createTime") != null ? item.get("createTime").toString() : "");

                exportList.add(row);
            }

            String[] headers = {"ID", "用户", "用户问题", "AI回答", "答案来源", "评分", "对话时间"};
            String[] fieldNames = {"id", "userName", "userQuestion", "aiAnswer", "answerSourceName", "rating", "createTime"};

            ExcelUtil.exportExcel(response, exportList, headers, fieldNames, "对话记录", "对话记录_" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("导出失败：" + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public boolean updateKnowledgeStatus(Integer id, Integer status) {
        return knowledgeMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean convertToKnowledge(Map<String, Object> knowledge) {
        // 设置默认值
        if (!knowledge.containsKey("status")) {
            knowledge.put("status", 1);
        }
        if (!knowledge.containsKey("source")) {
            knowledge.put("source", 2);  // AI生成待审核
        }
        if (!knowledge.containsKey("useCount")) {
            knowledge.put("useCount", 0);
        }
        return knowledgeMapper.insert(knowledge) > 0;
    }

    @Override
    public int batchConvertToKnowledge(List<Map<String, Object>> items) {
        int successCount = 0;
        for (Map<String, Object> item : items) {
            // 设置默认值
            if (!item.containsKey("status")) {
                item.put("status", 1);
            }
            if (!item.containsKey("source")) {
                item.put("source", 2);
            }
            if (!item.containsKey("useCount")) {
                item.put("useCount", 0);
            }
            if (knowledgeMapper.insert(item) > 0) {
                successCount++;
            }
        }
        return successCount;
    }
}