package com.ziphiro.browserBotPart.service;

import com.ziphiro.browserBotPart.entityes.UserFile;
import com.ziphiro.browserBotPart.repositories.UserFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class UserFileService {

    private final String storageDir = "/home/ziphiro/myBotStorage/";
    private final Path storagePath = Path.of("/home/ziphiro/myBotStorage/art_yommy/");

    @Autowired
    private UserFileRepository userFileRepository;

    public UserFile initUserFile(UserFile file){
        return userFileRepository.save(file);
    }

    public List<UserFile> getAllUserFilesPaths(String name){
        return userFileRepository.findFilesByCreator(name);
    }

    @Async
    public CompletableFuture<ResponseEntity<Resource>> downloadFile(String fileName, String userName) throws IOException {
        CompletableFuture<ResponseEntity<Resource>> task = new CompletableFuture<>();
        Path filePath  = Path.of(storageDir, userName, fileName);
        InputStream fileStream = new FileInputStream(filePath.toFile());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                fileName + "/");
        return CompletableFuture.completedFuture(ResponseEntity.ok().headers(headers).contentLength(filePath.toFile().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(fileStream)));
    }
    @Async
    public CompletableFuture<String> uploadFile(MultipartFile file, String userName) {
        String request = "";
        if (file.isEmpty()){
            request = "error in upload";
        } else {
            try {
                file.transferTo(Path.of(storageDir, userName, file.getOriginalFilename()));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            String uploadFileDir = storageDir + file.getOriginalFilename();
            UserFile uploadFile = UserFile.builder().fileName(file.getOriginalFilename())
                    .filePath(uploadFileDir)
                    .creator(userName).build();
            userFileRepository.save(uploadFile);
            request = "file successful uploaded";
        }
        return CompletableFuture.completedFuture(request);
    }

    public void deleteFileFromStorage(String fileName, String userName) throws IOException {
        Files.delete(Path.of(storageDir + userName + "/" + fileName));
    }
    public void deleteFileFromDataBase(Long fileId){
        userFileRepository.deleteById(fileId);
    }
}
