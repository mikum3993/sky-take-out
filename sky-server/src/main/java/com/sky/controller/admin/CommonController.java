package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


/**
 * @program: sky-take-out
 * @description: 通用接口
 * @author: {}
 * @create: 2024/4/13 19:25
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;


    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传:{}", file);

        try {
            // 原始文件名
            String originalFilename = file.getOriginalFilename();
            // 截取原始文件名的后缀 .jpg
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 构建新文件名称
            String ObjectName = UUID.randomUUID().toString() + extension;

            aliOssUtil.upload(file.getBytes(), ObjectName);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e);
        }


        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
