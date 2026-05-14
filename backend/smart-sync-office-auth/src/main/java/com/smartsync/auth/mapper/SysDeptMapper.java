package com.smartsync.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsync.auth.entity.SysDept;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysDeptMapper extends BaseMapper<SysDept> {

    @Select("SELECT COUNT(*) FROM sys_dept WHERE parent_id = #{deptId} AND is_deleted = 0")
    int countByParentId(@Param("deptId") Long deptId);
}
