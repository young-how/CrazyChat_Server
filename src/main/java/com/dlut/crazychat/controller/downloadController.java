package com.dlut.crazychat.controller;

import com.dlut.crazychat.service.fileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Controller
public class downloadController {
    @Autowired
    private fileService fileservice;
    @GetMapping("/mediaDownload")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String filename,
                                                            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {
        System.out.println("加载资源路径："+fileservice.getMediaPath());
        File file = new File(fileservice.getMediaPath() + filename);
        fileservice.buildFileClass(file);  //生成并存入数据库
        long fileLength = file.length();

        InputStream inputStream;
        long start = 0;
        long end = fileLength - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            try {
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1) {
                    end = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
            }
        }

        if (start > end || end >= fileLength) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        }

        long contentLength = end - start + 1;
        inputStream = new FileInputStream(file);
        inputStream.skip(start);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(contentLength);
        headers.set("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);

        return new ResponseEntity<>(new InputStreamResource(inputStream), headers, HttpStatus.PARTIAL_CONTENT);
    }
    @GetMapping("/asyncDownload")
    public CompletableFuture<ResponseEntity<InputStreamResource>> asyncDownloadFile(@RequestParam String filename,
                                                                                    @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return downloadFile(filename, rangeHeader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
