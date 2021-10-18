package com.lagou.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.lagou.bean.UpLoadResult;
import com.lagou.config.AliyunConfig;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.UUID;

@Service
public class FileService {
    @Autowired
    private AliyunConfig aliyunConfig;
    @Autowired
    private OSSClient ossClient;
    // 允许上传的格式
    private static final String[] IMAGE_TYPE = new String[]{".bmp", ".jpg", ".jpeg", ".gif", ".png"};

    public UpLoadResult upload(MultipartFile multipartFile) {
        // 校验图片格式
        boolean isLegal = false;
        for (String type : IMAGE_TYPE) {
            if (StringUtils.endsWithIgnoreCase(multipartFile.getOriginalFilename(), type)) {
                isLegal = true;
                break;
            }
        }
        UpLoadResult upLoadResult = new UpLoadResult();
        if (!isLegal) {
            upLoadResult.setStatus("error");
            return upLoadResult;
        }
        String fileName = multipartFile.getOriginalFilename();
        String filePath = getFilePath(fileName);
        try {
            ossClient.putObject(aliyunConfig.getBucketName(), filePath, new ByteArrayInputStream(multipartFile.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
            // 上传失败
            upLoadResult.setStatus("error");
            return upLoadResult;
        }
        upLoadResult.setStatus("done");
        upLoadResult.setName(aliyunConfig.getUrlPrefix() + filePath);
        upLoadResult.setUid(filePath);
        return upLoadResult;
    }

    // 生成不重复的文件路径和文件名
    private String getFilePath(String sourceFileName) {
        DateTime dateTime = new DateTime();
        return "images/" + dateTime.toString("yyyy")
                + "/" + dateTime.toString("MM") + "/"
                + dateTime.toString("dd") + "/" + UUID.randomUUID().toString() + "." +
                StringUtils.substringAfterLast(sourceFileName, ".");
    }

    public String delete(String objectNameUrl) {
        System.out.println(objectNameUrl);
        if (!ossClient.doesObjectExist("dabing-heads", objectNameUrl)) {
            return "删除失败，文件不存在";
        }
        ossClient.deleteObject("dabing-heads", objectNameUrl);
        return "删除成功";
    }


    public void download(String bucketName, String fileUrl, HttpServletResponse response) throws Exception {
        OSSObject ossObject = ossClient.getObject(bucketName, fileUrl);
        InputStream inputStream = ossObject.getObjectContent();
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileUrl, "UTF-8"));
        response.setCharacterEncoding("UTF-8");
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}