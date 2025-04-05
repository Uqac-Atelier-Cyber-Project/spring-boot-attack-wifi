package com.uqac.wifi_attack.services;

import com.uqac.wifi_attack.dto.WifiResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {

    public static WifiResult parseLog(String logContent, Long reportId) {
        String accessPointName = null;
        String accessPointBSSID = null;
        String encryption = null;
        String extraInformation = null;
        String psk = null;

        String[] lines = logContent.split("\n");
        for (String line : lines) {
            if (line.contains("ESSID:")) {
                accessPointName = line.split(":")[1].trim();
            } else if (line.contains("Access Point Name:")) {
                accessPointName = line.split(":")[1].trim();
            } else if (line.contains("Access Point BSSID:")) {
                Pattern pattern = Pattern.compile("([0-9A-Fa-f]{2}(:[0-9A-Fa-f]{2}){5})");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    accessPointBSSID = matcher.group(1);
                }
            } else if (line.contains("BSSID:")) {
                Pattern pattern = Pattern.compile("([0-9A-Fa-f]{2}(:[0-9A-Fa-f]{2}){5})");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    accessPointBSSID = matcher.group(1);
                }
            } else if (line.contains("Encryption:")) {
                encryption = line.split(":")[1].trim();
            } else if (line.contains("WPS PIN:")) {
                extraInformation = line.split(":")[1].trim();
            } else if (line.contains("Handshake File:")) {
                extraInformation = line.split(":")[1].trim();
            } else if (line.contains("PSK/Password:")) {
                psk = line.split(":")[1].trim();
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
                .extraInformation(extraInformation)
                .psk(psk)
                .build();
    }
}