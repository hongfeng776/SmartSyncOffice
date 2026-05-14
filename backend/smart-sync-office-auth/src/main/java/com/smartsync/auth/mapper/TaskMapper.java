package com.smartsync.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsync.auth.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
