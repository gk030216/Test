package com.pet.util;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.ServletContextAware;
import javax.servlet.ServletContext;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FileUploadUtil implements ServletContextAware {

    private static ServletContext servletContext;

    // 配置独立的图片存储目录
    private static final String UPLOAD_BASE_PATH = "D:/pet_uploads/";

    @Override
    public void setServletContext(ServletContext servletContext) {
        FileUploadUtil.servletContext = servletContext;
        initDirs();
    }

    private static void initDirs() {
        String[] subDirs = {"avatar", "carousel", "product", "comment", "post"};
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
     * 上传头像
     */
    public static String uploadAvatar(MultipartFile file) throws Exception {
        return uploadImage(file, "avatar");
    }

    /**
     * 上传轮播图
     */
    public static String uploadCarousel(MultipartFile file) throws Exception {
        return uploadImage(file, "carousel");
    }

    /**
     * 上传商品图片
     */
    public static String uploadProduct(MultipartFile file) throws Exception {
        return uploadImage(file, "product");
    }

    /**
     * 上传评价图片
     */
    public static String uploadComment(MultipartFile file) throws Exception {
        return uploadImage(file, "comment");
    }

    /**
     * 上传帖子图片
     */
    public static String uploadPost(MultipartFile file) throws Exception {
        return uploadImage(file, "post");
    }

    /**
     * 上传宠物头像
     */
    public static String uploadPetAvatar(MultipartFile file) throws Exception {
        return uploadImage(file, "pet");
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

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("图片大小不能超过2MB");
        }

        // 生成文件名
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = timestamp + "_" + uuid + extension;

        // 保存到独立目录
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

        // 返回访问URL
        return "/uploads/" + subDir + "/" + fileName;
    }

    /**
     * 删除图片
     */
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
}