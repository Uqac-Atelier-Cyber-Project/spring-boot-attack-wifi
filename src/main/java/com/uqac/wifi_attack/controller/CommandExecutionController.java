package com.uqac.wifi_attack.controller;

import com.uqac.wifi_attack.dto.SubmitRequest;
import com.uqac.wifi_attack.services.CommandExecutionService;
import org.springframework.web.bind.annotation.*;

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
     * @param request Requête de soumission
     *
     */
    @RequestMapping("/attack")
    public String executeAttack(@RequestBody SubmitRequest request) {
        String attackId = UUID.randomUUID().toString();
        commandExecutionService.executeAttack(request, "src/main/resources/wordlist-top4800-probable.txt", attackId);
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
