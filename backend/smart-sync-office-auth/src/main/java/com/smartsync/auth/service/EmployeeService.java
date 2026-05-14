package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.exception.BusinessException;
import com.smartsync.api.result.ResultCode;
import com.smartsync.auth.entity.SysDept;
import com.smartsync.auth.entity.SysEmployee;
import com.smartsync.auth.mapper.SysDeptMapper;
import com.smartsync.auth.mapper.SysEmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final SysEmployeeMapper employeeMapper;
    private final SysDeptMapper deptMapper;

    public Page<SysEmployee> getEmployeePage(int page, int size, Long deptId, String keyword) {
        Page<SysEmployee> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();

        if (deptId != null) {
            wrapper.eq(SysEmployee::getDeptId, deptId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(SysEmployee::getRealName, keyword)
                    .or().like(SysEmployee::getEmpNo, keyword)
                    .or().like(SysEmployee::getPhone, keyword));
        }
        wrapper.orderByDesc(SysEmployee::getCreateTime);

        Page<SysEmployee> result = employeeMapper.selectPage(pageParam, wrapper);

        for (SysEmployee emp : result.getRecords()) {
            if (emp.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(emp.getDeptId());
                if (dept != null) {
                    emp.setDeptName(dept.getDeptName());
                }
            }
        }

        return result;
    }

    public SysEmployee getEmployeeById(Long id) {
        SysEmployee employee = employeeMapper.selectById(id);
        if (employee != null && employee.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(employee.getDeptId());
            if (dept != null) {
                employee.setDeptName(dept.getDeptName());
            }
        }
        return employee;
    }

    @Transactional
    public SysEmployee createEmployee(SysEmployee employee) {
        SysEmployee existEmp = employeeMapper.selectOne(
                new LambdaQueryWrapper<SysEmployee>().eq(SysEmployee::getEmpNo, employee.getEmpNo())
        );
        if (existEmp != null) {
            throw new BusinessException(400, "员工编号已存在");
        }

        if (employee.getDeptId() == null) {
            throw new BusinessException(400, "请选择部门");
        }
        SysDept dept = deptMapper.selectById(employee.getDeptId());
        if (dept == null) {
            throw new BusinessException(400, "部门不存在");
        }

        if (employee.getGender() == null) {
            employee.setGender(0);
        }
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        employee.setIsDeleted(0);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.insert(employee);
        employee.setDeptName(dept.getDeptName());
        return employee;
    }

    @Transactional
    public SysEmployee updateEmployee(SysEmployee employee) {
        SysEmployee existEmp = employeeMapper.selectById(employee.getId());
        if (existEmp == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        if (employee.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(employee.getDeptId());
            if (dept == null) {
                throw new BusinessException(400, "部门不存在");
            }
            employee.setDeptName(dept.getDeptName());
        }

        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.updateById(employee);
        return employee;
    }

    @Transactional
    public void deleteEmployee(Long id) {
        SysEmployee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        employeeMapper.deleteById(id);
    }

    public List<SysEmployee> getEmployeesByDept(Long deptId) {
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmployee::getDeptId, deptId);
        List<SysEmployee> employees = employeeMapper.selectList(wrapper);
        for (SysEmployee emp : employees) {
            if (emp.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(emp.getDeptId());
                if (dept != null) {
                    emp.setDeptName(dept.getDeptName());
                }
            }
        }
        return employees;
    }

    @Transactional
    public int batchUpdateDept(Long newDeptId, List<Long> employeeIds) {
        if (newDeptId == null) {
            throw new BusinessException(400, "请选择目标部门");
        }
        if (employeeIds == null || employeeIds.isEmpty()) {
            throw new BusinessException(400, "请选择要调整的员工");
        }

        SysDept dept = deptMapper.selectById(newDeptId);
        if (dept == null) {
            throw new BusinessException(400, "目标部门不存在");
        }

        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysEmployee::getId, employeeIds);
        List<SysEmployee> employees = employeeMapper.selectList(wrapper);

        if (employees.isEmpty()) {
            throw new BusinessException(400, "未找到有效的员工数据");
        }

        for (SysEmployee emp : employees) {
            emp.setDeptId(newDeptId);
            emp.setUpdateTime(LocalDateTime.now());
            employeeMapper.updateById(emp);
        }

        return employees.size();
    }
}
