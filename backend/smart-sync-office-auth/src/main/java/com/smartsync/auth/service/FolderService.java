package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartsync.api.dto.FolderDTO;
import com.smartsync.auth.entity.SysFile;
import com.smartsync.auth.entity.SysFileFolder;
import com.smartsync.auth.entity.SysFileUploadLog;
import com.smartsync.auth.mapper.SysFileFolderMapper;
import com.smartsync.auth.mapper.SysFileMapper;
import com.smartsync.auth.mapper.SysFileUploadLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FolderService {

    @Autowired
    private SysFileFolderMapper folderMapper;

    @Autowired
    private SysFileMapper sysFileMapper;

    @Autowired
    private SysFileUploadLogMapper uploadLogMapper;

    public List<SysFileFolder> getFolderTree() {
        LambdaQueryWrapper<SysFileFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFileFolder::getIsDeleted, 0);
        wrapper.orderByAsc(SysFileFolder::getSort);
        wrapper.orderByDesc(SysFileFolder::getCreateTime);

        List<SysFileFolder> allFolders = folderMapper.selectList(wrapper);
        return buildTree(allFolders, 0L);
    }

    private List<SysFileFolder> buildTree(List<SysFileFolder> allFolders, Long parentId) {
        List<SysFileFolder> tree = new ArrayList<>();
        for (SysFileFolder folder : allFolders) {
            Long currentParentId = folder.getParentId() == null ? 0L : folder.getParentId();
            if (currentParentId.equals(parentId)) {
                tree.add(folder);
                folder.setChildren(buildTree(allFolders, folder.getId()));
            }
        }
        return tree;
    }

    @Transactional
    public SysFileFolder createFolder(FolderDTO dto) {
        SysFileFolder folder = new SysFileFolder();
        folder.setFolderName(dto.getFolderName());
        folder.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        folder.setSort(dto.getSort() == null ? 0 : dto.getSort());
        folder.setCreatorId("1");
        folder.setCreatorName("admin");
        folder.setIsDeleted(0);
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());

        if (folder.getParentId() == 0L) {
            folder.setLevel(1);
            folder.setFolderPath("/" + folder.getFolderName());
        } else {
            SysFileFolder parent = folderMapper.selectById(folder.getParentId());
            if (parent != null) {
                folder.setLevel(parent.getLevel() + 1);
                folder.setFolderPath(parent.getFolderPath() + "/" + folder.getFolderName());
            } else {
                folder.setLevel(1);
                folder.setFolderPath("/" + folder.getFolderName());
            }
        }

        folderMapper.insert(folder);
        log.info("创建文件夹成功: {}", folder.getFolderName());
        return folder;
    }

    @Transactional
    public void updateFolder(FolderDTO dto) {
        SysFileFolder folder = folderMapper.selectById(dto.getId());
        if (folder == null) {
            throw new RuntimeException("文件夹不存在");
        }

        folder.setFolderName(dto.getFolderName());
        folder.setSort(dto.getSort());
        folder.setUpdateTime(LocalDateTime.now());

        folderMapper.updateById(folder);
        log.info("更新文件夹成功: {}", folder.getFolderName());
    }

    @Transactional
    public void deleteFolder(Long folderId) {
        SysFileFolder folder = folderMapper.selectById(folderId);
        if (folder == null) {
            throw new RuntimeException("文件夹不存在");
        }

        folder.setIsDeleted(1);
        folder.setUpdateTime(LocalDateTime.now());
        folderMapper.updateById(folder);

        deleteFilesInFolder(folderId);

        deleteChildrenRecursive(folderId);
        log.info("删除文件夹成功: {}", folder.getFolderName());
    }

    private void deleteFilesInFolder(Long folderId) {
        LambdaQueryWrapper<SysFile> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(SysFile::getFolderId, folderId);
        fileWrapper.eq(SysFile::getIsDeleted, 0);

        List<SysFile> files = sysFileMapper.selectList(fileWrapper);
        for (SysFile file : files) {
            file.setIsDeleted(1);
            file.setUpdateTime(LocalDateTime.now());
            sysFileMapper.updateById(file);

            LambdaUpdateWrapper<SysFileUploadLog> logWrapper = new LambdaUpdateWrapper<>();
            logWrapper.eq(SysFileUploadLog::getFileId, file.getId());
            logWrapper.set(SysFileUploadLog::getUploadStatus, "DELETED");
            logWrapper.set(SysFileUploadLog::getUpdateTime, LocalDateTime.now());
            uploadLogMapper.update(null, logWrapper);
        }
    }

    private void deleteChildrenRecursive(Long parentId) {
        LambdaQueryWrapper<SysFileFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFileFolder::getParentId, parentId);
        wrapper.eq(SysFileFolder::getIsDeleted, 0);

        List<SysFileFolder> children = folderMapper.selectList(wrapper);
        for (SysFileFolder child : children) {
            child.setIsDeleted(1);
            child.setUpdateTime(LocalDateTime.now());
            folderMapper.updateById(child);

            deleteFilesInFolder(child.getId());
            deleteChildrenRecursive(child.getId());
        }
    }
}
