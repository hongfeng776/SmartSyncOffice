package com.smartsync.auth.controller;

import com.smartsync.api.dto.FolderDTO;
import com.smartsync.api.result.Result;
import com.smartsync.auth.entity.SysFileFolder;
import com.smartsync.auth.service.FolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @GetMapping("/tree")
    public Result<List<SysFileFolder>> getFolderTree() {
        List<SysFileFolder> tree = folderService.getFolderTree();
        return Result.success(tree);
    }

    @PostMapping
    public Result<SysFileFolder> createFolder(@RequestBody FolderDTO dto) {
        SysFileFolder folder = folderService.createFolder(dto);
        return Result.success(folder);
    }

    @PutMapping
    public Result<Void> updateFolder(@RequestBody FolderDTO dto) {
        folderService.updateFolder(dto);
        return Result.success();
    }

    @DeleteMapping("/{folderId}")
    public Result<Void> deleteFolder(@PathVariable Long folderId) {
        folderService.deleteFolder(folderId);
        return Result.success();
    }
}
