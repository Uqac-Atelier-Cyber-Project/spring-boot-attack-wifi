package com.uqac.wifi_attack.services;

import com.uqac.wifi_attack.dto.ApiProperties;
import com.uqac.wifi_attack.dto.SubmitRequest;
import com.uqac.wifi_attack.dto.WifiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommandExecutionService {

    @Autowired
    private ApiProperties apiProperties;

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutionService.class);

    // Stocke l'état des scans (clé = scanId)
    private final Map<String, String> scanStatus = new ConcurrentHashMap<>();

    /**
     * Exécute une attaque WiFi en utilisant un script Bash
     *
     * @param request      Requête de soumission
     * @param scanId       Identifiant du scan
     */
    @Async
    public void executeAttack(SubmitRequest request, String scanId) {
        logger.info("Executing wifi attack with ID: {}", scanId);

        StringBuilder logBuilder = new StringBuilder();
        scanStatus.put(scanId, "IN_PROGRESS");

        Path scriptPath = null;
        Path wordlistPath = null;
        WifiResult result;

        try {
            scriptPath = extractResourceIfNeeded("bashAttack.sh");
            wordlistPath = extractResourceIfNeeded("wordlist-top4800-probable.txt");

            logger.info("Script path: {}", scriptPath);
            logger.info("Wordlist path: {}", wordlistPath);
            logger.info("Target ESSID: {}", request.getOption());

            if (request.getOption() == null || request.getOption().isEmpty()) {
                throw new IllegalArgumentException("ESSID must not be null or empty");
            }

            List<String> command = Arrays.asList(
                    "script", "-q", "-c",
                    "sudo bash " + scriptPath + " \"" + request.getOption() + "\" " + wordlistPath
            );
            logger.info("Command: {}", command);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("COLUMNS", "80");
            processBuilder.environment().put("LINES", "24");
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                Pattern ansiPattern = Pattern.compile("\\u001B\\[[;\\d]*m");
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = ansiPattern.matcher(line);
                    line = matcher.replaceAll(""); // Supprime les séquences ANSI
                    logBuilder.append(line).append("\n");
                    logger.info(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                scanStatus.put(scanId, "SUCCESS");
                logger.info("Attaque terminée avec succès");
            } else {
                scanStatus.put(scanId, "FAILED");
                logger.error("Erreur lors de l'exécution de l'attaque (code {})", exitCode);
                result = createErrorResult(logBuilder.toString(), String.valueOf(request.getReportId()));
                sendResult(result);
                return;
            }

        } catch (Exception e) {
            logger.error("Exception pendant l'exécution de l'attaque", e);
            scanStatus.put(scanId, "ERROR");
            result = createErrorResult(logBuilder.toString(), String.valueOf(request.getReportId()));
            sendResult(result);
            return;
        } finally {
            deleteIfExists(scriptPath);
            deleteIfExists(wordlistPath);

            result = LogParser.parseLog(logBuilder.toString(), request.getReportId());

            logger.info("Result: {}", result);

            sendResult(result);
        }

        logger.debug("Logs de l'attaque [{}]:\n{}", scanId, logBuilder.toString());
    }

    private WifiResult createErrorResult(String logContent, String reportId) {
        return WifiResult.builder()
                .reportId(Long.valueOf(reportId))
                .logContent("L'attaque ne s'est pas bien déroulée :\n" + logContent)
                .build();
    }

    private void sendResult(WifiResult result) {
        String externalServiceUrl = apiProperties.getUrl() + "/report/bffwifi";
        try {
            Thread.sleep(20000);
            logger.info("Envoi du résultat à l'adresse {}", externalServiceUrl);
            new RestTemplate().postForObject(externalServiceUrl, result, Void.class);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du résultat", e);
        }
    }

    /**
     * Extrait une ressource du JAR si elle n'existe pas déjà
     * @param resourceName Nom de la ressource à extraire
     * @return Le chemin de la ressource extraite
     * @throws IOException Erreur d'entrée/sortie
     */
    private Path extractResourceIfNeeded(String resourceName) throws IOException {
        Path tempPath = Paths.get("/tmp", resourceName);

        if (!Files.exists(tempPath)) {
            try (InputStream in = getClass().getResourceAsStream("/" + resourceName);
                 OutputStream out = Files.newOutputStream(tempPath)) {
                if (in == null) {
                    throw new IOException("Ressource " + resourceName + " non trouvée dans le JAR !");
                }

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            if (resourceName.endsWith(".sh")) {
                tempPath.toFile().setExecutable(true);
            }
        }

        return tempPath;
    }

    /**
     * Supprime un fichier temporaire s'il existe
     * @param path Chemin du fichier à supprimer
     */
    private void deleteIfExists(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
                logger.info("Fichier temporaire supprimé : {}", path);
            } catch (IOException e) {
                logger.warn("Impossible de supprimer le fichier temporaire : {}", path, e);
            }
        }
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
