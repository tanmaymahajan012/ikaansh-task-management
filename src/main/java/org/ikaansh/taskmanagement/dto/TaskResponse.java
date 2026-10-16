package org.ikaansh.taskmanagement.dto;

import org.ikaansh.taskmanagement.enums.TaskPriority;
import org.ikaansh.taskmanagement.enums.TaskStatus;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long userId
) {}
