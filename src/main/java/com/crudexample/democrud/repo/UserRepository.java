package com.crudexample.democrud.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crudexample.democrud.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	List<User> findByLastNameStartsWithIgnoreCase(String lastName);
}
