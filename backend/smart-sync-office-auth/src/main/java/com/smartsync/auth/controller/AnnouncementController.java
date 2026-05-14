package com.smartsync.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.result.Result;
import com.smartsync.auth.entity.SysAnnouncement;
import com.smartsync.auth.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Result<Map<String, Object>> getAnnouncementPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status
    ) {
        Page<SysAnnouncement> announcementPage = announcementService.getAnnouncementPage(page, size, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", announcementPage.getRecords());
        result.put("total", announcementPage.getTotal());
        result.put("page", announcementPage.getCurrent());
        result.put("size", announcementPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<SysAnnouncement> getAnnouncementById(@PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementById(id));
    }

    @PostMapping
    public Result<Void> createAnnouncement(@RequestBody SysAnnouncement announcement) {
        announcementService.createAnnouncement(announcement);
        return Result.success();
    }

    @PutMapping
    public Result<Void> updateAnnouncement(@RequestBody SysAnnouncement announcement) {
        announcementService.updateAnnouncement(announcement);
        return Result.success();
    }

    @PutMapping("/{id}/publish")
    public Result<Void> publishAnnouncement(@PathVariable Long id) {
        announcementService.publishAnnouncement(id);
        return Result.success();
    }

    @PutMapping("/{id}/withdraw")
    public Result<Void> withdrawAnnouncement(@PathVariable Long id) {
        announcementService.withdrawAnnouncement(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }
}
