package com.pet.controller.admin;

import com.pet.service.AIService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/ai")
@CrossOrigin(origins = "*")
public class AdminAIController {

    @Autowired
    private AIService aiService;

    // ============= 知识库管理 =============

    @GetMapping("/knowledge/list")
    public Result<Map<String, Object>> getKnowledgeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer source,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = aiService.getKnowledgeList(page, pageSize, keyword, category, source, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/knowledge/add")
    public Result<?> addKnowledge(@RequestBody Map<String, Object> knowledge) {
        try {
            boolean success = aiService.addKnowledge(knowledge);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/knowledge/update")
    public Result<?> updateKnowledge(@RequestBody Map<String, Object> knowledge) {
        try {
            boolean success = aiService.updateKnowledge(knowledge);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/knowledge/status")
    public Result<?> updateKnowledgeStatus(@RequestParam Integer id, @RequestParam Integer status) {
        try {
            boolean success = aiService.updateKnowledgeStatus(id, status);
            if (success) {
                return Result.success(status == 1 ? "启用成功" : "禁用成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/knowledge/delete/{id}")
    public Result<?> deleteKnowledge(@PathVariable Integer id) {
        try {
            boolean success = aiService.deleteKnowledge(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/knowledge/batch-delete")
    public Result<?> batchDeleteKnowledge(@RequestParam String ids) {
        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int successCount = 0;
            for (Integer id : idList) {
                if (aiService.deleteKnowledge(id)) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取有对话记录的用户列表
     */
    @GetMapping("/history/users")
    public Result<List<Map<String, Object>>> getUserListWithHistory(
            @RequestParam(required = false) String keyword) {
        try {
            List<Map<String, Object>> userList = aiService.getUserListWithHistory(keyword);
            return Result.success(userList);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 常见问题管理 =============

    @GetMapping("/faq/list")
    public Result<Map<String, Object>> getFaqList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = aiService.getFaqList(page, pageSize, keyword, category, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/faq/add")
    public Result<?> addFaq(@RequestBody Map<String, Object> faq) {
        try {
            boolean success = aiService.addFaq(faq);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/faq/update")
    public Result<?> updateFaq(@RequestBody Map<String, Object> faq) {
        try {
            boolean success = aiService.updateFaq(faq);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/faq/status")
    public Result<?> updateFaqStatus(@RequestParam Integer id, @RequestParam Integer status) {
        try {
            boolean success = aiService.updateFaqStatus(id, status);
            if (success) {
                return Result.success(status == 1 ? "启用成功" : "禁用成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/faq/delete/{id}")
    public Result<?> deleteFaq(@PathVariable Integer id) {
        try {
            boolean success = aiService.deleteFaq(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/faq/batch-delete")
    public Result<?> batchDeleteFaq(@RequestParam String ids) {
        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int successCount = 0;
            for (Integer id : idList) {
                if (aiService.deleteFaq(id)) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 对话记录管理 =============

    @GetMapping("/history/list")
    public Result<Map<String, Object>> getChatHistoryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer answerSource,
            @RequestParam(required = false) Integer rating) {
        try {
            Map<String, Object> result = aiService.getChatHistoryList(page, pageSize, keyword, answerSource, rating);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/history/delete/{id}")
    public Result<?> deleteChatHistory(@PathVariable Integer id) {
        try {
            boolean success = aiService.deleteChatHistory(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/history/batch-delete")
    public Result<?> batchDeleteChatHistory(@RequestParam String ids) {
        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int successCount = 0;
            for (Integer id : idList) {
                if (aiService.deleteChatHistory(id)) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/history/export")
    public void exportChatHistory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer answerSource,
            @RequestParam(required = false) Integer rating,
            HttpServletResponse response) {
        try {
            aiService.exportChatHistory(keyword, answerSource, rating, response);
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

    /**
     * 单条对话记录转为知识库
     */
    @PostMapping("/history/convert")
    public Result<?> convertToKnowledge(@RequestBody Map<String, Object> params) {
        try {
            boolean success = aiService.convertToKnowledge(params);
            if (success) {
                return Result.success("已添加到知识库，请审核");
            } else {
                return Result.error("转换失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量转换对话记录为知识库
     */
    @PostMapping("/history/batch-convert")
    public Result<?> batchConvertToKnowledge(@RequestBody Map<String, Object> params) {
        try {
            List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");
            int successCount = aiService.batchConvertToKnowledge(items);
            return Result.success("批量转换成功，成功：" + successCount + "条");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= AI生成关键词 =============

    /**
     * AI生成关键词
     */
    @PostMapping("/generate-keywords")
    public Result<?> generateKeywords(@RequestBody Map<String, String> params) {
        try {
            String question = params.get("question");
            String answer = params.get("answer");
            if (question == null || question.trim().isEmpty()) {
                return Result.error("问题不能为空");
            }
            String keywords = aiService.generateKeywords(question, answer);
            Map<String, String> result = new HashMap<>();
            result.put("keywords", keywords);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * AI生成内容（根据标题）
     */
    @PostMapping("/generate-content")
    public Result<Map<String, String>> generateContent(@RequestBody Map<String, String> params) {
        try {
            String title = params.get("title");
            String category = params.get("category");

            if (title == null || title.trim().isEmpty()) {
                return Result.error("标题不能为空");
            }

            // 调用AI服务生成内容
            String content = aiService.generateContent(title, category);

            Map<String, String> result = new HashMap<>();
            result.put("content", content);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI生成失败：" + e.getMessage());
        }
    }

    /**
     * 知识库转为常见问题
     */
    @PostMapping("/knowledge/convert-to-faq")
    public Result<?> convertKnowledgeToFaq(@RequestBody Map<String, Object> params) {
        try {
            Integer knowledgeId = (Integer) params.get("id");
            String title = (String) params.get("title");
            String content = (String) params.get("content");
            String category = (String) params.get("category");

            if (title == null || title.isEmpty()) {
                return Result.error("标题不能为空");
            }

            // ✅ 检查是否已存在相同问题的常见问题
            Map<String, Object> existingFaq = aiService.checkFaqExists(title);
            if (existingFaq != null) {
                return Result.error("该问题已存在于常见问题中，无法重复转换");
            }

            Map<String, Object> faq = new HashMap<>();
            faq.put("question", title);
            faq.put("answer", content);
            faq.put("category", category != null ? category : "other");
            faq.put("sortOrder", 0);
            faq.put("status", 1);

            boolean success = aiService.addFaq(faq);

            if (success) {
                return Result.success("转换成功");
            } else {
                return Result.error("转换失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}