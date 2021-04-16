package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.SystemLog;

public interface SystemLogDao extends  JpaRepository<SystemLog, Integer> {

}
