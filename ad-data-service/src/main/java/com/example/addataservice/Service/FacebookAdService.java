package com.example.addataservice.Service;

import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.AdAccount;
import com.facebook.ads.sdk.APIException;
import com.facebook.ads.sdk.APINodeList;
import com.facebook.ads.sdk.AdsInsights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FacebookAdService 是一个 Spring 服务类，用于从 Facebook 广告账户拉取广告洞察数据。
 * 该服务使用 facebook-java-business-sdk 与 Facebook Marketing API 交互。
 */
@Service
public class FacebookAdService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookAdService.class); // 日志记录器
    private final APIContext context; // API 上下文，用于初始化 SDK 客户端

    /**
     * 构造函数，初始化 API 上下文。
     * @param accessToken 从配置文件中注入的 Facebook 访问令牌
     */
    public FacebookAdService(@Value("${facebook.access-token}") String accessToken) {
        this.context = new APIContext(accessToken);
        logger.info("Initializing FacebookAdService with access token: [REDACTED]"); // 记录服务初始化日志
    }

    /**
     * 从指定广告账户拉取广告洞察数据。
     * @param accountId 广告账户 ID
     * @return 包含广告数据的列表，每个元素是一个 Map，映射中文字段名到值
     * @throws APIException 如果 API 调用失败，抛出异常
     */
    public List<Map<String, Object>> fetchAdData(String accountId) throws APIException {
        AdAccount account = new AdAccount(accountId, context); // 创建广告账户对象
        String[] fields = {
                "account_name", "campaign_name", "adset_name", "ad_name",
                "account_id", "campaign_id", "adset_id", "ad_id",
                "spend", "impressions", "reach", "frequency",
                "clicks", "cpc", "cpm", "ctr",
                "actions", "action_values", "cost_per_action_type",
                "date_start", "date_stop"
        }; // 定义要拉取的字段列表

        logger.info("Fetching ad data for account: act_{}", accountId); // 记录数据拉取开始
        APINodeList<AdsInsights> insights = account.getInsights()
                .setParam("level", "ad") // 设置洞察级别为广告级别
                .setParam("date_preset", "yesterday") // 设置时间范围为昨天
                .setParam("fields", String.join(",", fields)) // 设置要返回的字段
                .setParam("action_breakdowns", "action_type") // 设置行动分解
                .execute(); // 执行 API 调用

        logger.info("Successfully fetched {} insights records", insights.size()); // 记录拉取到的记录数

        List<Map<String, Object>> results = new ArrayList<>(); // 存储结果的列表
        for (AdsInsights insight : insights) {
            Map<String, Object> data = new HashMap<>(); // 每个洞察数据映射
            try {
                // 必须字段
                data.put("单日", getFieldValue(insight, "date_start"));
                data.put("帐户名称", getFieldValue(insight, "account_name"));
                data.put("广告系列名称", getFieldValue(insight, "campaign_name"));
                data.put("广告组名称", getFieldValue(insight, "adset_name"));
                data.put("广告名称", getFieldValue(insight, "ad_name"));
                data.put("账户编号", getFieldValue(insight, "account_id"));
                data.put("广告系列编号", getFieldValue(insight, "campaign_id"));
                data.put("广告组编号", getFieldValue(insight, "adset_id"));
                data.put("广告编号", getFieldValue(insight, "ad_id"));
                data.put("货币", "USD");
                data.put("报告开始日期", getFieldValue(insight, "date_start"));
                data.put("报告结束日期", getFieldValue(insight, "date_stop"));

                // 可选字段
                data.put("花费金额 (USD)", parseDouble(getFieldValue(insight, "spend")));
                data.put("展示次数", parseLong(getFieldValue(insight, "impressions")));
                data.put("覆盖人数", parseLong(getFieldValue(insight, "reach")));
                data.put("频次", parseDouble(getFieldValue(insight, "frequency")));
                data.put("链接点击量", parseLong(getActionValue(getFieldList(insight, "actions"), "link_click")));
                data.put("单次链接点击费用", parseDouble(getFieldValue(insight, "cpc")));
                data.put("点击量（全部）", parseLong(getFieldValue(insight, "clicks")));
                data.put("单次点击费用（全部）", parseDouble(getFieldValue(insight, "cpc")));
                data.put("千次展示费用", parseDouble(getFieldValue(insight, "cpm")));
                data.put("点击率（全部）", parseDouble(getFieldValue(insight, "ctr")));
                data.put("购物次数", parseLong(getActionValue(getFieldList(insight, "actions"), "purchase")));
                data.put("单次购物费用", parseDouble(getCostPerAction(getFieldList(insight, "cost_per_action_type"), "purchase")));
                data.put("购物转化价值", parseDouble(getActionValue(getFieldList(insight, "action_values"), "purchase")));
                data.put("完成注册次数", parseLong(getActionValue(getFieldList(insight, "actions"), "complete_registration")));
                data.put("单次完成注册费用", parseDouble(getCostPerAction(getFieldList(insight, "cost_per_action_type"), "complete_registration")));
                data.put("完成注册转化价值", parseDouble(getActionValue(getFieldList(insight, "action_values"), "complete_registration")));
                data.put("目标", "OUTCOME_SALES");
                data.put("浏览量", parseLong(getFieldValue(insight, "impressions")));
                data.put("应用安装", parseLong(getActionValue(getFieldList(insight, "actions"), "app_install")));
                data.put("单次应用安装费用", parseDouble(getCostPerAction(getFieldList(insight, "cost_per_action_type"), "app_install")));
                data.put("渠道包", null);

                // 获取广告创意中的 URL
                String adId = (String) getFieldValue(insight, "ad_id");
                String url = getAdUrl(adId);
                data.put("网址", url != null ? url : "");

                results.add(data);
            } catch (Exception e) {
//                logger.error("Error processing insight for adId {}: {}", getFieldValue(insight, "ad_id"), e.getMessage());
            }
        }
        logger.info("Processed {} ad records", results.size()); // 记录处理完成的数据条数
        return results;
    }

    /**
     * 获取指定字段的值，适用于不同类型。
     * @param insight AdsInsights 对象
     * @param fieldName 字段名
     * @return 字段值，若失败返回 null
     */
    private Object getFieldValue(AdsInsights insight, String fieldName) {
        try {
            // 尝试直接访问字段值，假设在 execute() 后已填充
            switch (fieldName) {
                case "date_start":
                case "date_stop":
                case "account_name":
                case "campaign_name":
                case "adset_name":
                case "ad_name":
                case "account_id":
                case "campaign_id":
                case "adset_id":
                case "ad_id":
                    return getField(insight, fieldName);
                case "spend":
                case "impressions":
                case "reach":
                case "frequency":
                case "clicks":
                case "cpc":
                case "cpm":
                case "ctr":
                    return getField(insight, fieldName);
                case "actions":
                case "action_values":
                case "cost_per_action_type":
                    return getField(insight, fieldName);
                default:
                    logger.warn("Unsupported field: {}", fieldName);
                    return null;
            }
        } catch (Exception e) {
            logger.warn("Failed to get field {}: {}", fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定字段的值，使用反射或动态访问。
     * @param insight AdsInsights 对象
     * @param fieldName 字段名
     * @return 字段值，若失败返回 null
     */
    private Object getField(AdsInsights insight, String fieldName) {
        try {
            // 假设字段值已在 execute() 后填充，通过反射访问
            return insight.getClass().getMethod("get" + capitalize(fieldName)).invoke(insight);
        } catch (Exception e) {
            try {
                // 回退到直接属性访问（可能需要 SDK 内部支持）
                return insight.getClass().getField(fieldName).get(insight);
            } catch (Exception ex) {
                logger.warn("Failed to access field {}: {}", fieldName, ex.getMessage());
                return null;
            }
        }
    }

    /**
     * 将字段名首字母大写，用于 getter 方法名。
     * @param str 字段名
     * @return 首字母大写的字符串
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 获取指定字段的列表值，适用于 actions, action_values 等。
     * @param insight AdsInsights 对象
     * @param fieldName 字段名
     * @return 解析后的列表，若失败返回空列表
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getFieldList(AdsInsights insight, String fieldName) {
        try {
            Object value = getFieldValue(insight, fieldName);
            return value instanceof List ? (List<Map<String, Object>>) value : new ArrayList<>();
        } catch (Exception e) {
            logger.warn("Failed to get list field {}: {}", fieldName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取指定广告的 URL。
     * @param adId 广告 ID
     * @return 广告 URL，若失败返回 null
     * @throws APIException 如果 API 调用失败
     */
    private String getAdUrl(String adId) {
//        try {

//            Ad ad1 = new Ad(adId, context).get()
//                    .requestField("creative")
//                    .execute();
//            String creativeId = (String) getField(ad1, "creative");
//            AdCreative creative = new AdCreative(creativeId, context).get()
//                    .requestField("url")
//                    .execute();
//            return (String) getField(creative, "url");
            return null;
//        } catch (APIException e) {
//            logger.error("Failed to fetch URL for adId {}: {}", adId, e.getMessage());
//            return null;
//        }
    }

    /**
     * 从动作列表中获取指定类型的动作值。
     * @param actions 动作列表
     * @param actionType 动作类型
     * @return 动作值，若未找到返回 null
     */
    private String getActionValue(List<Map<String, Object>> actions, String actionType) {
        if (actions == null || actions.isEmpty()) return null;
        for (Map<String, Object> action : actions) {
            if (actionType.equals(action.get("action_type"))) {
                return String.valueOf(action.get("value"));
            }
        }
        return null;
    }

    /**
     * 从成本每行动列表中获取指定类型的成本。
     * @param costPerActions 成本每行动列表
     * @param actionType 动作类型
     * @return 成本值，若未找到返回 null
     */
    private String getCostPerAction(List<Map<String, Object>> costPerActions, String actionType) {
        if (costPerActions == null || costPerActions.isEmpty()) return null;
        for (Map<String, Object> cost : costPerActions) {
            if (actionType.equals(cost.get("action_type"))) {
                return String.valueOf(cost.get("value"));
            }
        }
        return null;
    }

    /**
     * 将字符串解析为 Double 类型。
     * @param value 待解析的字符串
     * @return 解析后的 Double 值，若失败返回 null
     */
    private Double parseDouble(Object value) {
        try {
            return value != null ? Double.parseDouble(value.toString()) : null;
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse double value: {}", value);
            return null;
        }
    }

    /**
     * 将字符串解析为 Long 类型。
     * @param value 待解析的字符串
     * @return 解析后的 Long 值，若失败返回 null
     */
    private Long parseLong(Object value) {
        try {
            return value != null ? Long.parseLong(value.toString()) : null;
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse long value: {}", value);
            return null;
        }
    }
}