package com.dlut.crazychat.pojo;

import com.dlut.crazychat.enum_class.platForm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientVersion {
    private String executeFileName;  //客户端可执行文件名称
    private String zipFileName;  //客户端压缩包文件名称
    private String dmgFileName;  //客户端压缩包文件名称
    private String Version;
    private String updateLog;
    private platForm platform;  //平台
    private double executeFileSize;  //软件可执行文件容量（MB）
    private double zipFileSize;  //软件压缩包文件容量（MB）
    private double DMGFileSize;  //软件压缩包文件容量（MB）
    private String date;  //更新日期
    private String executeFilePath;  //可执行文件所在服务器的路径
    private String zipFilePath;  //压缩包文件所在服务器的路径
    private String DMGFilePath;  //压缩包文件所在服务器的路径
}
