package com.example.addataservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AdDataController {
    private final FacebookAdService adService;

    public AdDataController(FacebookAdService adService) {
        this.adService = adService;
    }

    @GetMapping("/ads/{accountId}")
    public List<Map<String, Object>> getAdData(@PathVariable String accountId) throws Exception {
        return adService.fetchAdData(accountId);
    }
}