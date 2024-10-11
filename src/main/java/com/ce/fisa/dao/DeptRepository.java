package com.ce.fisa.dao;
//jdbc -> jpa -> spring data jpa
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ce.fisa.model.entity.Dept;

//dept table와 1:1 매핑
//DAO구조의 이런 interface는 table 당 1:1 기본
//<Dept, Integer> - table과 매핑된 entity명, pk의 타입 
@Repository
public interface DeptRepository extends JpaRepository<Dept, Integer>{
	//기본 crud 
	//findAll
}
