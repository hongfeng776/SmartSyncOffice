package com.smartsync.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.dto.BatchDeptDTO;
import com.smartsync.api.result.Result;
import com.smartsync.auth.annotation.OperationLog;
import com.smartsync.auth.entity.SysEmployee;
import com.smartsync.auth.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Result<Map<String, Object>> getEmployeePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String keyword
    ) {
        Page<SysEmployee> empPage = employeeService.getEmployeePage(page, size, deptId, keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", empPage.getRecords());
        result.put("total", empPage.getTotal());
        result.put("page", empPage.getCurrent());
        result.put("size", empPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<SysEmployee> getEmployeeById(@PathVariable Long id) {
        return Result.success(employeeService.getEmployeeById(id));
    }

    @PostMapping
    @OperationLog(moduleName = "员工管理", operationType = "新增", description = "新增员工")
    public Result<SysEmployee> createEmployee(@RequestBody SysEmployee employee) {
        return Result.success(employeeService.createEmployee(employee));
    }

    @PutMapping
    @OperationLog(moduleName = "员工管理", operationType = "修改", description = "修改员工信息")
    public Result<SysEmployee> updateEmployee(@RequestBody SysEmployee employee) {
        return Result.success(employeeService.updateEmployee(employee));
    }

    @DeleteMapping("/{id}")
    @OperationLog(moduleName = "员工管理", operationType = "删除", description = "删除员工")
    public Result<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }

    @GetMapping("/dept/{deptId}")
    public Result<List<SysEmployee>> getEmployeesByDept(@PathVariable Long deptId) {
        return Result.success(employeeService.getEmployeesByDept(deptId));
    }

    @PutMapping("/batch-dept")
    @OperationLog(moduleName = "员工管理", operationType = "批量调整", description = "批量调整员工部门")
    public Result<Map<String, Object>> batchUpdateDept(@RequestBody BatchDeptDTO batchDeptDTO) {
        int count = employeeService.batchUpdateDept(batchDeptDTO.getNewDeptId(), batchDeptDTO.getEmployeeIds());
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }
}
