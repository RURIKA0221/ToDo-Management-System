package com.dmm.task.data.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dmm.task.data.entity.Tasks;

public interface TasksRepository extends JpaRepository<Tasks, Integer> {
	@Query("select a from Tasks a where a.date between :from and :to and name = :name")
	List<Tasks> findByDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("name") String name);

	@Query("select b from Tasks b where b.date between :from and :to and (b.name = :name or b.name in (select c.name from Users c where c.isAdmin = true))")
	List<Tasks> findAdminAndUserTasksByDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to,
			@Param("name") String name);

}
