#!/bin/bash

# Afficher la valeur de $#
#echo "Nombre de paramètres: $#"
#
## afficher les paramètres
#for i in "$@"
#do
#    echo "Paramètre: $i"
#done

# Vérifier si le nombre de paramètres est correct
if [ "$#" -ne 2 ]; then

    echo "Usage: $0 <essid> <dictionary_path>"
    exit 1
fi

ESSID=$1
DICTIONARY_PATH=$2

# Fonction pour désactiver le mode moniteur et réactiver le Wi-Fi
cleanup() {
    echo "Désactivation du mode moniteur et réactivation du Wi-Fi..."
    sudo airmon-ng stop wlp110s0f0mon 2>/dev/null
    sudo service NetworkManager restart 2>/dev/null
    sudo rm cracked.json 2>/dev/null
    exit 0
}

# Capturer le signal CTRL+C (SIGINT)
trap cleanup SIGINT

# Fonction pour exécuter wifite et vérifier le log
run_wifite() {
    timeout 300 sudo python3 ./wifite2/wifite.py --kill --essid "$ESSID" --dict "$DICTIONARY_PATH" --all | tee wifite.log
    EXIT_CODE=$?

    # Vérifier si Wifite a échoué
    if [ $EXIT_CODE -ne 0 ]; then
        echo "Wifite a échoué, réactivation du Wi-Fi..."
    fi

    # Vérifier le log pour la présence de "PSK/Password: N/A"
    if grep -Fq "[0m[2m[[0m[32m+[0m[2m][0m PSK/Password: [32m[33mN/A[0m[0m" wifite.log; then
        echo "PSK/Password: N/A trouvé dans le log, relance de Wifite..."
        sleep 10 #
        run_wifite
    fi
}

# Si wifite2 n'existe pas, cloner le dépôt
if [ ! -d "wifite2" ]; then
    git clone https://github.com/kimocoder/wifite2.git 2>/dev/null
    # build wifite2
    # shellcheck disable=SC2164
    cd wifite2 2>/dev/null
    python3 -m venv venv 2>/dev/null
    source venv/bin/activate 2>/dev/null
    pip install -r requirements.txt 2>/dev/null
    # shellcheck disable=SC2103
    cd .. 2>/dev/null
fi


# Exécuter wifite
run_wifite

# Désactiver le mode moniteur et réactiver le Wi-Fi
cleanup