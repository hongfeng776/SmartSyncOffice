package com.smartsync.auth.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartsync.auth.entity.SysNotification;
import com.smartsync.auth.entity.Task;
import com.smartsync.auth.mapper.SysNotificationMapper;
import com.smartsync.auth.mapper.TaskMapper;
import com.smartsync.auth.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskReminderScheduler {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final SysNotificationMapper notificationMapper;

    private static final int URGENT_HOURS = 24;

    @Scheduled(cron = "0 0 * * * ?")
    @Async("taskExecutor")
    public void sendUrgentTaskReminders() {
        log.info("开始执行任务临近截止提醒调度");
        try {
            List<Task> urgentTasks = taskService.getUrgentTasks(URGENT_HOURS);
            log.info("发现{}个即将到期的任务需要提醒", urgentTasks.size());

            for (Task task : urgentTasks) {
                if (task.getRemindSent() != null && task.getRemindSent() == 1) {
                    continue;
                }

                sendNotification(task, "即将到期提醒", String.format("您有一个任务即将在%d小时内到期：%s", URGENT_HOURS, task.getTitle()));

                taskMapper.update(null,
                        new LambdaUpdateWrapper<Task>()
                                .eq(Task::getId, task.getId())
                                .set(Task::getRemindSent, 1)
                                .set(Task::getUpdateTime, LocalDateTime.now()));

                log.info("任务{}临近截止提醒已发送给执行人{}", task.getTaskNo(), task.getAssigneeName());
            }
        } catch (Exception e) {
            log.error("任务临近截止提醒调度执行失败", e);
        }
        log.info("任务临近截止提醒调度执行完成");
    }

    @Scheduled(cron = "0 30 * * * ?")
    @Async("taskExecutor")
    public void sendOverdueTaskReminders() {
        log.info("开始执行任务逾期提醒调度");
        try {
            List<Task> overdueTasks = taskService.getOverdueTasks();
            log.info("发现{}个已逾期的任务需要提醒", overdueTasks.size());

            for (Task task : overdueTasks) {
                if (task.getOverdueRemindSent() != null && task.getOverdueRemindSent() == 1) {
                    continue;
                }

                sendNotification(task, "任务逾期提醒", String.format("您有一个任务已逾期：%s", task.getTitle()));

                taskMapper.update(null,
                        new LambdaUpdateWrapper<Task>()
                                .eq(Task::getId, task.getId())
                                .set(Task::getOverdueRemindSent, 1)
                                .set(Task::getUpdateTime, LocalDateTime.now()));

                log.info("任务{}逾期提醒已发送给执行人{}", task.getTaskNo(), task.getAssigneeName());
            }
        } catch (Exception e) {
            log.error("任务逾期提醒调度执行失败", e);
        }
        log.info("任务逾期提醒调度执行完成");
    }

    private void sendNotification(Task task, String title, String content) {
        if (task.getAssigneeId() == null) {
            return;
        }

        SysNotification notification = new SysNotification();
        notification.setUserId(task.getAssigneeId());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("TASK_REMINDER");
        notification.setBusinessType("TASK");
        notification.setBusinessId(task.getId());
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }
}
