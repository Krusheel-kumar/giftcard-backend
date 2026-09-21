package com.popobob.giftcard.controller;

import com.popobob.giftcard.dto.AdminCustomerDTO;
import com.popobob.giftcard.dto.AdminCustomerRewardDTO;
import com.popobob.giftcard.model.JourneyCustomer;
import com.popobob.giftcard.repository.CustomerRewardRepository;
import com.popobob.giftcard.repository.JourneyCustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/customers")
@CrossOrigin(origins = "*")
public class AdminCustomerController {

    @Autowired
    private JourneyCustomerRepository customerRepository;

    @Autowired
    private CustomerRewardRepository rewardRepository;

    @GetMapping
    public Page<AdminCustomerDTO> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        // Clean empty search params
        if (search != null && search.trim().isEmpty()) {
            search = null;
        }
        return customerRepository.findAdminCustomers(search, pageable);
    }

    @GetMapping("/{mobile}/history")
    public List<AdminCustomerRewardDTO> getCustomerHistory(@PathVariable String mobile) {
        Optional<JourneyCustomer> customer = customerRepository.findByMobile(mobile);
        if (customer.isPresent()) {
            return rewardRepository.findHistoryByCustomerId(customer.get().getId());
        }
        return List.of();
    }

    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportCustomers() {
        StreamingResponseBody stream = out -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                writer.write("Name,Mobile,Join Date\n");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                int pageNum = 0;
                int pageSize = 500;
                Page<JourneyCustomer> customerPage;
                
                do {
                    customerPage = customerRepository.findAll(PageRequest.of(pageNum, pageSize));
                    for (JourneyCustomer c : customerPage.getContent()) {
                        String name = c.getName() != null ? c.getName().replace(",", " ") : "Unknown";
                        String mobile = c.getMobile() != null ? c.getMobile() : "";
                        String date = c.getCreatedAt() != null ? c.getCreatedAt().format(formatter) : "";
                        writer.write(name + "," + mobile + "," + date + "\n");
                    }
                    writer.flush();
                    pageNum++;
                } while (customerPage.hasNext());
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(stream);
    }
}
