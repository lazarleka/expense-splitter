package com.coinis.splitter.repository;

import com.coinis.splitter.model.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByOrderByIdAsc();
}
