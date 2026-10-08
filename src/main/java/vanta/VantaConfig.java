package vanta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class VantaConfig {
    // HUD
    public boolean enabled = true;
    public boolean showFps = true;
    public boolean showCps = true;
    public boolean showPing = true;
    public boolean showCombo = true;
    public boolean showReach = true;
    public boolean showCoords = true;
    public boolean showDirection = true;
    public boolean showSpeed = false;
    public boolean showTime = false;
    public boolean showMemory = false;
    public boolean showIp = false;
    public boolean showArrows = false;
    public boolean showKeystrokes = true;
    public boolean showArmor = true;
    public boolean showPotions = true;
    public boolean targetHud = true;
    public boolean background = true;

    // Gorsel
    public boolean noHurtShake = true;
    public boolean lowFire = true;
    public boolean visualDay = false;
    public boolean clearWeather = false;
    public boolean hitParticles = true;
    public int hitParticleType = 0;
    public int hitParticleCount = 8;
    public boolean deathEffect = true;
    public boolean trails = true;
    public boolean showHitboxes = false;
    public boolean crosshairIndicator = true;
    /** 0 mor, 1 mavi, 2 kirmizi, 3 yesil, 4 rgb */
    public int theme = 0;

    // Genel
    public boolean autoSprint = false;
    public boolean autoBoost = true;
    public boolean boostApplied = false;
    /** 0 sol ust, 1 sag ust, 2 sol alt, 3 sag alt */
    public int corner = 0;
    public float scale = 1.0f;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static VantaConfig instance = new VantaConfig();

    public static VantaConfig get() {
        return instance;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("vanta.json");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) {
            save();
            return;
        }
        try (Reader r = Files.newBufferedReader(p)) {
            VantaConfig c = GSON.fromJson(r, VantaConfig.class);
            if (c != null) {
                if (c.scale < 0.5f || c.scale > 2f) c.scale = 1.0f;
                if (c.corner < 0 || c.corner > 3) c.corner = 0;
                if (c.theme < 0 || c.theme > 4) c.theme = 0;
                if (c.hitParticleType < 0 || c.hitParticleType >= Visuals.PARTICLE_NAMES.length) c.hitParticleType = 0;
                if (c.hitParticleCount < 1 || c.hitParticleCount > 64) c.hitParticleCount = 8;
                instance = c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        save();
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(path())) {
            GSON.toJson(instance, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
