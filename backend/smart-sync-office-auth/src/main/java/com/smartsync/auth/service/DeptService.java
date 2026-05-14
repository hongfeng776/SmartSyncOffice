package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsync.api.exception.BusinessException;
import com.smartsync.api.result.ResultCode;
import com.smartsync.auth.entity.SysDept;
import com.smartsync.auth.mapper.SysDeptMapper;
import com.smartsync.auth.mapper.SysEmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final SysDeptMapper deptMapper;
    private final SysEmployeeMapper employeeMapper;

    public List<SysDept> getAllDepts() {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDept::getSortOrder).orderByAsc(SysDept::getId);
        List<SysDept> allDepts = deptMapper.selectList(wrapper);
        return buildTree(allDepts, 0L);
    }

    public List<SysDept> getFlatDepts() {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDept::getSortOrder).orderByAsc(SysDept::getId);
        return deptMapper.selectList(wrapper);
    }

    public SysDept getDeptById(Long id) {
        return deptMapper.selectById(id);
    }

    @Transactional
    public SysDept createDept(SysDept dept) {
        if (dept.getParentId() == null) {
            dept.setParentId(0L);
        }

        LambdaQueryWrapper<SysDept> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SysDept::getParentId, dept.getParentId())
                .eq(SysDept::getDeptName, dept.getDeptName())
                .eq(SysDept::getIsDeleted, 0);
        Long count = deptMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusinessException(400, "同一父部门下不能存在同名部门");
        }

        String ancestors = "0";
        if (dept.getParentId() > 0) {
            SysDept parentDept = deptMapper.selectById(dept.getParentId());
            if (parentDept == null) {
                throw new BusinessException(400, "父部门不存在");
            }
            ancestors = parentDept.getAncestors() + "," + dept.getParentId();
        }

        dept.setAncestors(ancestors);
        if (dept.getSortOrder() == null) {
            dept.setSortOrder(0);
        }
        if (dept.getStatus() == null) {
            dept.setStatus(1);
        }
        dept.setIsDeleted(0);
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.insert(dept);
        return dept;
    }

    @Transactional
    public SysDept updateDept(SysDept dept) {
        SysDept existDept = deptMapper.selectById(dept.getId());
        if (existDept == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        if (dept.getParentId() == null) {
            dept.setParentId(0L);
        }

        LambdaQueryWrapper<SysDept> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SysDept::getParentId, dept.getParentId())
                .eq(SysDept::getDeptName, dept.getDeptName())
                .eq(SysDept::getIsDeleted, 0)
                .ne(SysDept::getId, dept.getId());
        Long count = deptMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusinessException(400, "同一父部门下不能存在同名部门");
        }

        if (!existDept.getParentId().equals(dept.getParentId())) {
            String ancestors = "0";
            if (dept.getParentId() > 0) {
                SysDept parentDept = deptMapper.selectById(dept.getParentId());
                if (parentDept == null) {
                    throw new BusinessException(400, "父部门不存在");
                }
                ancestors = parentDept.getAncestors() + "," + dept.getParentId();
            }
            dept.setAncestors(ancestors);
        }

        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.updateById(dept);
        return dept;
    }

    @Transactional
    public void deleteDept(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        int childCount = deptMapper.countByParentId(id);
        if (childCount > 0) {
            throw new BusinessException(400, "存在子部门，不能删除");
        }

        int employeeCount = employeeMapper.countByDeptId(id);
        if (employeeCount > 0) {
            throw new BusinessException(400, "部门下存在员工，不能删除");
        }

        deptMapper.deleteById(id);
    }

    private List<SysDept> buildTree(List<SysDept> depts, Long parentId) {
        List<SysDept> tree = new ArrayList<>();
        for (SysDept dept : depts) {
            if (dept.getParentId().equals(parentId)) {
                dept.setChildren(buildTree(depts, dept.getId()));
                tree.add(dept);
            }
        }
        return tree;
    }
}
