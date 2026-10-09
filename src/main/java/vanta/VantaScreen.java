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
    private static final String[] TABS = {"HUD", "Hedef", "Görsel", "Ayarlar"};
    private static final String[] INDICATORS = {"Kırmızı", "Tema", "Beyaz"};
    private static final float[] SCALES = {0.7f, 0.85f, 1.0f, 1.25f, 1.5f};
    private static final int[] OPACITIES = {0, 25, 50, 65, 80, 100};
    private static final int[] ZOOMS = {30, 40, 50};
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

    private Maker button(String text, Consumer<ButtonWidget> action) {
        return (x, y, w, h) -> ButtonWidget.builder(Text.literal(text), b -> action.accept(b))
                .dimensions(x, y, w, h).build();
    }

    private static int nearest(float[] arr, float v) {
        int best = 0;
        for (int i = 1; i < arr.length; i++) {
            if (Math.abs(arr[i] - v) < Math.abs(arr[best] - v)) best = i;
        }
        return best;
    }

    private static int nearest(int[] arr, int v) {
        int best = 0;
        for (int i = 1; i < arr.length; i++) {
            if (Math.abs(arr[i] - v) < Math.abs(arr[best] - v)) best = i;
        }
        return best;
    }

    @Override
    protected void init() {
        VantaConfig c = VantaConfig.get();

        int n = TABS.length;
        int tw = Math.max(36, Math.min(80, (width - 16) / n - 4));
        int tx0 = (width - (n * tw + (n - 1) * 4)) / 2;
        for (int i = 0; i < n; i++) {
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
            m.add(toggle("K/D", () -> c.showKd, v -> c.showKd = v));
            m.add(toggle("Koordinat", () -> c.showCoords, v -> c.showCoords = v));
            m.add(toggle("Yön", () -> c.showDirection, v -> c.showDirection = v));
            m.add(toggle("Hız", () -> c.showSpeed, v -> c.showSpeed = v));
            m.add(toggle("Saat", () -> c.showTime, v -> c.showTime = v));
            m.add(toggle("Süre", () -> c.showSession, v -> c.showSession = v));
            m.add(toggle("RAM", () -> c.showMemory, v -> c.showMemory = v));
            m.add(toggle("IP", () -> c.showIp, v -> c.showIp = v));
            m.add(toggle("Ok", () -> c.showArrows, v -> c.showArrows = v));
            m.add(toggle("Tuşlar", () -> c.showKeystrokes, v -> c.showKeystrokes = v));
            m.add(toggle("Zırh", () -> c.showArmor, v -> c.showArmor = v));
            m.add(toggle("Efekt", () -> c.showPotions, v -> c.showPotions = v));
        } else if (tab == 1) {
            m.add(toggle("Hedef HUD", () -> c.targetHud, v -> c.targetHud = v));
            m.add(toggle("Yüz", () -> c.tFace, v -> c.tFace = v));
            m.add(toggle("Can Sayısı", () -> c.tHealthNum, v -> c.tHealthNum = v));
            m.add(toggle("Zırh Puanı", () -> c.tArmorPts, v -> c.tArmorPts = v));
            m.add(toggle("Eşyalar", () -> c.tItems, v -> c.tItems = v));
            m.add(toggle("Mesafe", () -> c.tDistance, v -> c.tDistance = v));
            m.add(toggle("Ping", () -> c.tPing, v -> c.tPing = v));
            m.add(toggle("Önde/Geride", () -> c.tStatus, v -> c.tStatus = v));
            m.add(toggle("Can Animasyonu", () -> c.tAnimate, v -> c.tAnimate = v));
            m.add(toggle("Hedef Kalsın", () -> c.tLinger, v -> c.tLinger = v));
            m.add(cycle("Boyut", () -> Math.round(c.targetScale * 100) + "%",
                    () -> c.targetScale = SCALES[(nearest(SCALES, c.targetScale) + 1) % SCALES.length]));
            m.add(button("Konumu Düzenle", b -> client.setScreen(new HudEditScreen(this))));
        } else if (tab == 2) {
            m.add(cycle("Tema", () -> Theme.NAMES[c.theme], () -> c.theme = (c.theme + 1) % Theme.NAMES.length));
            m.add(toggle("Sarsıntı Yok", () -> c.noHurtShake, v -> c.noHurtShake = v));
            m.add(toggle("Düşük Ateş", () -> c.lowFire, v -> c.lowFire = v));
            m.add(toggle("Hep Gündüz", () -> c.visualDay, v -> c.visualDay = v));
            m.add(toggle("Yağmursuz", () -> c.clearWeather, v -> c.clearWeather = v));
            m.add(toggle("Vuruş Efekti", () -> c.hitParticles, v -> c.hitParticles = v));
            m.add(cycle("Efekt Türü", () -> Visuals.PARTICLE_NAMES[c.hitParticleType],
                    () -> c.hitParticleType = (c.hitParticleType + 1) % Visuals.PARTICLE_NAMES.length));
            m.add(cycle("Efekt Sayısı", () -> String.valueOf(c.hitParticleCount),
                    () -> c.hitParticleCount = Visuals.COUNTS[(nearest(Visuals.COUNTS, c.hitParticleCount) + 1) % Visuals.COUNTS.length]));
            m.add(toggle("Ölüm Efekti", () -> c.deathEffect, v -> c.deathEffect = v));
            m.add(toggle("Mermi İzi", () -> c.trails, v -> c.trails = v));
            m.add(toggle("Hitbox", () -> c.showHitboxes, v -> {
                c.showHitboxes = v;
                MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderHitboxes(v);
            }));
            m.add(toggle("Nişan Göstergesi", () -> c.crosshairIndicator, v -> c.crosshairIndicator = v));
            m.add(cycle("Gösterge Rengi", () -> INDICATORS[c.indicatorColor],
                    () -> c.indicatorColor = (c.indicatorColor + 1) % INDICATORS.length));
        } else {
            m.add(cycle("Köşe", () -> CORNERS[c.corner], () -> c.corner = (c.corner + 1) % 4));
            m.add(cycle("Boyut", () -> Math.round(c.scale * 100) + "%",
                    () -> c.scale = SCALES[(nearest(SCALES, c.scale) + 1) % SCALES.length]));
            m.add(toggle("Arkaplan", () -> c.background, v -> c.background = v));
            m.add(cycle("Opaklık", () -> c.bgOpacity + "%",
                    () -> c.bgOpacity = OPACITIES[(nearest(OPACITIES, c.bgOpacity) + 1) % OPACITIES.length]));
            m.add(toggle("Yazı Gölgesi", () -> c.textShadow, v -> c.textShadow = v));
            m.add(button("HUD Düzenle", b -> client.setScreen(new HudEditScreen(this))));
            m.add(toggle("Zoom (C)", () -> c.zoomEnabled, v -> c.zoomEnabled = v));
            m.add(cycle("Zoom FOV", () -> String.valueOf(c.zoomFov),
                    () -> c.zoomFov = ZOOMS[(nearest(ZOOMS, c.zoomFov) + 1) % ZOOMS.length]));
            m.add(toggle("Oto Sprint", () -> c.autoSprint, v -> c.autoSprint = v));
            m.add(toggle("Oto Boost", () -> c.autoBoost, v -> c.autoBoost = v));
            m.add(button("FPS Boost Uygula", b -> {
                FpsBoost.apply(client);
                b.setMessage(Text.literal("\u00a7aFPS Boost: TAMAM"));
            }));
            m.add(button("İstatistik Sıfırla", b -> {
                Stats.resetSession();
                b.setMessage(Text.literal("\u00a7aSıfırlandı"));
            }));
            m.add(button("Ayarları Sıfırla", b -> {
                VantaConfig.reset();
                clearAndInit();
            }));
            m.add(button("Kapat", b -> close()));
        }

        int cnt = m.size();
        int cols = Math.max(2, Math.min(4, (width - 8) / 112));
        int colW = Math.min(150, (width - 8 - (cols - 1) * 4) / cols);
        int rows = (cnt + cols - 1) / cols;
        int top = 42;
        int step = 22;
        if (top + rows * step > height - 4) {
            step = Math.max(16, (height - top - 4) / rows);
        }
        int bh = step - 2;
        int totalW = cols * colW + (cols - 1) * 4;
        int x0 = (width - totalW) / 2;
        for (int i = 0; i < cnt; i++) {
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
