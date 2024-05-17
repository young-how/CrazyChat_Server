package com.dlut.crazychat.pojo;

import lombok.Data;

@Data
public class mediaResource {
    private String fileName;
    private long fileSize;
    private String information;
    private long downloadNum;   //下载数
    private String uploadTime;  //资源上传时间
    private String tag;   //资源标签，用于分类
}
