package com.dlut.crazychat.service;

import com.dlut.crazychat.pojo.ClientVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
@Component
public class ClientService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private String NewestClient="ClientVersionNewest&";
    private String ClientVersionHisSuffix="ClientVersionHisRedis&";
    /*
    获取客户端集合
     */
    public Map<String, ClientVersion> getClientVersion(){
        Map<String,ClientVersion> re=new HashMap<>();
        Map<Object,Object> entries=redisTemplate.opsForHash().entries(ClientVersionHisSuffix);
        for(Map.Entry<Object,Object> et:entries.entrySet()){
            String ClientVersion=(String)et.getKey();
            ClientVersion entity=(ClientVersion)et.getValue();
            re.put(ClientVersion,entity);
        }
        return re;
    }
    /*
    修改历史客户端版本号和更新日志
     */
    public void updateClientVersion(ClientVersion client){
        redisTemplate.opsForHash().put(ClientVersionHisSuffix,client.getVersion(),client);  //在hashmap中添加客户端信息
    }
    /*
    更新最新客户端信息
     */
    public void updateNewestClient(ClientVersion client){
        redisTemplate.opsForValue().set(NewestClient,client);
        updateClientVersion(client);
    }
    /*
    获取最新的客户端信息
     */
    public ClientVersion getNewestClientVersion(){
        ClientVersion re=(ClientVersion)redisTemplate.opsForValue().get(NewestClient);
        return re;
    }
    /*
    通过版本号获取客户端实体
     */
    public ClientVersion getClientPathByVersion(String version){
        ClientVersion re=(ClientVersion)redisTemplate.opsForHash().get(ClientVersionHisSuffix,version);
        return re;
    }
    public String copyFile2Server(MultipartFile file,String uploadPath,ClientVersion client) throws IOException {
        if(file.getOriginalFilename().contains(".exe")){
            uploadPath+="/windows/exe/"+client.getVersion()+"/";
            client.setExecuteFileName(file.getOriginalFilename());
            client.setExecuteFileSize(file.getSize()/1024.0/1024.0);  //将文件大小转换为mb
            client.setExecuteFilePath(uploadPath+"/"+file.getOriginalFilename());  //设置上传路径位置
        }
        else if(file.getOriginalFilename().contains(".zip")){
            uploadPath+="/windows/full/"+client.getVersion()+"/";
            client.setZipFileName(file.getOriginalFilename());
            client.setZipFileSize(file.getSize()/1024.0/1024.0);  //将文件大小转换为mb
            client.setZipFilePath(uploadPath+"/"+file.getOriginalFilename());  //设置上传路径位置
        }
        else if(file.getOriginalFilename().contains(".dmg")){
            uploadPath+="/MacCrazyChat/"+client.getVersion()+"/";
            client.setDmgFileName(file.getOriginalFilename());
            client.setDMGFileSize(file.getSize()/1024.0/1024.0);  //将文件大小转换为mb
            client.setDMGFilePath(uploadPath+"/"+file.getOriginalFilename());  //设置上传路径位置
        }
        else return "未知的文件类型:"+file.getOriginalFilename();

        // 创建一个File对象来表示路径
        File directory = new File(uploadPath);
        // 检查路径是否存在
        if (!directory.exists()) {
            // 如果路径不存在，则创建路径
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("路径已创建");
            } else {
                System.out.println("路径创建失败");
            }
        } else {
            System.out.println("路径已存在");
        }
        Files.copy(file.getInputStream(), Paths.get(uploadPath, file.getOriginalFilename()));
        updateNewestClient(client);  //更新为最新的客户端版本
        return "文件上传成功";
    }

}
