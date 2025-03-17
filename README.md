# spring-boot-attack-wifi

## Description
Ce projet est une application Spring Boot permettant d'exécuter des attaques WiFi en utilisant des scripts Bash. L'application permet de soumettre des requêtes d'attaque, de suivre leur statut et d'envoyer les résultats à un service externe.

## Prérequis
- Java 21 ou supérieur
- Maven 3.6.0 ou supérieur
- Un environnement Linux avec les permissions nécessaires pour exécuter des scripts Bash

## Installation
1. Clonez le dépôt :
    ```sh
    git clone https://github.com/Uqac-Atelier-Cyber-Project/spring-boot-attack-wifi.git
    cd spring-boot-attack-wifi
    ```

2. Compilez le projet avec Maven :
    ```sh
    mvn clean install
    ```

## Utilisation
1. Démarrez l'application Spring Boot :
    ```sh
    mvn spring-boot:run
    ```

2. Soumettez une attaque WiFi en envoyant une requête POST à l'endpoint `/execute` avec les paramètres nécessaires.

3. Suivez le statut de l'attaque en envoyant une requête GET à l'endpoint `/status/{attackId}`.

## Endpoints
- **POST /execute** : Soumet une nouvelle attaque WiFi.
- **GET /status/{attackId}** : Récupère le statut d'une attaque à partir de son ID.

## Exemple de requête
### Soumettre une attaque
```sh
curl -X POST http://localhost:8080/execute -H "Content-Type: application/json" -d '{
  "option": "votre_option",
  "reportId": "votre_report_id"
}'