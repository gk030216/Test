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
}