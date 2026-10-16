package org.ikaansh.taskmanagement.service;

import org.ikaansh.taskmanagement.dto.TaskRequest;
import org.ikaansh.taskmanagement.dto.TaskResponse;
import org.ikaansh.taskmanagement.entity.Task;
import org.ikaansh.taskmanagement.entity.User;
import org.ikaansh.taskmanagement.enums.TaskPriority;
import org.ikaansh.taskmanagement.enums.TaskStatus;
import org.ikaansh.taskmanagement.exception.ResourceNotFoundException;
import org.ikaansh.taskmanagement.repository.TaskRepository;
import org.ikaansh.taskmanagement.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null,
                        List.of()
                )
        );

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTask_ShouldUseDefaultStatusAndPriority() {
        TaskRequest request = new TaskRequest(
                "Complete assignment",
                "Finish the Spring Boot assignment",
                null,
                null,
                LocalDate.of(2026, 10, 20)
        );

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    task.setId(10L);
                    return task;
                });

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Complete assignment", response.title());
        assertEquals(TaskStatus.TODO, response.status());
        assertEquals(TaskPriority.MEDIUM, response.priority());
        assertEquals(1L, response.userId());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getMyTasks_ShouldReturnFilteredPaginatedTasks() {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Complete assignment");
        task.setDescription("Finish the assignment");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.of(2026, 10, 20));
        task.setUser(user);

        Pageable pageable = PageRequest.of(0, 5);

        Page<Task> taskPage = new PageImpl<>(
                List.of(task),
                pageable,
                1
        );

        when(taskRepository.findTasksByFilters(
                eq(1L),
                eq(TaskStatus.TODO),
                eq(TaskPriority.HIGH),
                isNull(),
                any(Pageable.class)
        )).thenReturn(taskPage);

        Page<TaskResponse> result = taskService.getMyTasks(
                TaskStatus.TODO,
                TaskPriority.HIGH,
                null,
                0,
                5
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals("Complete assignment",
                result.getContent().get(0).title());
        assertEquals(TaskPriority.HIGH,
                result.getContent().get(0).priority());

        verify(taskRepository).findTasksByFilters(
                eq(1L),
                eq(TaskStatus.TODO),
                eq(TaskPriority.HIGH),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    void getTaskById_ShouldThrowWhenTaskDoesNotExistForUser() {
        when(taskRepository.findByIdAndUserId(99L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(99L)
        );

        verify(taskRepository)
                .findByIdAndUserId(99L, 1L);
    }
}
