package com.ecode.controller.admin;

import com.ecode.constant.MessageConstant;
import com.ecode.result.Result;
import com.ecode.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件为: " + file);
        try {
            String fileOriginalFilename = file.getOriginalFilename();
            String extension = fileOriginalFilename.substring(fileOriginalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID() + extension;
            String filePath = aliOssUtil.upload(file.getBytes(), fileName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
