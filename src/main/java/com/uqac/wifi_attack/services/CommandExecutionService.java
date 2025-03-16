package com.uqac.wifi_attack.services;

import com.uqac.wifi_attack.dto.SubmitRequest;
import com.uqac.wifi_attack.dto.WifiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommandExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutionService.class);

    // Stocke l'état des scans (clé = scanId)
    private final Map<String, String> scanStatus = new ConcurrentHashMap<>();

    /**
     * Exécute une attaque WiFi en utilisant un script Bash
     *
     * @param request      Requête de soumission
     * @param wordlistPath Chemin vers le dictionnaire de mots de passe
     * @param scanId       Identifiant du scan
     */
    @Async
    public void executeAttack(SubmitRequest request, String wordlistPath, String scanId) {

        logger.info( "Executing wifi attack with ID: {}", scanId);

        StringBuilder logBuilder = new StringBuilder();
        scanStatus.put(scanId, "IN_PROGRESS");

        Path scriptPath = null;
        WifiResult result;
        try {
            // 🔹 Extraction du script s'il est intégré dans le JAR
            scriptPath = extractScriptIfNeeded("bashAttack.sh");
            logger.info(scriptPath.toString());
            logger.info(request.getOption());
            logger.info(wordlistPath);

            // 🔹 Vérification des arguments
            if (request.getOption() == null || request.getOption().isEmpty() || wordlistPath == null || wordlistPath.isEmpty()) {
                throw new IllegalArgumentException("ESSID and dictionary path must not be null or empty");
            }

            // 🔹 Construction sécurisée de la commande
            List<String> command = Arrays.asList("script", "-q", "-c", "sudo bash " + scriptPath.toString() + " \"" + request.getOption() + "\" " + wordlistPath);
            logger.info(command.toString());
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("COLUMNS", "80");
            processBuilder.environment().put("LINES", "24");
            processBuilder.redirectErrorStream(true);

            // 🔹 Exécution du processus
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logBuilder.append(line).append("\n");
                    logger.info(line);
                }
            }

            // 🔹 Attendre la fin du processus et enregistrer le statut
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                scanStatus.put(scanId, "SUCCESS");
                logger.info("Attaque sans erreur");
            } else {
                logger.error("Erreur lors de l'exécution de l'attaque");
                scanStatus.put(scanId, "FAILED");
            }

        } catch (HttpClientErrorException e) {
            logger.error("HTTP error occurred while sending the result: {}", e.getStatusCode());
            logger.error("Response body: {}", e.getResponseBodyAsString());
            scanStatus.put(scanId, "ERROR");
        } catch (Exception e) {
            logger.error("Erreur lors de l'exécution de l'attaque", e);
            scanStatus.put(scanId, "ERROR");
        } finally {
            // 🔹 Suppression du script après utilisation
            if (scriptPath != null) {
                try {
                    Files.deleteIfExists(scriptPath);
                    logger.info("Script {} supprimé avec succès", scriptPath.toString());
                } catch (IOException e) {
                    logger.error("Erreur lors de la suppression du script {}", scriptPath.toString(), e);
                }
            }

            // 🔹 Création de l'objet WifiResult
            result = WifiResult.builder()
                    .reportId(request.getReportId())
                    .logContent(logBuilder.toString())
                    .build();

            // 🔹 Envoi du résultat au service externe
            RestTemplate restTemplate = new RestTemplate();
            String externalServiceUrl = "http://localhost:8090/report/bffwifi";
            try {
                // On attends 10 secondes pour que le service externe soit prêt
                Thread.sleep(20000);
                logger.info("Envoi du résultat à l'adresse {}", externalServiceUrl);
                restTemplate.postForObject(externalServiceUrl, result, Void.class);
            } catch (ResourceAccessException e) {
                logger.error("Resource access error while posting scan result: {}", e.getMessage());
                scanStatus.put(scanId, "ERROR: Resource access error while posting scan result");
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi du résultat", e);
            }
        }

        String logs = logBuilder.toString();
        logger.info("Logs de l'attaque [{}]:\n{}", scanId, logs);
    }

    /**
     * Vérifie si le script Bash existe, sinon l'extrait du JAR vers /tmp
     *
     * @param scriptName Nom du script à extraire
     * @return Chemin du script extrait
     * @throws IOException Si une erreur d'accès se produit
     */
    private Path extractScriptIfNeeded(String scriptName) throws IOException {
        Path tempScript = Paths.get("/tmp/" + scriptName);

        logger.info("Extraction du script {} depuis le JAR... a l'emplacement {}", scriptName, tempScript.toString());

        if (!Files.exists(tempScript)) {
            logger.info("Extraction du script {} depuis le JAR...", scriptName);
            try (InputStream in = getClass().getResourceAsStream("/" + scriptName);
                 OutputStream out = Files.newOutputStream(tempScript)) {
                if (in == null) throw new IOException("Script non trouvé dans le JAR !");

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            tempScript.toFile().setExecutable(true);
        }

        return tempScript;
    }

    /**
     * Récupère l'état d'un scan donné
     *
     * @param scanId Identifiant du scan
     * @return État du scan (IN_PROGRESS, SUCCESS, FAILED, ERROR, UNKNOWN_SCAN_ID)
     */
    public String getAttackStatus(String scanId) {
        return scanStatus.getOrDefault(scanId, "UNKNOWN_SCAN_ID");
    }

}
