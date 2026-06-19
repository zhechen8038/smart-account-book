package com.example.project1server.repository;

import com.example.project1server.entity.AccountRecord;
import com.example.project1server.entity.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountRecordRepository
        extends JpaRepository<AccountRecord, Long> {

    List<AccountRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
            Long userId,
            LocalDate start,
            LocalDate end
    );

    Optional<AccountRecord> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Query("""
            select coalesce(sum(r.amount), 0)
            from AccountRecord r
            where r.user.id = :userId
              and r.type = :type
              and r.recordDate between :start and :end
            """)
    BigDecimal sumAmount(
            @Param("userId") Long userId,
            @Param("type") RecordType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}