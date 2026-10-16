package org.ikaansh.taskmanagement.service;

import org.ikaansh.taskmanagement.dto.TaskRequest;
import org.ikaansh.taskmanagement.dto.TaskResponse;
import org.ikaansh.taskmanagement.entity.Task;
import org.ikaansh.taskmanagement.entity.User;
import org.ikaansh.taskmanagement.enums.TaskPriority;
import org.ikaansh.taskmanagement.enums.TaskStatus;
import org.ikaansh.taskmanagement.repository.TaskRepository;
import org.ikaansh.taskmanagement.repository.UserRepository;
import org.ikaansh.taskmanagement.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getUser().getId()
        );
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        User user = getCurrentUser();

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status() != null
                ? request.status() : TaskStatus.TODO);
        task.setPriority(request.priority() != null
                ? request.priority() : TaskPriority.MEDIUM);
        task.setDueDate(request.dueDate());
        task.setUser(user);

        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks() {
        User user = getCurrentUser();

        return taskRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getMyTasks(
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        User user = getCurrentUser();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        return taskRepository.findTasksByFilters(
                        user.getId(),
                        status,
                        priority,
                        dueDate,
                        pageable
                )
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        User user = getCurrentUser();

        Task task = taskRepository
                .findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with ID: " + taskId));

        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest request) {
        User user = getCurrentUser();

        Task task = taskRepository
                .findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with ID: " + taskId));

        task.setTitle(request.title());
        task.setDescription(request.description());

        if (request.status() != null) {
            task.setStatus(request.status());
        }

        if (request.priority() != null) {
            task.setPriority(request.priority());
        }

        task.setDueDate(request.dueDate());

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId) {
        User user = getCurrentUser();

        Task task = taskRepository
                .findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with ID: " + taskId));

        taskRepository.delete(task);
    }
}

