package com.uqac.wifi_attack.services;

import com.uqac.wifi_attack.dto.WifiResult;

public class LogParser {

    public static WifiResult parseLog(String logContent, Long reportId) {
        String accessPointName = null;
        String accessPointBSSID = null;
        String encryption = null;
        String handshakeFile = null;
        String psk = null;

        String[] lines = logContent.split("\n");
        for (String line : lines) {
            if (line.contains("Access Point Name:")) {
                accessPointName = line.split(":")[1].trim();
            } else if (line.contains("Access Point BSSID:")) {
                accessPointBSSID = line.split(":")[1].trim();
            } else if (line.contains("Encryption:")) {
                encryption = line.split(":")[1].trim();
            } else if (line.contains("Handshake File:")) {
                handshakeFile = line.split(":")[1].trim();
            } else if (line.contains("PSK (password):")) {
                psk = line.split(":")[1].trim();
            }
        }

        return WifiResult.builder()
                .reportId(reportId)
                .logContent(logContent)
                .accessPointName(accessPointName)
                .accessPointBSSID(accessPointBSSID)
                .encryption(encryption)
                .handshakeFile(handshakeFile)
                .psk(psk)
                .build();
    }
}