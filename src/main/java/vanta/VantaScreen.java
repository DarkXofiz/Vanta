package vanta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class VantaScreen extends Screen {
    private static final String[] CORNERS = {"Sol Üst", "Sağ Üst", "Sol Alt", "Sağ Alt"};
    private static final float[] SCALES = {0.7f, 0.85f, 1.0f, 1.25f, 1.5f};
    private static final String[] TABS = {"HUD", "Görsel", "Ayarlar"};
    private static int tab = 0;

    private final Screen parent;

    private interface Maker {
        ButtonWidget make(int x, int y, int w, int h);
    }

    public VantaScreen(Screen parent) {
        super(Text.literal("Vanta"));
        this.parent = parent;
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "\u00a7aAÇIK" : "\u00a7cKAPALI"));
    }

    private Maker toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
        return (x, y, w, h) -> ButtonWidget.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            VantaConfig.save();
            b.setMessage(label(name, get.getAsBoolean()));
        }).dimensions(x, y, w, h).build();
    }

    private Maker cycle(String name, Supplier<String> value, Runnable next) {
        return (x, y, w, h) -> ButtonWidget.builder(Text.literal(name + ": " + value.get()), b -> {
            next.run();
            VantaConfig.save();
            b.setMessage(Text.literal(name + ": " + value.get()));
        }).dimensions(x, y, w, h).build();
    }

    private static int scaleIndex(float s) {
        int best = 0;
        for (int i = 1; i < SCALES.length; i++) {
            if (Math.abs(SCALES[i] - s) < Math.abs(SCALES[best] - s)) best = i;
        }
        return best;
    }

    private static int countIndex(int n) {
        for (int i = 0; i < Visuals.COUNTS.length; i++) if (Visuals.COUNTS[i] == n) return i;
        return 1;
    }

    @Override
    protected void init() {
        VantaConfig c = VantaConfig.get();

        int tw = Math.max(40, Math.min(100, (width - 16) / 3 - 4));
        int tx0 = (width - (3 * tw + 8)) / 2;
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(
                    Text.literal((i == tab ? "\u00a7e" : "") + TABS[i]), b -> {
                        tab = idx;
                        clearAndInit();
                    }).dimensions(tx0 + i * (tw + 4), 20, tw, 16).build());
        }

        List<Maker> m = new ArrayList<>();
        if (tab == 0) {
            m.add(toggle("HUD", () -> c.enabled, v -> c.enabled = v));
            m.add(toggle("FPS", () -> c.showFps, v -> c.showFps = v));
            m.add(toggle("CPS", () -> c.showCps, v -> c.showCps = v));
            m.add(toggle("Ping", () -> c.showPing, v -> c.showPing = v));
            m.add(toggle("Combo", () -> c.showCombo, v -> c.showCombo = v));
            m.add(toggle("Reach", () -> c.showReach, v -> c.showReach = v));
            m.add(toggle("Koordinat", () -> c.showCoords, v -> c.showCoords = v));
            m.add(toggle("Yön", () -> c.showDirection, v -> c.showDirection = v));
            m.add(toggle("Hız", () -> c.showSpeed, v -> c.showSpeed = v));
            m.add(toggle("Saat", () -> c.showTime, v -> c.showTime = v));
            m.add(toggle("RAM", () -> c.showMemory, v -> c.showMemory = v));
            m.add(toggle("IP", () -> c.showIp, v -> c.showIp = v));
            m.add(toggle("Ok", () -> c.showArrows, v -> c.showArrows = v));
            m.add(toggle("Tuşlar", () -> c.showKeystrokes, v -> c.showKeystrokes = v));
            m.add(toggle("Zırh", () -> c.showArmor, v -> c.showArmor = v));
            m.add(toggle("Efekt", () -> c.showPotions, v -> c.showPotions = v));
            m.add(toggle("Hedef HUD", () -> c.targetHud, v -> c.targetHud = v));
        } else if (tab == 1) {
            m.add(cycle("Tema", () -> Theme.NAMES[c.theme], () -> c.theme = (c.theme + 1) % Theme.NAMES.length));
            m.add(toggle("Sarsıntı Yok", () -> c.noHurtShake, v -> c.noHurtShake = v));
            m.add(toggle("Düşük Ateş", () -> c.lowFire, v -> c.lowFire = v));
            m.add(toggle("Hep Gündüz", () -> c.visualDay, v -> c.visualDay = v));
            m.add(toggle("Yağmursuz", () -> c.clearWeather, v -> c.clearWeather = v));
            m.add(toggle("Vuruş Efekti", () -> c.hitParticles, v -> c.hitParticles = v));
            m.add(cycle("Efekt Türü", () -> Visuals.PARTICLE_NAMES[c.hitParticleType],
                    () -> c.hitParticleType = (c.hitParticleType + 1) % Visuals.PARTICLE_NAMES.length));
            m.add(cycle("Efekt Sayısı", () -> String.valueOf(c.hitParticleCount),
                    () -> c.hitParticleCount = Visuals.COUNTS[(countIndex(c.hitParticleCount) + 1) % Visuals.COUNTS.length]));
            m.add(toggle("Ölüm Efekti", () -> c.deathEffect, v -> c.deathEffect = v));
            m.add(toggle("Mermi İzi", () -> c.trails, v -> c.trails = v));
            m.add(toggle("Hitbox", () -> c.showHitboxes, v -> {
                c.showHitboxes = v;
                MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderHitboxes(v);
            }));
            m.add(toggle("Nişan Göstergesi", () -> c.crosshairIndicator, v -> c.crosshairIndicator = v));
        } else {
            m.add(cycle("Köşe", () -> CORNERS[c.corner], () -> c.corner = (c.corner + 1) % 4));
            m.add(cycle("Boyut", () -> Math.round(c.scale * 100) + "%",
                    () -> c.scale = SCALES[(scaleIndex(c.scale) + 1) % SCALES.length]));
            m.add(toggle("Arkaplan", () -> c.background, v -> c.background = v));
            m.add(toggle("Oto Sprint", () -> c.autoSprint, v -> c.autoSprint = v));
            m.add(toggle("Oto Boost", () -> c.autoBoost, v -> c.autoBoost = v));
            m.add((x, y, w, h) -> ButtonWidget.builder(Text.literal("FPS Boost Uygula"), b -> {
                FpsBoost.apply(client);
                b.setMessage(Text.literal("\u00a7aFPS Boost: TAMAM"));
            }).dimensions(x, y, w, h).build());
            m.add((x, y, w, h) -> ButtonWidget.builder(Text.literal("Kapat"), b -> close())
                    .dimensions(x, y, w, h).build());
        }

        int n = m.size();
        int cols = Math.max(2, Math.min(4, (width - 8) / 112));
        int colW = Math.min(150, (width - 8 - (cols - 1) * 4) / cols);
        int rows = (n + cols - 1) / cols;
        int top = 42;
        int step = 22;
        if (top + rows * step > height - 4) {
            step = Math.max(16, (height - top - 4) / rows);
        }
        int bh = step - 2;
        int totalW = cols * colW + (cols - 1) * 4;
        int x0 = (width - totalW) / 2;
        for (int i = 0; i < n; i++) {
            addDrawableChild(m.get(i).make(x0 + (i % cols) * (colW + 4), top + (i / cols) * step, colW, bh));
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "Vanta", width / 2, 6, Theme.accent());
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
