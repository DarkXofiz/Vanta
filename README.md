# Vanta

Fabric 1.20.1 istemci modu. PvP HUD, görsel efektler, hasar sarsıntısı kaldırma, FPS artırıcı.

- Menü: Sağ Shift (HUD / Görsel / Ayarlar sekmeleri)
- HUD aç/kapat: H
- Ayarlar: config/vanta.json

## HUD
FPS, CPS, ping, combo, reach, koordinat, yön, hız, saat, RAM, IP, ok sayısı, tuşlar, zırh, efektler, hedef HUD, nişan göstergesi.

## Görsel
Tema (RGB dahil), sarsıntı kaldırma, düşük ateş, hep gündüz, yağmursuz, vuruş efekti, ölüm efekti, ender pearl ve ok izi, hitbox.

## Derleme
Gereksinim: Java 17. Gradle wrapper projede hazır (8.8).

- Yerelde: `./gradlew build`, çıktı `build/libs/vanta-1.1.0.jar`
- GitHub: repoya yükle, Actions sekmesinde `Vanta` artifact'ını indir.
  `v1.1.0` gibi bir tag atarsan jar otomatik Release'e eklenir.

`gradle/wrapper/gradle-wrapper.jar` repoda olmalı, .gitignore bunu hariç tutmaz.
