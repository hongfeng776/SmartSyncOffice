package com.smartsync.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsync.auth.entity.SysEmployee;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysEmployeeMapper extends BaseMapper<SysEmployee> {

    @Select("SELECT COUNT(*) FROM sys_employee WHERE dept_id = #{deptId} AND is_deleted = 0")
    int countByDeptId(@Param("deptId") Long deptId);
}
