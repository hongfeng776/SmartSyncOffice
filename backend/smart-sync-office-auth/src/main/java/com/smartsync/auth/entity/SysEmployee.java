package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_employee")
public class SysEmployee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String empNo;
    private Long userId;
    private Long deptId;
    private String realName;
    private Integer gender;
    private LocalDate birthday;
    private String idCard;
    private String phone;
    private String email;
    private String position;
    private LocalDate entryDate;
    private String workPlace;
    private String avatar;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;

    @TableField(exist = false)
    private String deptName;
}
