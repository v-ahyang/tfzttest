package com.example.datastorageservice;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class AdReport {
    @Id
    private String adId;
    private String campaignName;
    private Long clicks;
    private Double spend;
}