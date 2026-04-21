package com.pet.controller;

import com.pet.util.FileUploadUtil;
import com.pet.util.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUploadUtil.uploadAvatar(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/carousel")
    public Result<Map<String, String>> uploadCarousel(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUploadUtil.uploadCarousel(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/product")
    public Result<Map<String, String>> uploadProduct(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUploadUtil.uploadProduct(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 添加服务图片上传接口
    @PostMapping("/service")
    public Result<Map<String, String>> uploadService(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUploadUtil.uploadServiceImage(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/feedback")
    public Result<Map<String, String>> uploadFeedback(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUploadUtil.uploadFeedback(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/comment")
    public Result<Map<String, String>> uploadComment(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUploadUtil.uploadComment(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/pet")
    public Result<Map<String, String>> uploadPet(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUploadUtil.uploadPetAvatar(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 帖子图片上传
    @PostMapping("/post")
    public Result<Map<String, String>> uploadPost(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUploadUtil.uploadPost(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 通用图片上传（系统设置用）
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUploadUtil.uploadSettingsImage(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}

