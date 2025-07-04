package com.example.addataservice.controller;

import com.example.addataservice.Service.AccountService;
import com.example.addataservice.Service.FacebookAdService;
import com.example.addataservice.model.AdAccountList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AdDataController {
    private final FacebookAdService adService;
    private final AccountService accountservice;

    public AdDataController(FacebookAdService adService, AccountService accountservice) {
        this.adService = adService;
        this.accountservice = accountservice;
    }

    @GetMapping("/ads/{accountId}")
    public List<Map<String, Object>> getAdData(@PathVariable String accountId) throws Exception {
        return adService.fetchAdData(accountId);
    }

    @GetMapping("/accounts")
    public List<AdAccountList> getAccountList() throws Exception {
        return accountservice.fetchAccountList();
    }
}