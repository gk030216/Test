package com.pet.service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface AIService {

    // ============= 用户端方法 =============

    /**
     * 智能问答
     */
    Map<String, Object> smartChat(String question, Integer userId);

    /**
     * 获取用户对话历史
     */
    List<Map<String, Object>> getChatHistory(Integer userId, Integer page, Integer pageSize);

    /**
     * 获取常见问题列表（前台）
     */
    List<Map<String, Object>> getFaqList();

    /**
     * 评价答案
     */
    boolean rateAnswer(Integer historyId, Integer rating);

    /**
     * 清空对话历史
     */
    boolean clearHistory(Integer userId);

    // ============= 知识库管理 =============

    /**
     * 获取知识库列表（分页）
     */
    Map<String, Object> getKnowledgeList(Integer page, Integer pageSize, String keyword,
                                         String category, Integer source, Integer status);

    /**
     * 添加知识库
     */
    boolean addKnowledge(Map<String, Object> knowledge);

    /**
     * 更新知识库
     */
    boolean updateKnowledge(Map<String, Object> knowledge);

    /**
     * 更新知识库状态
     */
    boolean updateKnowledgeStatus(Integer id, Integer status);

    /**
     * 删除知识库
     */
    boolean deleteKnowledge(Integer id);

    /**
     * 搜索知识库
     */
    List<Map<String, Object>> searchKnowledge(String keyword, Integer limit);

    // ============= 常见问题管理 =============

    /**
     * 获取常见问题列表（后台分页）
     */
    Map<String, Object> getFaqList(Integer page, Integer pageSize, String keyword);

    /**
     * 添加常见问题
     */
    boolean addFaq(Map<String, Object> faq);

    /**
     * 更新常见问题
     */
    boolean updateFaq(Map<String, Object> faq);

    /**
     * 更新常见问题状态
     */
    boolean updateFaqStatus(Integer id, Integer status);

    /**
     * 删除常见问题
     */
    boolean deleteFaq(Integer id);

    // ============= 对话记录管理 =============

    /**
     * 获取对话记录列表（后台）
     */
    Map<String, Object> getChatHistoryList(Integer page, Integer pageSize, String keyword, Integer answerSource, Integer rating);

    /**
     * 删除对话记录
     */
    boolean deleteChatHistory(Integer id);

    /**
     * 导出对话记录
     */
    void exportChatHistory(String keyword, Integer answerSource, Integer rating, HttpServletResponse response);

    // 添加以下方法
    boolean convertToKnowledge(Map<String, Object> knowledge);
    int batchConvertToKnowledge(List<Map<String, Object>> items);

}

