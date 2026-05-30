package com.lyf.supplychain.common.oss.template;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.lyf.supplychain.common.oss.properties.OssProperties;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/**
 * 阿里云 OSS 操作模板。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
public class OssTemplate {

    private final OSS ossClient;

    private final OssProperties properties;

    public OssTemplate(OSS ossClient, OssProperties properties) {
        this.ossClient = ossClient;
        this.properties = properties;
    }

    /**
     * 上传对象。
     *
     * @param objectName 对象名称
     * @param inputStream 文件输入流
     * @return 对象访问地址
     */
    public String upload(String objectName, InputStream inputStream) {
        String finalObjectName = buildObjectName(objectName);
        ossClient.putObject(properties.getBucketName(), finalObjectName, inputStream);
        return buildPublicUrl(finalObjectName);
    }

    /**
     * 上传带元数据的对象。
     *
     * @param objectName 对象名称
     * @param inputStream 文件输入流
     * @param metadata 对象元数据
     * @return OSS 上传结果
     */
    public PutObjectResult upload(String objectName, InputStream inputStream, ObjectMetadata metadata) {
        return ossClient.putObject(properties.getBucketName(), buildObjectName(objectName), inputStream, metadata);
    }

    /**
     * 上传带元数据的对象并返回访问地址。
     *
     * @param objectName 对象名称
     * @param inputStream 文件输入流
     * @param metadata 对象元数据
     * @return 对象访问地址
     */
    public String uploadAndReturnUrl(String objectName, InputStream inputStream, ObjectMetadata metadata) {
        String finalObjectName = buildObjectName(objectName);
        ossClient.putObject(properties.getBucketName(), finalObjectName, inputStream, metadata);
        return buildPublicUrl(finalObjectName);
    }

    /**
     * 删除对象。
     *
     * @param objectName 对象名称
     */
    public void delete(String objectName) {
        ossClient.deleteObject(properties.getBucketName(), buildObjectName(objectName));
    }

    /**
     * 判断对象是否存在。
     *
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean exists(String objectName) {
        return ossClient.doesObjectExist(properties.getBucketName(), buildObjectName(objectName));
    }

    /**
     * 拼接对象存储路径。
     *
     * @param objectName 对象名称
     * @return 带业务前缀的对象名称
     */
    public String buildObjectName(String objectName) {
        String normalizedObjectName = trimSlash(objectName);
        if (!StringUtils.hasText(properties.getObjectPrefix())) {
            return normalizedObjectName;
        }
        return trimSlash(properties.getObjectPrefix()) + "/" + normalizedObjectName;
    }

    /**
     * 按租户、业务类型和日期生成目录化对象名称。
     *
     * @param tenantId 租户ID
     * @param bizType 业务类型
     * @param originalFilename 原始文件名
     * @return 目录化对象名称
     */
    public String buildDirectoryObjectName(Long tenantId, String bizType, String originalFilename) {
        return buildDirectoryObjectName(tenantId, bizType, originalFilename, LocalDate.now());
    }

    /**
     * 按租户、业务类型和指定日期生成目录化对象名称。
     *
     * @param tenantId 租户ID
     * @param bizType 业务类型
     * @param originalFilename 原始文件名
     * @param date 存储日期
     * @return 目录化对象名称
     */
    public String buildDirectoryObjectName(Long tenantId, String bizType, String originalFilename, LocalDate date) {
        return tenantId + "/" + trimSlash(bizType) + "/" + date + "/" + renameFile(originalFilename);
    }

    /**
     * 使用 UUID 重命名文件，保留原始扩展名。
     *
     * @param originalFilename 原始文件名
     * @return 重命名后的文件名
     */
    public String renameFile(String originalFilename) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extractExtension(originalFilename);
    }

    /**
     * 拼接公开访问地址。
     *
     * @param objectName 对象名称
     * @return 公开访问地址
     */
    public String buildPublicUrl(String objectName) {
        String normalizedObjectName = trimSlash(objectName);
        if (StringUtils.hasText(properties.getDomain())) {
            return trimSlash(properties.getDomain()) + "/" + normalizedObjectName;
        }
        return buildBucketEndpoint() + "/" + normalizedObjectName;
    }

    private String buildBucketEndpoint() {
        String endpoint = trimSlash(properties.getEndpoint());
        if (endpoint.startsWith("https://")) {
            return "https://" + properties.getBucketName() + "." + endpoint.substring("https://".length());
        }
        if (endpoint.startsWith("http://")) {
            return "http://" + properties.getBucketName() + "." + endpoint.substring("http://".length());
        }
        return "https://" + properties.getBucketName() + "." + endpoint;
    }

    private String trimSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String result = value.trim();
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String extractExtension(String originalFilename) {
        String filename = StringUtils.hasText(originalFilename) ? originalFilename.trim() : "";
        int index = filename.lastIndexOf(".");
        if (index < 0 || index == filename.length() - 1) {
            return "bin";
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
