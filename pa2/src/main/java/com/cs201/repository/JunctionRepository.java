package com.cs201.repository;

import com.cs201.model.Junction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JunctionRepository extends JpaRepository<Junction, Long> {

}