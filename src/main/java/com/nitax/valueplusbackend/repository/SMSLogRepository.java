package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.SMSLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface SMSLogRepository extends JpaRepository<SMSLog, Long> {}
