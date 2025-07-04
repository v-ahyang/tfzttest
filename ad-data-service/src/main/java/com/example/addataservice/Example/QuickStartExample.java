package com.example.addataservice.Example;

import com.facebook.ads.sdk.*;
import com.facebook.ads.sdk.AdAccount.*;

import java.awt.*;
import java.util.Arrays;
import java.util.List;


public class QuickStartExample {
//54
//    public static final String ACCESS_TOKEN = "EAAekq6c2E0cBOzHbIy27erszzx2lLoibmZBZBCFtw8ThGhL4mAeJa5TlGomghAGx8fLn1kyjGv6Gek652VQdSwOJ4pveMDjQF14JA1W3TBByvzAM45EnZAcScrLTr6dQiLtuzYUMq6r1bdCFJNwh77wZCPFjSmr5hZB6nkvYZCGyEMxOFgozJu4kl7fjRi4SjJVCqJRCsZA";
//    public static final Long ACCOUNT_ID = Long.valueOf("1771075083758285");
    //app-id: 2151381988676423
//    public static final String APP_SECRET = "e493b4df857bf0796b49a312c63f4ac2";


//    15
    public static final String ACCESS_TOKEN = "EAAURlsCmW9IBPGVu0hDYRZB1SCZCP8F2KEdoFg7ZB2iLoH2IIqEreZBLUaL0qI0bRVKyj0geC1CeDZC3AfyrHWPX1kq4ZBTZBBTFm2fBzaSvg67b9zGSDxPxeFT7NAV3vIn8uxl42KK9WXdn7K01vpO0k2oaNg5pdcZASOy40pqypebbRG9v3CvAY93ZAaxJuM2qSa7CcXDLH";
    public static final Long ACCOUNT_ID = Long.valueOf("1995361727660581");

    public static final String APP_SECRET = "ed9299bca204b2722bebfc27c9b79aa8";

    public static final APIContext context = new APIContext(ACCESS_TOKEN, APP_SECRET).enableDebug(true).setLogger(System.out);
    public static void main(String[] args) {
        try {
            AdAccount account = new AdAccount(ACCOUNT_ID, context);
            // 调用getCampaigns()获取广告活动列表
            // 指定需要返回的字段
            List<String> Fields= Arrays.asList("id", "name", "objective", "status") ;
            APIRequest<Campaign> campaigns = account.getCampaigns()
                    .setParam("fields", "id,name,objective,status")
                    .requestFields(Fields);

            // 执行请求并获取结果
            APINodeList<Campaign> campaignList = (APINodeList<Campaign>) campaigns.execute();

            // 打印广告活动信息
            System.out.println("=== 广告活动列表 ===");
            for (Campaign campaign : campaignList) {
                System.out.println("ID: " + campaign.getFieldId());
                System.out.println("名称: " + campaign.getFieldName());
                System.out.println("目标: " + campaign.getFieldObjective());
                System.out.println("状态: " + campaign.getFieldStatus());
                System.out.println("-------------------");
            }
        } catch (APIException e) {
            System.err.println("API错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}