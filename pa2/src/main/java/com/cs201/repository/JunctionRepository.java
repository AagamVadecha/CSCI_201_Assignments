package com.cs201.repository;

import com.cs201.model.Junction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface JunctionRepository extends JpaRepository<Junction, Long> {
    List<Junction> findJunctionByAccount_UserIDOrderByTimeStamp(int userID);
    @Transactional
    void removeJunctionsByAccount_UserIDAndBookID(int userID, String bookID);
    List<Junction> findJunctionByAccount_UserIDAndBookID(int userID, String bookID);
}