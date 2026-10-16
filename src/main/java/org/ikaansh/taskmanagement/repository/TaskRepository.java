package org.ikaansh.taskmanagement.repository;

import org.ikaansh.taskmanagement.entity.Task;
import org.ikaansh.taskmanagement.enums.TaskPriority;
import org.ikaansh.taskmanagement.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    Optional<Task> findByIdAndUserId(Long taskId, Long userId);

    @Query("""
        SELECT t FROM Task t
        WHERE t.user.id = :userId
          AND (:status IS NULL OR t.status = :status)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:dueDate IS NULL OR t.dueDate = :dueDate)
        """)
    Page<Task> findTasksByFilters(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("dueDate") LocalDate dueDate,
            Pageable pageable
    );
}
