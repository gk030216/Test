package com.pet.util;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.ServletContextAware;
import com.pet.entity.SystemSetting;
import com.pet.mapper.SystemSettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class FileUploadUtil implements ServletContextAware {

    @Autowired
    private SystemSettingMapper settingMapper;

    private static ServletContext servletContext;

    // 默认配置
    private static String UPLOAD_BASE_PATH = "D:/pet_uploads/";
    private static long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static String[] ALLOWED_TYPES = {"jpg", "jpeg", "png", "gif"};
    private static int CAROUSEL_LIMIT = 5;

    @Override
    public void setServletContext(ServletContext servletContext) {
        FileUploadUtil.servletContext = servletContext;
        initDirs();
    }

    @PostConstruct
    public void init() {
        loadConfigFromDatabase();
        initDirs();
    }

    /**
     * 从数据库加载配置
     */
    private void loadConfigFromDatabase() {
        try {
            List<SystemSetting> settings = settingMapper.findAll();
            for (SystemSetting setting : settings) {
                switch (setting.getSettingKey()) {
                    case "upload_path":
                        UPLOAD_BASE_PATH = setting.getSettingValue();
                        break;
                    case "upload_max_size":
                        MAX_FILE_SIZE = (long) (Double.parseDouble(setting.getSettingValue()) * 1024 * 1024);
                        break;
                    case "upload_allowed_types":
                        ALLOWED_TYPES = setting.getSettingValue().split(",");
                        break;
                    case "carousel_limit":
                        CAROUSEL_LIMIT = Integer.parseInt(setting.getSettingValue());
                        break;
                }
            }
            System.out.println("✅ 上传配置加载成功: " + UPLOAD_BASE_PATH);
        } catch (Exception e) {
            System.err.println("❌ 上传配置加载失败，使用默认配置");
            e.printStackTrace();
        }
    }

    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        loadConfigFromDatabase();
        initDirs();
    }

    private static void initDirs() {
        String[] subDirs = {"avatar", "carousel", "product", "comment", "post", "pet", "service", "settings","feedback"};
        for (String subDir : subDirs) {
            File dir = new File(UPLOAD_BASE_PATH + subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    private static String getRealPath() {
        return UPLOAD_BASE_PATH;
    }

    /**
     * 通用图片上传
     */
    private static String uploadImage(MultipartFile file, String subDir) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只支持图片格式");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("图片大小不能超过" + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        // 检查文件扩展名
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }

        boolean allowed = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equalsIgnoreCase(extension)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new RuntimeException("不支持的图片格式，仅支持: " + String.join(",", ALLOWED_TYPES));
        }

        // 生成文件名
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String fileName = timestamp + "_" + uuid + "." + extension;

        // 保存到目录
        String savePath = getRealPath() + subDir + "/";
        File dir = new File(savePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File destFile = new File(savePath + fileName);
        file.transferTo(destFile);

        System.out.println("图片保存目录: " + getRealPath());
        System.out.println("文件保存路径: " + destFile.getAbsolutePath());
        System.out.println("图片类型: " + subDir);

        return "/uploads/" + subDir + "/" + fileName;
    }

    // 以下方法保持不变
    public static String uploadAvatar(MultipartFile file) throws Exception {
        return uploadImage(file, "avatar");
    }

    public static String uploadCarousel(MultipartFile file) throws Exception {
        return uploadImage(file, "carousel");
    }

    public static String uploadProduct(MultipartFile file) throws Exception {
        return uploadImage(file, "product");
    }

    public static String uploadComment(MultipartFile file) throws Exception {
        return uploadImage(file, "comment");
    }

    public static String uploadPost(MultipartFile file) throws Exception {
        return uploadImage(file, "post");
    }

    public static String uploadPetAvatar(MultipartFile file) throws Exception {
        return uploadImage(file, "pet");
    }

    public static String uploadServiceImage(MultipartFile file) throws Exception {
        return uploadImage(file, "service");
    }

    public static String uploadSettingsImage(MultipartFile file) throws Exception {
        return uploadImage(file, "settings");
    }

    /**
     * 上传反馈图片
     */
    public static String uploadFeedback(MultipartFile file) throws Exception {
        return uploadImage(file, "feedback");
    }

    public static boolean deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        try {
            String relativePath = imageUrl.replace("/uploads/", "");
            String fullPath = UPLOAD_BASE_PATH + relativePath;
            File file = new File(fullPath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // getter方法供外部使用
    public static String getUploadPath() { return UPLOAD_BASE_PATH; }
    public static long getMaxFileSize() { return MAX_FILE_SIZE; }
    public static String[] getAllowedTypes() { return ALLOWED_TYPES; }
    public static int getCarouselLimit() { return CAROUSEL_LIMIT; }
}