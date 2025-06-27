package com.example.addataservice;

import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.AdAccount;
import com.facebook.ads.sdk.Ad;
import com.facebook.ads.sdk.APIException;
import com.facebook.ads.sdk.APINodeList;
import com.facebook.ads.sdk.AdsInsights;
import com.facebook.ads.sdk.AdCreative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FacebookAdService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookAdService.class);
    private final APIContext context;

    public FacebookAdService(@Value("${facebook.access-token}") String accessToken) {
        this.context = new APIContext(accessToken).setDebug(true);
        logger.info("Initializing FacebookAdService with access token: [REDACTED]");
    }

    public List<Map<String, Object>> fetchAdData(String accountId) throws APIException {
        AdAccount account = new AdAccount("act_" + accountId, context);
        String[] fields = {
                "account_name", "campaign_name", "adset_name", "ad_name",
                "account_id", "campaign_id", "adset_id", "ad_id",
                "spend", "impressions", "reach", "frequency",
                "clicks", "cpc", "cpm", "ctr",
                "actions", "action_values", "cost_per_action_type",
                "date_start", "date_stop"
        };

        logger.info("Fetching ad data for account: act_{}", accountId);
        APINodeList<AdsInsights> insights = account.getInsights()
                .setParam("level", "ad")
                .setParam("date_preset", "yesterday")
                .setParam("fields", String.join(",", fields))
                .setParam("action_breakdowns", "action_type")
                .execute();

        logger.info("Successfully fetched {} insights records", insights.size());

        List<Map<String, Object>> results = new ArrayList<>();
        for (AdsInsights insight : insights) {
            Map<String, Object> data = new HashMap<>();
            try {
                // 必须字段
                data.put("单日", insight.getFieldDateStart());
                data.put("帐户名称", insight.getFieldAccountName());
                data.put("广告系列名称", insight.getFieldCampaignName());
                data.put("广告组名称", insight.getFieldAdsetName());
                data.put("广告名称", insight.getFieldAdName());
                data.put("账户编号", insight.getFieldAccountId());
                data.put("广告系列编号", insight.getFieldCampaignId());
                data.put("广告组编号", insight.getFieldAdsetId());
                data.put("广告编号", insight.getFieldAdId());
                data.put("货币", "USD");
                data.put("报告开始日期", insight.getFieldDateStart());
                data.put("报告结束日期", insight.getFieldDateStop());

                // 可选字段
                data.put("花费金额 (USD)", parseDouble(insight.getFieldSpend()));
                data.put("展示次数", parseLong(insight.getFieldImpressions()));
                data.put("覆盖人数", parseLong(insight.getFieldReach()));
                data.put("频次", parseDouble(insight.getFieldFrequency()));
                data.put("链接点击量", parseLong(getActionValue(insight.getFieldActions(), "link_click")));
                data.put("单次链接点击费用", parseDouble(insight.getFieldCpc()));
                data.put("点击量（全部）", parseLong(insight.getFieldClicks()));
                data.put("单次点击费用（全部）", parseDouble(insight.getFieldCpc()));
                data.put("千次展示费用", parseDouble(insight.getFieldCpm()));
                data.put("点击率（全部）", parseDouble(insight.getFieldCtr()));
                data.put("购物次数", parseLong(getActionValue(insight.getFieldActions(), "purchase")));
                data.put("单次购物费用", parseDouble(getCostPerAction(insight.getFieldCostPerActionType(), "purchase")));
                data.put("购物转化价值", parseDouble(getActionValue(insight.getFieldActionValues(), "purchase")));
                data.put("完成注册次数", parseLong(getActionValue(insight.getFieldActions(), "complete_registration")));
                data.put("单次完成注册费用", parseDouble(getCostPerAction(insight.getFieldCostPerActionType(), "complete_registration")));
                data.put("完成注册转化价值", parseDouble(getActionValue(insight.getFieldActionValues(), "complete_registration")));
                data.put("目标", "OUTCOME_SALES");
                data.put("浏览量", parseLong(insight.getFieldImpressions()));
                data.put("应用安装", parseLong(getActionValue(insight.getFieldActions(), "app_install")));
                data.put("单次应用安装费用", parseDouble(getCostPerAction(insight.getFieldCostPerActionType(), "app_install")));
                data.put("渠道包", null);

                // 获取广告创意中的 URL
                String adId = insight.getFieldAdId();
                String url = getAdUrl(adId);
                data.put("网址", url != null ? url : "");

                results.add(data);
            } catch (Exception e) {
                logger.error("Error processing insight for adId {}: {}", insight.getFieldAdId(), e.getMessage());
            }
        }
        logger.info("Processed {} ad records", results.size());
        return results;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getFieldActions(List<Object> actions) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (actions != null) {
            for (Object action : actions) {
                if (action instanceof Map) {
                    result.add((Map<String, Object>) action);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getFieldActionValues(List<Object> actionValues) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (actionValues != null) {
            for (Object value : actionValues) {
                if (value instanceof Map) {
                    result.add((Map<String, Object>) value);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getFieldCostPerActionType(List<Object> costPerActions) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (costPerActions != null) {
            for (Object cost : costPerActions) {
                if (cost instanceof Map) {
                    result.add((Map<String, Object>) cost);
                }
            }
        }
        return result;
    }

    private String getActionValue(List<Map<String, Object>> actions, String actionType) {
        if (actions == null || actions.isEmpty()) return null;
        for (Map<String, Object> action : actions) {
            if (actionType.equals(action.get("action_type"))) {
                return String.valueOf(action.get("value"));
            }
        }
        return null;
    }

    private String getCostPerAction(List<Map<String, Object>> costPerActions, String actionType) {
        if (costPerActions == null || costPerActions.isEmpty()) return null;
        for (Map<String, Object> cost : costPerActions) {
            if (actionType.equals(cost.get("action_type"))) {
                return String.valueOf(cost.get("value"));
            }
        }
        return null;
    }

    private Double parseDouble(String value) {
        try {
            return value != null ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse double value: {}", value);
            return null;
        }
    }

    private Long parseLong(String value) {
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse long value: {}", value);
            return null;
        }
    }

    private String getAdUrl(String adId) {
        try {
            Ad ad = new Ad(adId, context).get()
                    .requestField("creative")
                    .execute();
            String creativeId = ad.getFieldCreative().getFieldId();
            AdCreative creative = new AdCreative(creativeId, context).get()
                    .requestField("url")
                    .execute();
            return creative.getFieldUrl();
        } catch (APIException e) {
            logger.error("Failed to fetch URL for adId {}: {}", adId, e.getMessage());
            return null;
        }
    }
}