package com.uqac.wifi_attack.controller;

import com.uqac.wifi_attack.services.CommandExecutionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CommandExecutionController {

    // Utilisation dépendances
    private final CommandExecutionService commandExecutionService;

    /**
     * Constructeur
     * @param commandExecutionService
     */
    public CommandExecutionController(CommandExecutionService commandExecutionService) {
        this.commandExecutionService = commandExecutionService;
    }

    /**
     * Lance une attaque de déauthentification sur un réseau WiFi pour récupérer le mot de passe
     * @param essid Adresse MAC du point d'accès
     *              (ex: 00:11:22:33:44:55)
     */
    @RequestMapping("/attack")
    public String executeAttack(@RequestParam String essid) {
        String attackId = UUID.randomUUID().toString();
        commandExecutionService.executeAttack(essid, "src/main/resources/wordlist-top4800-probable.txt", attackId);
        return "Attaque lancée avec ID: " + attackId;
    }

    /**
     * Récupère le statut d'une attaque à partir de son ID
     * @param attackId ID de l'attaque
     * @return Statut de l'attaque
     */
    @GetMapping("/status/{attackId}")
    public String getAttackStatus(@RequestParam String attackId) {
        return commandExecutionService.getAttackStatus(attackId);
    }

}
