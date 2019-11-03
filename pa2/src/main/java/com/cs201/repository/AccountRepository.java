package com.cs201.repository;

import com.cs201.model.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Accounts, Long> {
    @Query(nativeQuery = true, value= "SELECT u from Accounts u where u.username=?1")
    List<Accounts> findAccountsByUsername(String username);

    @Query(nativeQuery = true, value= "SELECT u from Accounts u where u.username=?1 and u.password=?2")
    List<Accounts> findAccount(String username, String password);

}