package com.popobob.giftcard.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminStatsDto {
    private long totalGenerated;
    private long totalRedeemed;
    private List<AdminRecordDto> records;
}
