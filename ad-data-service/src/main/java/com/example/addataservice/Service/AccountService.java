package com.example.addataservice.Service;

import com.example.addataservice.util.ReflectionUtil;
import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.APIException;
import com.facebook.ads.sdk.APIRequest;
import com.facebook.ads.sdk.AdAccount;
import com.example.addataservice.model.AdAccountList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AccountService 处理 Facebook 广告账户列表查询。
 */
@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final APIContext context;

    public AccountService(@Value("${facebook.access-token}") String accessToken) {
        this.context = new APIContext(accessToken);
        logger.info("Initializing AccountService with access token: [REDACTED]");
    }

    public List<AdAccountList> fetchAccountList() throws APIException {
        logger.info("Fetching account list");
        APIRequest<AdAccount> request = new APIRequest<AdAccount>(context)
                .setEndpoint("me/adaccounts")
                .setRequestType("GET")
                .setFields("id", "name", "account_status", "currency", "timezone_id", "business_name", "amount_spent")
                .execute();

        List<AdAccountList> accounts = new ArrayList<>();
        for (AdAccount account : request.getResult()) {
            try {
                String id = (String) ReflectionUtil.getFieldValue(account, "id");
                String name = (String) ReflectionUtil.getFieldValue(account, "name");
                String accountStatus = (String) ReflectionUtil.getFieldValue(account, "account_status");
                String currency = (String) ReflectionUtil.getFieldValue(account, "currency");
                String timezoneId = (String) ReflectionUtil.getFieldValue(account, "timezone_id");
                String businessName = (String) ReflectionUtil.getFieldValue(account, "business_name");
                Double amountSpent = (Double) ReflectionUtil.getFieldValue(account, "amount_spent");
                accounts.add(new AdAccountList(id, name, accountStatus, currency, timezoneId, businessName, amountSpent));
            } catch (Exception e) {
                logger.error("Error processing account: {}", e.getMessage());
            }
        }
        logger.info("Successfully fetched {} accounts", accounts.size());
        return accounts;
    }
}