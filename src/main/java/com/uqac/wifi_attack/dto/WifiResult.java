package com.uqac.wifi_attack.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WifiResult {
    private Long reportId;
    private String logContent;
}
