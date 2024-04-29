package com.dlut.crazychat.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class updateController {

    private static final String FILE_DIRECTORY = "/home/ubuntu/younghow/update/"; // 替换为你的文件存放目录

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
    @GetMapping("/full")
    public ResponseEntity<byte[]> downloadDirectory() {
        // 创建临时zip文件
        String baseDirectory=FILE_DIRECTORY+"/Fullversion";
        File zipFile = new File("temp.zip");
        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // 遍历基础目录下的所有文件和子目录
            File baseDir = new File(baseDirectory);
            addFilesToZip(baseDir, baseDir, zos);

            zos.close();
            fos.close();

            // 读取zip文件内容并构建HTTP响应
            FileInputStream fis = new FileInputStream(zipFile);
            byte[] data = new byte[(int) zipFile.length()];
            fis.read(data);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("filename", "directory.zip");
            zipFile.delete(); // 删除临时zip文件
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 将文件和子目录添加到zip文件中
    private void addFilesToZip(File baseDir, File file, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                addFilesToZip(baseDir, subFile, zos);
            }
        } else {
            String relativePath = baseDir.toURI().relativize(file.toURI()).getPath();
            FileInputStream fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(relativePath);
            zos.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            fis.close();
        }
    }
}