package org.ikaansh.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.ikaansh.taskmanagement.enums.TaskPriority;
import org.ikaansh.taskmanagement.enums.TaskStatus;

import java.time.LocalDate;

public record TaskRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title cannot exceed 255 characters")
        String title,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        TaskStatus status,

        TaskPriority priority,

        LocalDate dueDate
) {}

