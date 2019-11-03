package com.cs201.repository;

import com.cs201.model.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Accounts, Long> {
    List<Accounts> findAccountsByUsernameEquals(String username);

    List<Accounts> findAccountsByUsernameEqualsAndPasswordEquals(String username, String password);


}