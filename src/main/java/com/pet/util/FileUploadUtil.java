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

    @Override
    public void setServletContext(ServletContext servletContext) {
        FileUploadUtil.servletContext = servletContext;
    }

    // 获取项目部署目录 - 确保路径正确
    private static String getRealPath() {
        if (servletContext == null) {
            // 开发环境备用路径
            String userDir = System.getProperty("user.dir");
            return userDir + "/src/main/webapp/upload/";
        }
        String realPath = servletContext.getRealPath("/");
        System.out.println("RealPath: " + realPath);
        return realPath + "upload/";
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
     * 上传社区图片
     */
    public static String uploadPostImage(MultipartFile file) throws Exception {
        return uploadImage(file, "post");
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

        // 保存到部署目录下的 upload/subDir/
        String savePath = getRealPath() + subDir + "/";
        File dir = new File(savePath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("创建目录: " + savePath + ", 成功: " + created);
        }

        File destFile = new File(savePath + fileName);
        file.transferTo(destFile);

        System.out.println("部署目录: " + getRealPath());
        System.out.println("文件保存路径: " + destFile.getAbsolutePath());
        System.out.println("文件是否存在: " + destFile.exists());

        // 返回相对路径
        return "/upload/" + subDir + "/" + fileName;
    }
}