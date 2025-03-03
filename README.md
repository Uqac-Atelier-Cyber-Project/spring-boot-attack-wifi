# spring-boot-attack-wifi

## Description

This project is a simple spring boot application that allows to attack a wifi network using the `aircrack-ng` and `wifite` tool.

## Requirements

- Java 21
- Maven
-  Wifite
    * As we moved from older python and changed to fully support and run on `python3.11`
    * [`Iw`](https://wireless.wiki.kernel.org/en/users/documentation/iw): For identifying wireless devices already in Monitor Mode.
    * [`Ip`](https://packages.debian.org/buster/net-tools): For starting/stopping wireless devices.
    * [`Aircrack-ng`](https://aircrack-ng.org/) suite, includes:
        * [`airmon-ng`](https://tools.kali.org/wireless-attacks/airmon-ng): For enumerating and enabling Monitor Mode on wireless devices.
        * [`aircrack-ng`](https://tools.kali.org/wireless-attacks/aircrack-ng): For cracking WEP .cap files and WPA handshake captures.
        * [`aireplay-ng`](https://tools.kali.org/wireless-attacks/aireplay-ng): For deauthing access points, replaying capture files, various WEP attacks.
        * [`airodump-ng`](https://tools.kali.org/wireless-attacks/airodump-ng): For target scanning & capture file generation.
        * [`packetforge-ng`](https://tools.kali.org/wireless-attacks/packetforge-ng): For forging capture files.
   * [`tshark`](https://www.wireshark.org/docs/man-pages/tshark.html): For detecting WPS networks and inspecting handshake capture files.
   * [`reaver`](https://github.com/t6x/reaver-wps-fork-t6x): For WPS Pixie-Dust & brute-force attacks.
     * Note: Reaver's `wash` tool can be used to detect WPS networks if `tshark` is not found.
   * [`bully`](https://github.com/aanarchyy/bully): For WPS Pixie-Dust & brute-force attacks.
     * Alternative to Reaver. Specify `--bully` to use Bully instead of Reaver.
     * Bully is also used to fetch PSK if `reaver` cannot after cracking WPS PIN.
   * [`john`](https://www.openwall.com/john): For CPU (OpenCL)/GPU cracking passwords fast.
   * [`coWPAtty`](https://tools.kali.org/wireless-attacks/cowpatty): For detecting handshake captures.
   * [`hashcat`](https://hashcat.net/): For cracking PMKID hashes.
     * [`hcxdumptool`](https://github.com/ZerBea/hcxdumptool): For capturing PMKID hashes.
     * [`hcxpcapngtool`](https://github.com/ZerBea/hcxtools): For converting PMKID packet captures into `hashcat`'s format.