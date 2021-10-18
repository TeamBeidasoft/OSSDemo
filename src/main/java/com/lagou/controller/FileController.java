package com.lagou.controller;

import com.aliyun.oss.model.OSSObject;
import com.lagou.bean.UpLoadResult;
import com.lagou.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

@Controller
@RequestMapping("/pic")
public class FileController {
    @Autowired
    private FileService fileService;


    @PostMapping("/upload")
    @ResponseBody
    public UpLoadResult upload(@RequestParam("file") MultipartFile  multipartFile){
        return  fileService.upload(multipartFile);
    }

    @PostMapping("/delete")
    @ResponseBody
    public String delete(String objectNameUrl){
      return  fileService.delete(objectNameUrl);
    }


    @PostMapping("/download")
    public void downloadFile(String bucketName,String fileUrl, HttpServletResponse response) throws Exception {
        fileService.download(bucketName,fileUrl,response);
    }

}
