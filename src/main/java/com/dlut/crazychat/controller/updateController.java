package com.dlut.crazychat.controller;

import com.dlut.crazychat.enum_class.platForm;
import com.dlut.crazychat.pojo.ClientVersion;
import com.dlut.crazychat.service.ClientService;
import com.dlut.crazychat.service.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
客户端更新相关的控制器
 */
@Controller
public class updateController {
//    @Value("${admin.upload.update_home}")
//    private static final String FILE_DIRECTORY ; // 替换为你的文件存放目录
    @Value("${admin.upload.update_home}")
    private String FILE_DIRECTORY ; // 替换为你的文件存放目录
    @Autowired
    private ClientService clientservice;

    @GetMapping("/latestVersion")
    @ResponseBody
    public String getLatestVersion(){
        ClientVersion newestClientVersion = clientservice.getNewestClientVersion();
        return newestClientVersion.getVersion();
    }
    @GetMapping("/update")
    public ResponseEntity<Resource> downloadFile() {
        String fileName=FILE_DIRECTORY+"/CrazyChat.exe";
        // 构建文件路径
        Path filePath = Paths.get(FILE_DIRECTORY);
        try {
            // 读取文件资源
            Resource resource = new UrlResource(filePath.toUri());
            // 确保文件存在并可读
            if (resource.exists() && resource.isReadable()) {
                // 构建HTTP响应头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", fileName);
                // 返回文件内容
                return new ResponseEntity<>(resource, headers, HttpStatus.OK);
            } else {
                // 文件不存在或不可读
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            // 处理异常
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {
        File file = new File(FILE_DIRECTORY, fileName);
        if (!file.exists()) {
            // 文件不存在，返回404
            return ResponseEntity.notFound().build();
        }

        // 将文件转换为资源
        Resource resource = new InputStreamResource(new FileInputStream(file));
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        // 返回响应实体
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
    /*
    下载指定版本的客户端
     */
    @GetMapping("/downloadClientByVersion")
    public ResponseEntity<Resource> downloadClientByVersion(@RequestParam("version") String clientVersion,@RequestParam("format") String format) throws IOException {
        ClientVersion targetClient=clientservice.getClientPathByVersion(clientVersion);
        if(targetClient==null){
            System.out.println("请求的客户端版本不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        File file=null;
        String fileName;
        if(format.equals(".exe")){
            file = new File(targetClient.getExecuteFilePath());
            fileName=targetClient.getExecuteFileName();
        }
        else if(format.equals(".zip")){
            file = new File(targetClient.getZipFilePath());
            fileName=targetClient.getZipFileName();
        }
        else if(format.equals(".dmg")){
            file = new File(targetClient.getZipFilePath());
            fileName=targetClient.getZipFileName();
        }
        else{
            System.out.println("请求的文件格式不正确");
            return ResponseEntity.notFound().build();
        }


        // 将文件转换为资源
        Resource resource = new InputStreamResource(new FileInputStream(file));
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        // 返回响应实体
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
    @PostMapping("/adminUpload")
    @ResponseBody
    public String handleFileUpload(@RequestParam("file") MultipartFile[] files,
                                   @RequestParam("platform") String platform,
                                   @RequestParam("changelog") String changelog,
                                   @RequestParam("version") String version) {

        // 处理文件上传
        if (files.length!=0) {
            //设置客户端通用的信息
            ClientVersion client=new ClientVersion();
            client.setVersion(version);  //设置版本号
            client.setUpdateLog(changelog);  //设置更新日志
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            client.setDate(format.format(new Date()));  //设置更新时间
            if(platform=="mac"||platform=="Mac"){
                client.setPlatform(platForm.Mac);
            }
            else if(platform=="windows"||platform=="Windows"){
                client.setPlatform(platForm.Windows);
            }
            for(MultipartFile file:files){
                try {
                    String uploadPath=FILE_DIRECTORY; //默认目录
                    clientservice.copyFile2Server(file,uploadPath,client);  //将上传信息拷贝到服务器
                } catch (Exception e) {
                    return "文件上传失败：" + e.getMessage();
                }
            }
        } else {
            return "上传文件为空";
        }
        return "上传文件成功";
    }
    @GetMapping("/adminPage")
    public String getAdminPage(Model model){
        Map<String,ClientVersion> re=clientservice.getClientVersion();
        List<ClientVersion> append=new ArrayList<>(re.values());
        model.addAttribute("clientVersions",append);
        return "uploadUpdate";
    }
    /*
    获取客户端的版本信息
     */
    @GetMapping("/ClientInfo")
    public String getClientInfo(Model model){
        Map<String,ClientVersion> re=clientservice.getClientVersion();
        List<ClientVersion> append=new ArrayList<>(re.values());
        model.addAttribute("clientVersions",append);
        return "ClientVersionPage";
    }
}