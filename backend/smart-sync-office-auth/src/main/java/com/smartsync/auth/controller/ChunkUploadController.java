package com.smartsync.auth.controller;

import com.smartsync.api.dto.ChunkUploadDTO;
import com.smartsync.api.result.Result;
import com.smartsync.auth.service.ChunkUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/file/chunk")
public class ChunkUploadController {

    @Autowired
    private ChunkUploadService chunkUploadService;

    @GetMapping("/check")
    public Result<Object> checkChunk(
            @RequestParam String uploadId,
            @RequestParam String filename,
            @RequestParam Long totalSize,
            @RequestParam(required = false) Long folderId) {
        try {
            Object result = chunkUploadService.checkChunk(uploadId, filename, totalSize, folderId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查分片失败: {}", e.getMessage());
            return Result.error("检查分片失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public Result<Object> uploadChunk(@ModelAttribute ChunkUploadDTO dto) {
        try {
            Object result = chunkUploadService.uploadChunk(dto);
            return Result.success(result);
        } catch (Exception e) {
            log.error("上传分片失败: {}", e.getMessage());
            return Result.error("上传分片失败: " + e.getMessage());
        }
    }

    @PostMapping("/merge")
    public Result<Object> mergeChunks(@RequestParam String uploadId) {
        try {
            Object result = chunkUploadService.mergeChunks(uploadId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("合并分片失败: {}", e.getMessage());
            return Result.error("合并分片失败: " + e.getMessage());
        }
    }
}
