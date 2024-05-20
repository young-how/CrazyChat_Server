package com.dlut.crazychat.controller;

import com.dlut.crazychat.enum_class.platForm;
import com.dlut.crazychat.pojo.ClientVersion;
import com.dlut.crazychat.pojo.rankList;
import com.dlut.crazychat.pojo.userStat;
import com.dlut.crazychat.service.ClientService;
import com.dlut.crazychat.service.fileService;
import com.dlut.crazychat.service.userService;
import com.dlut.crazychat.utils.SystemManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
public class userController {
    @Autowired
    public userService userservice;
    @Autowired
    private SystemManager Sys;
    @Autowired
    private ClientService clientservice;
    @Value("${Server.config.mediaSourcePath}")
    private String mediaSourcePath;
    @Autowired
    private fileService fileservice;
    private String picTemplate_pre="<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Adjust Image Max Width</title>\n" +
            "    <style>\n" +
            "        img {\n" +
            "            width: 250px; /* 设置图片的最大宽度为 200px */\n" +
            //"            width: 100%; /* 设置图片的宽度为父元素的宽度 */\n" +
            "            height: auto; /* 保持图片宽高比 */\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "<img src=\"";
    private String picTemplate_back="\" alt=\"Example Image\">\n" +
            "</body>\n" +
            "</html>";
    @Value("${app.ip}")
    private String server_ip;
    @Value("${server.port}")
    private String server_port;
    public String getImageURI(String fileName){
        return "http://"+server_ip+":"+server_port+"/getImage/"+fileName;
    }
    //前端获取用户状态
    @PostMapping("/getState")
    public ResponseEntity<userStat> getUserStat(@RequestBody userStat userstat){
        //根据前端返回的json对象查找redis，并更新redis状态
        return ResponseEntity.ok().body(userservice.find_Update_User(userstat));
    }
    //前端发送消息，进行加分以及添加发言数目
    @PostMapping("/sendMessage")
    public ResponseEntity<userStat> sendMessage(@RequestBody userStat userstat){
        //根据前端返回的json对象查找redis，并更新redis状态
        return ResponseEntity.ok().body(userservice.addMessage(userstat));
    }
    @PostMapping("/joinFindSpy")
    public ResponseEntity<String> joinFindSpy(@RequestBody userStat userstat){
        //模拟用户加入游戏
        StringBuilder re=new StringBuilder(Sys.getGameservice().getFindspy().join(userstat));
        //re.append("\n你的身份是:"+Sys.getGameservice().getFindspy().getUserRole(userstat));
        return ResponseEntity.ok().body(re.toString());
    }
    @PostMapping("/userVote")
    public ResponseEntity<String> userVote(@RequestBody userStat userstat,@RequestParam("num") int num){
        //模拟用户加入游戏
        StringBuilder re=new StringBuilder();
        re.append(Sys.getGameservice().getFindspy().vote(userstat,num));
        re.append("\n你的身份是:"+Sys.getGameservice().getFindspy().getUserRole(userstat));
//        StringBuilder re=new StringBuilder(Sys.getGameservice().getFindspy().join(userstat));
//        re.append("\n你的身份是:"+Sys.getGameservice().getFindspy().getUserRole(userstat));
        return ResponseEntity.ok().body(re.toString());
    }
    @PostMapping("/rankList")
    public ResponseEntity<rankList> getRankList(){
        //查看积分榜
        rankList rank=userservice.getRankList();
        return ResponseEntity.ok().body(rank);
    }
    @GetMapping("/systemMessage")
    @ResponseBody
    public String sendSystemMessage(@RequestParam("id") String id){
        //查看积分榜
        Sys.send("test",id);
        return id;
    }
    /*
    向服务器发送媒体数据，包括图片、视频、程序等。
     */
    @PostMapping("/sendFile")
    @ResponseBody
    public String handleFileUpload(@RequestParam("file") MultipartFile[] files,@RequestParam("user") String json_user) throws JsonProcessingException {
        ObjectMapper objectMapper=new ObjectMapper();
        userStat user=objectMapper.readValue(json_user, userStat.class);
        if (files.length!=0) {

            for(MultipartFile file:files){
                try {

                    String fileMD5=fileservice.getMD5Checksum(file); //计算文件MD5
                    if(!fileservice.MD5exist(fileMD5)){
                        //MD5码不存在
                        userservice.addScore(user,500);   //单张表情包的奖励
                        fileservice.putMD2pool(fileMD5);  //添加到MD5
                    }
                    String refileName=file.getOriginalFilename();  //文件id
                    if(file.getOriginalFilename().contains(".png")){
                        refileName= "picID_"+UUID.randomUUID().toString()+".png";  //随机生成图片id
                    }
                    else if(file.getOriginalFilename().contains(".jpg")){
                        refileName= "picID_"+UUID.randomUUID().toString()+".jpg";  //随机生成图片id
                    }
                    else if(file.getOriginalFilename().contains(".gif")){
                        refileName= "picID_"+UUID.randomUUID().toString()+".gif";  //随机生成图片id
                    }
                    String uploadPath=mediaSourcePath+"/pic/"; //默认资源目录
                    Files.copy(file.getInputStream(), Paths.get(uploadPath, refileName));
                    String html_info="/WithHtmlContent:"+user.getName()+":"+picTemplate_pre+getImageURI(refileName)+picTemplate_back;  //生成html文档

                    Sys.send(html_info);
                } catch (Exception e) {
                    return "文件上传失败：" + e.getMessage();
                }
            }
        } else {
            return "上传文件为空";
        }
        return "上传文件成功";
    }
    @GetMapping("/getImage/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) {
        // 图片文件路径
        String imagePath = mediaSourcePath+"/pic/"+imageName;

        // 创建资源对象
        Resource resource = new FileSystemResource(imagePath);

        try {
            // 读取图片内容
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));

            // 构造响应实体
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
