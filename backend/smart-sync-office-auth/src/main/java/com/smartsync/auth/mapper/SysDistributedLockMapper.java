package com.smartsync.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsync.auth.entity.SysDistributedLock;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface SysDistributedLockMapper extends BaseMapper<SysDistributedLock> {

    @Insert("INSERT INTO sys_distributed_lock (lock_key, lock_value, expire_time, create_time) " +
            "VALUES (#{lockKey}, #{lockValue}, #{expireTime}, NOW())")
    int insertLock(@Param("lockKey") String lockKey, @Param("lockValue") String lockValue, @Param("expireTime") LocalDateTime expireTime);

    @Select("SELECT * FROM sys_distributed_lock WHERE lock_key = #{lockKey} FOR UPDATE")
    SysDistributedLock selectByKeyForUpdate(@Param("lockKey") String lockKey);

    @Update("UPDATE sys_distributed_lock SET lock_value = #{lockValue}, expire_time = #{expireTime}, update_time = NOW() " +
            "WHERE lock_key = #{lockKey} AND (expire_time <= NOW() OR lock_value = #{currentValue})")
    int updateLock(@Param("lockKey") String lockKey, @Param("lockValue") String lockValue,
                   @Param("expireTime") LocalDateTime expireTime, @Param("currentValue") String currentValue);

    @Delete("DELETE FROM sys_distributed_lock WHERE lock_key = #{lockKey} AND lock_value = #{lockValue}")
    int deleteLock(@Param("lockKey") String lockKey, @Param("lockValue") String lockValue);

    @Delete("DELETE FROM sys_distributed_lock WHERE expire_time <= NOW()")
    int deleteExpiredLocks();
}
