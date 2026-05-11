package com.smartsync.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsync.auth.entity.SysUser;
import com.smartsync.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("开始检查数据库初始化数据...");

        SysUser adminUser = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin")
        );

        if (adminUser == null) {
            log.info("正在创建默认管理员账号...");
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRealName("系统管理员");
            admin.setEmail("admin@example.com");
            admin.setPhone("13800000001");
            admin.setStatus(1);
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            admin.setIsDeleted(0);
            userMapper.insert(admin);
            log.info("管理员账号创建成功: admin / 123456");
        } else {
            log.info("管理员账号已存在，更新密码为: 123456");
            adminUser.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(adminUser);
        }

        SysUser employeeUser = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "employee")
        );

        if (employeeUser == null) {
            log.info("正在创建默认员工账号...");
            SysUser employee = new SysUser();
            employee.setUsername("employee");
            employee.setPassword(passwordEncoder.encode("123456"));
            employee.setRealName("普通员工");
            employee.setEmail("employee@example.com");
            employee.setPhone("13800000002");
            employee.setStatus(1);
            employee.setCreateTime(LocalDateTime.now());
            employee.setUpdateTime(LocalDateTime.now());
            employee.setIsDeleted(0);
            userMapper.insert(employee);
            log.info("员工账号创建成功: employee / 123456");
        } else {
            log.info("员工账号已存在，更新密码为: 123456");
            employeeUser.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(employeeUser);
        }

        log.info("数据库初始化完成！");
    }
}
