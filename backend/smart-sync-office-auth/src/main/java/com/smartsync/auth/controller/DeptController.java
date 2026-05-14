package com.smartsync.auth.controller;

import com.smartsync.api.result.Result;
import com.smartsync.auth.annotation.OperationLog;
import com.smartsync.auth.entity.SysDept;
import com.smartsync.auth.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depts")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @GetMapping
    public Result<List<SysDept>> getAllDepts() {
        return Result.success(deptService.getAllDepts());
    }

    @GetMapping("/flat")
    public Result<List<SysDept>> getFlatDepts() {
        return Result.success(deptService.getFlatDepts());
    }

    @GetMapping("/{id}")
    public Result<SysDept> getDeptById(@PathVariable Long id) {
        return Result.success(deptService.getDeptById(id));
    }

    @PostMapping
    @OperationLog(moduleName = "部门管理", operationType = "新增", description = "新增部门")
    public Result<SysDept> createDept(@RequestBody SysDept dept) {
        return Result.success(deptService.createDept(dept));
    }

    @PutMapping
    @OperationLog(moduleName = "部门管理", operationType = "修改", description = "修改部门信息")
    public Result<SysDept> updateDept(@RequestBody SysDept dept) {
        return Result.success(deptService.updateDept(dept));
    }

    @DeleteMapping("/{id}")
    @OperationLog(moduleName = "部门管理", operationType = "删除", description = "删除部门")
    public Result<Void> deleteDept(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.success();
    }
}
