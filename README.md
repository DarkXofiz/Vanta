# Vanta

Fabric 1.20.1 istemci modu. PvP HUD, görsel efektler, hasar sarsıntısı kaldırma, FPS artırıcı.

- Menü: Sağ Shift (HUD / Hedef / Görsel / Ayarlar sekmeleri)
- Zoom: C (basılı tut)
- HUD ve hedef kartı sürükleyerek taşınır: Ayarlar > HUD Düzenle
- HUD aç/kapat: H
- Ayarlar: config/vanta.json

## HUD
FPS, CPS, ping, combo, reach, K/D, koordinat, yön, hız, saat, oyun süresi, RAM, IP, ok sayısı, tuşlar, zırh, efektler, nişan göstergesi.

## Hedef HUD
Oyuncu yüzü, isim, animasyonlu can çubuğu, can sayısı, zırh puanı, mesafe, ping, önde/geride durumu, hedefin zırhı ve elindeki eşyalar. Her öğe ayrı ayrı açılıp kapanır, boyut ve konum ayarlanır.

## Görsel
Tema (RGB dahil), sarsıntı kaldırma, düşük ateş, hep gündüz, yağmursuz, vuruş efekti, ölüm efekti, ender pearl ve ok izi, hitbox.

## Derleme
Gereksinim: Java 17. Gradle wrapper projede hazır (8.12).

- Yerelde: `./gradlew build`, çıktı `build/libs/vanta-1.2.0.jar`
- GitHub: repoya yükle, Actions sekmesinde `Vanta` artifact'ını indir.
  `v1.2.0` gibi bir tag atarsan jar otomatik Release'e eklenir.

`gradle/wrapper/gradle-wrapper.jar` repoda olmalı, .gitignore bunu hariç tutmaz.
