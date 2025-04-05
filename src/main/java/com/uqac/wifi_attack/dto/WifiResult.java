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
    private String accessPointName;
    private String accessPointBSSID;
    private String encryption;
    private String extraInformation;
    private String psk;

    @Override
    public String toString() {
        return "WifiResult{" +
                "reportId=" + reportId +
                ", logContent='" + logContent + '\'' +
                ", accessPointName='" + accessPointName + '\'' +
                ", accessPointBSSID='" + accessPointBSSID + '\'' +
                ", encryption='" + encryption + '\'' +
                ", extraInformation='" + extraInformation + '\'' +
                ", psk='" + psk + '\'' +
                '}';
    }
}
