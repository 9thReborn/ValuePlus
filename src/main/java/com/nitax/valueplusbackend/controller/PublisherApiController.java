package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.dto.request.PublisherChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.PublisherChurnRecordDTO;
import com.nitax.valueplusbackend.service.CsvStorageService;
import com.nitax.valueplusbackend.service.PublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@RestController
@RequestMapping("/api/publishers")
@CrossOrigin
@Slf4j
@Tag(name = "Publisher API", description = "Public endpoints for publishers to query their campaign data using an API key")
public class PublisherApiController {

    private final PublisherService publisherService;
    private final CsvStorageService csvStorageService;

    @Autowired
    public PublisherApiController(PublisherService publisherService, CsvStorageService csvStorageService) {
        this.publisherService = publisherService;
        this.csvStorageService = csvStorageService;
    }

    @Operation(
        summary = "Get churn report as CSV download",
        description = """
            Returns a download link to a CSV file listing all churned subscribers for the authenticated publisher. \
            A subscriber is counted as churned if they unsubscribed within 8 days of acquisition. \
            Authenticate using your API key.\
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "CSV generated — follow the downloadUrl to download",
            content = @Content(schema = @Schema(example = "{\"success\":true,\"data\":\"https://...csv\"}"))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Invalid API key")
    })
    @PostMapping("/churn/report")
    public ResponseEntity<ApiResponse<String>> getChurnReport(
            @RequestBody @Valid PublisherChurnReportRequestDTO requestDTO) throws IOException {
        List<PublisherChurnRecordDTO> records = publisherService.generatePublisherApiChurnReport(requestDTO);
        String url = csvStorageService.uploadCsvBytes(toCsvBytes(records, "Date,Campaign,Click ID,Source ID"), "churn-report");
        return new ResponseEntity<>(ApiResponse.<String>builder().success(true).data(url).build(), HttpStatus.OK);
    }

    @Operation(
        summary = "Get 48-hour churn report as CSV download",
        description = """
            Returns a download link to a CSV file listing all churned subscribers for the authenticated publisher \
            where the subscriber unsubscribed within 48 hours of acquisition. \
            Authenticate using your API key.\
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "CSV generated — follow the downloadUrl to download",
            content = @Content(schema = @Schema(example = "{\"success\":true,\"data\":\"https://...csv\"}"))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Invalid API key")
    })
    @PostMapping("/churn/report/48hrs")
    public ResponseEntity<ApiResponse<String>> getChurnReport48hrs(
            @RequestBody @Valid PublisherChurnReportRequestDTO requestDTO) throws IOException {
        List<PublisherChurnRecordDTO> records = publisherService.generatePublisherApiChurnReport48hrs(requestDTO);
        String url = csvStorageService.uploadCsvBytes(toCsvBytes(records, "Date,Campaign,Click ID,Source ID"), "churn-report-48hrs");
        return new ResponseEntity<>(ApiResponse.<String>builder().success(true).data(url).build(), HttpStatus.OK);
    }

    @Operation(
        summary = "Get conversions report as CSV download",
        description = """
            Returns a download link to a CSV file listing all conversion click IDs for the authenticated publisher. \
            A conversion is recorded when a subscriber is successfully acquired (status: PUBLISHER_HOOK_SENT). \
            Authenticate using your API key.\
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "CSV generated — follow the downloadUrl to download",
            content = @Content(schema = @Schema(example = "{\"success\":true,\"data\":\"https://...csv\"}"))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Invalid API key")
    })
    @PostMapping("/conversions/report")
    public ResponseEntity<ApiResponse<String>> getConversionsReport(
            @RequestBody @Valid PublisherChurnReportRequestDTO requestDTO) throws IOException {
        List<PublisherChurnRecordDTO> records = publisherService.generatePublisherConversionsReport(requestDTO);
        String url = csvStorageService.uploadCsvBytes(toCsvBytes(records, "Date,Campaign,Click ID,Source ID"), "conversions-report");
        return new ResponseEntity<>(ApiResponse.<String>builder().success(true).data(url).build(), HttpStatus.OK);
    }

    private byte[] toCsvBytes(List<PublisherChurnRecordDTO> records, String header) throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(header.split(",")))) {
            for (PublisherChurnRecordDTO r : records) {
                printer.printRecord(r.getReportDate(), r.getCampaignName(), r.getClickId(), r.getSourceId());
            }
        }
        return sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
