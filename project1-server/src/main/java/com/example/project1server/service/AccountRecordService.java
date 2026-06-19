package com.example.project1server.service;

import com.example.project1server.dto.RecordResponse;
import com.example.project1server.dto.RecordSummaryResponse;
import com.example.project1server.dto.SaveRecordRequest;
import com.example.project1server.entity.AccountRecord;
import com.example.project1server.entity.RecordType;
import com.example.project1server.entity.User;
import com.example.project1server.exception.BusinessException;
import com.example.project1server.repository.AccountRecordRepository;
import com.example.project1server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class AccountRecordService {

    private final AccountRecordRepository recordRepository;
    private final UserRepository userRepository;

    public AccountRecordService(
            AccountRecordRepository recordRepository,
            UserRepository userRepository) {
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    public RecordResponse create(Long userId, SaveRecordRequest request) {
        User user = findUser(userId);

        AccountRecord record = new AccountRecord();
        record.setUser(user);
        updateFields(record, request);

        return toResponse(recordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<RecordResponse> findByMonth(Long userId, String monthText) {
        YearMonth month = parseMonth(monthText);

        return recordRepository
                .findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
                        userId,
                        month.atDay(1),
                        month.atEndOfMonth()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RecordResponse update(
            Long userId,
            Long recordId,
            SaveRecordRequest request) {
        AccountRecord record = findRecord(userId, recordId);
        updateFields(record, request);
        return toResponse(recordRepository.save(record));
    }

    public void delete(Long userId, Long recordId) {
        recordRepository.delete(findRecord(userId, recordId));
    }

    @Transactional(readOnly = true)
    public long count(Long userId) {
        return recordRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public RecordSummaryResponse summary(Long userId, String monthText) {
        YearMonth month = parseMonth(monthText);
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        BigDecimal income = recordRepository.sumAmount(
                userId, RecordType.INCOME, start, end);

        BigDecimal expense = recordRepository.sumAmount(
                userId, RecordType.EXPENSE, start, end);

        return new RecordSummaryResponse(
                month.toString(),
                income,
                expense,
                income.subtract(expense)
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private AccountRecord findRecord(Long userId, Long recordId) {
        return recordRepository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new BusinessException("记账记录不存在"));
    }

    private YearMonth parseMonth(String monthText) {
        try {
            return YearMonth.parse(monthText);
        } catch (Exception exception) {
            throw new BusinessException("月份格式必须为 yyyy-MM");
        }
    }

    private void updateFields(
            AccountRecord record,
            SaveRecordRequest request) {
        record.setType(request.type());
        record.setCategory(request.category());
        record.setAmount(request.amount());
        record.setRecordDate(request.recordDate());
        record.setRemark(request.remark());
    }

    private RecordResponse toResponse(AccountRecord record) {
        return new RecordResponse(
                record.getId(),
                record.getType(),
                record.getCategory(),
                record.getAmount(),
                record.getRecordDate(),
                record.getRemark(),
                record.getBillImageUrl()
        );
    }
}