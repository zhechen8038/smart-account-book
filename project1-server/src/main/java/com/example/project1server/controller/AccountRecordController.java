package com.example.project1server.controller;

import com.example.project1server.dto.RecordResponse;
import com.example.project1server.dto.RecordSummaryResponse;
import com.example.project1server.dto.SaveRecordRequest;
import com.example.project1server.service.AccountRecordService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.example.project1server.dto.BillRecognitionResponse;
import com.example.project1server.service.BillRecognitionService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/records")
public class AccountRecordController {

    private final AccountRecordService recordService;
    private final BillRecognitionService recognitionService;

    public AccountRecordController(
            AccountRecordService recordService,
            BillRecognitionService recognitionService
    ) {
        this.recordService = recordService;
        this.recognitionService = recognitionService;
    }



    @PostMapping
    public RecordResponse create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SaveRecordRequest request) {
        return recordService.create(userId, request);
    }

    @GetMapping
    public List<RecordResponse> findByMonth(
            @AuthenticationPrincipal Long userId,
            @RequestParam String month) {
        return recordService.findByMonth(userId, month);
    }

    @PutMapping("/{recordId}")
    public RecordResponse update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long recordId,
            @Valid @RequestBody SaveRecordRequest request) {
        return recordService.update(userId, recordId, request);
    }

    @DeleteMapping("/{recordId}")
    public Map<String, String> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long recordId) {
        recordService.delete(userId, recordId);
        return Map.of("message", "删除成功");
    }

    @GetMapping("/summary")
    public RecordSummaryResponse summary(
            @AuthenticationPrincipal Long userId,
            @RequestParam String month) {
        return recordService.summary(userId, month);
    }

    @GetMapping("/count")
    public Map<String, Long> count(
            @AuthenticationPrincipal Long userId) {
        return Map.of("count", recordService.count(userId));
    }

    @PostMapping(
            value = "/recognize",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public BillRecognitionResponse recognize(
            @RequestPart("image") MultipartFile image
    ) {
        return recognitionService.recognize(image);
    }
}