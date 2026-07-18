package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.BulkSmsAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkSmsAuditLogRepository extends JpaRepository<BulkSmsAuditLog,Long> {
}
