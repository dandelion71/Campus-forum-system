package com.dandelion.admin.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.Section;
import com.dandelion.system.dao.User;
import com.dandelion.system.service.SectionService;
import com.dandelion.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${spring.servlet.multipart.location}")
    private String fileRootPath;

    @Autowired
    private UserService userService;

    @Autowired
    private SectionService sectionService;

    @PostMapping("/avatar")
    public ResponseResult uploadAvatar(MultipartFile file, HttpServletRequest req) throws IOException {
        String format = "/user/";
        String url = uploadImage(file, req, format);
        return ResponseResult.success(url,null);
    }

    @PostMapping("/section")
    public ResponseResult uploadSectionImg(MultipartFile file, HttpServletRequest req) throws IOException {
        String format = "/section/";
        String url = uploadImage(file, req, format);
        return ResponseResult.success(url,null);
    }


    private String uploadImage(MultipartFile file, HttpServletRequest req,String format)throws IOException{
        String uuid = UUID.randomUUID().toString();
        String saveName =uuid+"-"+file.getOriginalFilename();
        String realPath = fileRootPath + format;
        File folder = new File(realPath);
        if(!folder.exists()){
            folder.mkdirs();
        }
        file.transferTo(new File(folder,saveName));
        return req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+"/files"+format+saveName;
    }
}
