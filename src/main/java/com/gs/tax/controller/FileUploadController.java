package com.gs.tax.controller;

import com.gs.tax.service.ExcelUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class FileUploadController {

    @Autowired
    private ExcelUploadService excelUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("jsonFile") MultipartFile jsonFile,
                                             @RequestParam("excelFile") MultipartFile excelFile) {
        try {
            Map<String, Object> result = excelUploadService.processFiles(jsonFile, excelFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to process files: " + e.getMessage());
        }
    }

}
