package com.dlut.crazychat.service;

import com.dlut.crazychat.pojo.mediaResource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Data
public class fileService {
    @Value("${Server.config.mediaSourcePath}")
    private String mediaPath;
    private String ResourceMap="ResourceHashMap";
    private String MD2poolKey="MD5map";
    private final RedisTemplate<String,Object> redisTemplate;
    @Autowired
    public fileService(RedisTemplate<String,Object> redisTemplate){
        this.redisTemplate=redisTemplate;
    }
    public mediaResource getFileClass(String fileName){
        mediaResource re=(mediaResource)redisTemplate.opsForHash().get(ResourceMap,fileName);
        return re;
    }

    /*
    将file对象置于redis，如果成功返回true，失败返回false
     */
    public boolean putFileClass(mediaResource file){
        boolean fileExist=(null!=redisTemplate.opsForHash().get(ResourceMap,file.getFileName()));
        if(fileExist) return false;
        redisTemplate.opsForHash().put(ResourceMap,file.getFileName(),file);
        return true;
    }
    public static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        // Create a FileInputStream for the file
        FileInputStream fis = new FileInputStream(file);

        // Create a byte array to hold the data
        byte[] byteArray = new byte[1024];
        int bytesRead = 0;

        // Read the file data and update the MessageDigest
        while ((bytesRead = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesRead);
        }

        // Close the FileInputStream
        fis.close();

        // Get the checksum bytes
        byte[] bytes = digest.digest();

        // Convert the bytes to a hex format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        // Return the checksum
        return sb.toString();
    }
    /*
    计算一个文件的MD5码
     */
    public static String calculateMD5(File file) {
        try {
            // Get a MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Calculate and return the checksum
            return getFileChecksum(md, file);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /*
    返回MD5
     */
    public static String getMD5Checksum(MultipartFile file) {
        try {
            // 获取 MD5 的 MessageDigest 实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 获取文件输入流
            try (InputStream is = file.getInputStream()) {
                // 创建一个字节数组来存储读取的数据
                byte[] buffer = new byte[1024];
                int bytesRead;

                // 读取文件数据并更新 MessageDigest
                while ((bytesRead = is.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }

            // 获取 MD5 校验和的字节数组
            byte[] digestBytes = md.digest();

            // 将字节数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digestBytes) {
                sb.append(String.format("%02x", b));
            }

            // 返回校验和字符串
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /*
    将文件的MD5放入池中
     */
    public void putMD2pool(String MD5){
        redisTemplate.opsForHash().put(MD2poolKey,MD5,true);
        redisTemplate.expire(MD2poolKey,7200, TimeUnit.SECONDS);
    }
    public boolean MD5exist(String MD5){
        if(redisTemplate.opsForHash().get(MD2poolKey,MD5)==null) return false;
        return true;
    }

    /*
    删除redis中的对象
     */
    public boolean deleteFileClass(mediaResource file){
        redisTemplate.opsForHash().delete(ResourceMap,file.getFileName());
        return true;
    }
    /*
    更新redis中的对象(慎用)
     */
    public boolean updateFileClass(mediaResource file){
        redisTemplate.opsForHash().put(ResourceMap,file.getFileName(),file);
        return true;
    }
    public mediaResource buildFileClass(File file){
        mediaResource fileClass=(mediaResource)redisTemplate.opsForHash().get(ResourceMap,file.getName());
        if(fileClass==null){
            fileClass=new mediaResource();
            fileClass.setFileName(file.getName());
            fileClass.setFileSize(file.length());
            SimpleDateFormat ft=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fileClass.setUploadTime(ft.format(new Date()));
            fileClass.setDownloadNum(0);
        }
        return fileClass;
    }

}
