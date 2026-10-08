package vanta;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import vanta.mixin.MinecraftClientAccessor;

public final class VantaHud {
    private static final int W = 118;
    private static final int K = 22;
    private static final int G = 2;
    private static final int GAP = 4;
    private static final int BG_TOP = 0xB0141420;
    private static final int BG_BOT = 0x90000000;
    private static final int KEY_BG = 0x90000000;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int WHITE = 0xFFFFFFFF;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String[] DIRS = {"Güney +Z", "Batı -X", "Kuzey -Z", "Doğu +X"};

    private record Line(String label, String value, int color) {}

    private interface Drawer {
        void draw(DrawContext c, TextRenderer tr, int x, int y, int accent);
    }

    private record Block(int height, Drawer drawer) {}

    private VantaHud() {}

    public static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Stats.poll(mc);
        VantaConfig cfg = VantaConfig.get();
        if (!cfg.enabled || mc.player == null || mc.options.hudHidden
                || mc.options.debugEnabled) {
            return;
        }

        TextRenderer tr = mc.textRenderer;
        int accent = Theme.accent();
        int scrW = mc.getWindow().getScaledWidth();
        int scrH = mc.getWindow().getScaledHeight();

        if (cfg.crosshairIndicator && mc.targetedEntity instanceof LivingEntity) {
            crosshair(ctx, scrW / 2, scrH / 2);
        }
        if (cfg.targetHud) {
            targetHud(ctx, mc, tr, accent, scrW, scrH);
        }

        List<Block> blocks = new ArrayList<>();
        List<Line> lines = infoLines(mc, cfg);
        if (!lines.isEmpty()) blocks.add(infoBlock(lines, cfg));
        if (cfg.showKeystrokes) blocks.add(keysBlock(mc));
        if (cfg.showArmor) {
            Block b = armorBlock(mc, cfg);
            if (b != null) blocks.add(b);
        }
        if (cfg.showPotions) {
            Block b = potionBlock(mc, cfg);
            if (b != null) blocks.add(b);
        }
        if (blocks.isEmpty()) return;

        float s = cfg.scale;
        int sw = (int) (scrW / s);
        int sh = (int) (scrH / s);
        int total = GAP * (blocks.size() - 1);
        for (Block b : blocks) total += b.height();

        boolean left = cfg.corner % 2 == 0;
        boolean top = cfg.corner < 2;
        int x = left ? 4 : sw - W - 4;
        int y = top ? 4 : sh - total - 4;

        ctx.getMatrices().push();
        ctx.getMatrices().scale(s, s, 1f);
        for (Block b : blocks) {
            b.drawer().draw(ctx, tr, x, y, accent);
            y += b.height() + GAP;
        }
        ctx.getMatrices().pop();
    }

    private static void crosshair(DrawContext c, int cx, int cy) {
        int col = 0xFFFF3B3B;
        c.fill(cx - 8, cy - 8, cx - 4, cy - 7, col);
        c.fill(cx - 8, cy - 8, cx - 7, cy - 4, col);
        c.fill(cx + 4, cy - 8, cx + 8, cy - 7, col);
        c.fill(cx + 7, cy - 8, cx + 8, cy - 4, col);
        c.fill(cx - 8, cy + 7, cx - 4, cy + 8, col);
        c.fill(cx - 8, cy + 4, cx - 7, cy + 8, col);
        c.fill(cx + 4, cy + 7, cx + 8, cy + 8, col);
        c.fill(cx + 7, cy + 4, cx + 8, cy + 8, col);
    }

    private static void targetHud(DrawContext c, MinecraftClient mc, TextRenderer tr,
                                  int accent, int scrW, int scrH) {
        if (!(mc.targetedEntity instanceof LivingEntity t) || !t.isAlive()) return;
        int w = 110;
        int h = 26;
        int x = scrW / 2 - w / 2;
        int y = scrH / 2 + 24;

        float hp = t.getHealth();
        float max = Math.max(1f, t.getMaxHealth());
        float ratio = MathHelper.clamp(hp / max, 0f, 1f);
        int col = ratio > 0.6f ? 0xFF55FF55 : ratio > 0.3f ? 0xFFFFFF55 : 0xFFFF5555;
        String hs = String.format("%.1f", hp);
        if (t.getAbsorptionAmount() > 0) hs += " +" + String.format("%.1f", t.getAbsorptionAmount());

        c.fillGradient(x, y, x + w, y + h, BG_TOP, BG_BOT);
        c.fill(x, y, x + w, y + 1, accent);
        String name = tr.trimToWidth(t.getName().getString(), w - 12 - tr.getWidth(hs));
        c.drawText(tr, name, x + 4, y + 4, WHITE, true);
        c.drawText(tr, hs, x + w - 4 - tr.getWidth(hs), y + 4, col, true);
        int bw = w - 8;
        c.fill(x + 4, y + 15, x + 4 + bw, y + 21, 0xFF222222);
        c.fill(x + 4, y + 15, x + 4 + (int) (bw * ratio), y + 21, col);
    }

    private static List<Line> infoLines(MinecraftClient mc, VantaConfig cfg) {
        ClientPlayerEntity p = mc.player;
        List<Line> l = new ArrayList<>();
        if (cfg.showFps) {
            int fps = MinecraftClientAccessor.vanta$getCurrentFps();
            l.add(new Line("FPS", String.valueOf(fps),
                    fps >= 100 ? 0xFF55FF55 : fps >= 50 ? 0xFFFFFF55 : 0xFFFF5555));
        }
        if (cfg.showCps) {
            l.add(new Line("CPS", Stats.leftCps() + " | " + Stats.rightCps(), WHITE));
        }
        if (cfg.showPing) {
            int ping = ping(mc);
            l.add(new Line("Ping", ping < 0 ? "--" : ping + " ms",
                    ping < 0 ? WHITE : ping <= 80 ? 0xFF55FF55 : ping <= 160 ? 0xFFFFFF55 : 0xFFFF5555));
        }
        if (cfg.showCombo) {
            l.add(new Line("Combo", String.valueOf(Stats.combo), Stats.combo > 0 ? 0xFFFFAA00 : WHITE));
        }
        if (cfg.showReach) {
            l.add(new Line("Reach", Stats.lastReach > 0 ? String.format("%.2f", Stats.lastReach) : "--", WHITE));
        }
        if (cfg.showCoords) {
            l.add(new Line("XYZ", p.getBlockX() + " " + p.getBlockY() + " " + p.getBlockZ(), WHITE));
        }
        if (cfg.showDirection) {
            float yaw = MathHelper.wrapDegrees(p.getYaw());
            int idx = ((int) Math.floor((yaw + 45f) / 90f)) & 3;
            l.add(new Line("Yön", DIRS[idx], WHITE));
        }
        if (cfg.showSpeed) {
            l.add(new Line("Hız", String.format("%.1f b/s", Stats.speed), WHITE));
        }
        if (cfg.showArrows) {
            l.add(new Line("Ok", String.valueOf(countArrows(p)), WHITE));
        }
        if (cfg.autoSprint) {
            l.add(new Line("Sprint", p.isSprinting() ? "Açık" : "Kapalı",
                    p.isSprinting() ? 0xFF55FF55 : GRAY));
        }
        if (cfg.showTime) {
            l.add(new Line("Saat", LocalTime.now().format(TIME), WHITE));
        }
        if (cfg.showMemory) {
            Runtime rt = Runtime.getRuntime();
            long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
            long max = rt.maxMemory() / 1048576L;
            l.add(new Line("RAM", used + "/" + max + " MB", WHITE));
        }
        if (cfg.showIp) {
            ServerInfo si = mc.getCurrentServerEntry();
            String ip = si == null ? "Tekli" : mc.textRenderer.trimToWidth(si.address, 70);
            l.add(new Line("IP", ip, WHITE));
        }
        return l;
    }

    private static int countArrows(ClientPlayerEntity p) {
        int n = 0;
        PlayerInventory inv = p.getInventory();
        for (ItemStack s : inv.main) if (s.getItem() instanceof ArrowItem) n += s.getCount();
        for (ItemStack s : inv.offHand) if (s.getItem() instanceof ArrowItem) n += s.getCount();
        return n;
    }

    private static int ping(MinecraftClient mc) {
        if (mc.getNetworkHandler() == null || mc.player == null) return -1;
        PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return e == null ? -1 : e.getLatency();
    }

    private static void bg(DrawContext c, VantaConfig cfg, int x, int y, int w, int h) {
        if (cfg.background) c.fillGradient(x, y, x + w, y + h, BG_TOP, BG_BOT);
    }

    private static Block infoBlock(List<Line> lines, VantaConfig cfg) {
        int h = lines.size() * 10 + 5;
        return new Block(h, (c, tr, x, y, accent) -> {
            bg(c, cfg, x, y, W, h);
            c.fill(x, y, x + W, y + 1, accent);
            int ly = y + 4;
            for (Line ln : lines) {
                c.drawText(tr, ln.label(), x + 5, ly, GRAY, true);
                c.drawText(tr, ln.value(), x + W - 4 - tr.getWidth(ln.value()), ly, ln.color(), true);
                ly += 10;
            }
        });
    }

    private static Block keysBlock(MinecraftClient mc) {
        int h = 3 * (K + G) + 12;
        return new Block(h, (c, tr, x, y, accent) -> {
            GameOptions o = mc.options;
            int w3 = 3 * K + 2 * G;
            int x0 = x + (W - w3) / 2;
            int step = K + G;

            key(c, tr, "W", o.forwardKey.isPressed(), x0 + step, y, K, K, accent);
            key(c, tr, "A", o.leftKey.isPressed(), x0, y + step, K, K, accent);
            key(c, tr, "S", o.backKey.isPressed(), x0 + step, y + step, K, K, accent);
            key(c, tr, "D", o.rightKey.isPressed(), x0 + 2 * step, y + step, K, K, accent);

            int mw = (w3 - G) / 2;
            mouse(c, tr, "LMB", Stats.leftCps(), o.attackKey.isPressed(), x0, y + 2 * step, mw, K, accent);
            mouse(c, tr, "RMB", Stats.rightCps(), o.useKey.isPressed(), x0 + mw + G, y + 2 * step, mw, K, accent);

            key(c, tr, "SPACE", o.jumpKey.isPressed(), x0, y + 3 * step, w3, 12, accent);
        });
    }

    private static int pressed(int accent) {
        return (accent & 0x00FFFFFF) | 0xC0000000;
    }

    private static void key(DrawContext c, TextRenderer tr, String label, boolean down,
                            int x, int y, int w, int h, int accent) {
        c.fill(x, y, x + w, y + h, down ? pressed(accent) : KEY_BG);
        if (!down) c.fill(x, y + h - 1, x + w, y + h, 0x40FFFFFF);
        int tw = tr.getWidth(label);
        c.drawText(tr, label, x + (w - tw) / 2, y + (h - 8) / 2, WHITE, false);
    }

    private static void mouse(DrawContext c, TextRenderer tr, String label, int cps, boolean down,
                              int x, int y, int w, int h, int accent) {
        c.fill(x, y, x + w, y + h, down ? pressed(accent) : KEY_BG);
        if (!down) c.fill(x, y + h - 1, x + w, y + h, 0x40FFFFFF);
        c.drawText(tr, label, x + (w - tr.getWidth(label)) / 2, y + 3, WHITE, false);
        String s = String.valueOf(cps);
        c.drawText(tr, s, x + (w - tr.getWidth(s)) / 2, y + 12, down ? WHITE : GRAY, false);
    }

    private static Block armorBlock(MinecraftClient mc, VantaConfig cfg) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            ItemStack s = mc.player.getInventory().armor.get(i);
            if (!s.isEmpty()) items.add(s);
        }
        if (items.isEmpty()) return null;
        int h = items.size() * 18 + 2;
        return new Block(h, (c, tr, x, y, accent) -> {
            bg(c, cfg, x, y, W, h);
            for (int i = 0; i < items.size(); i++) {
                ItemStack s = items.get(i);
                int iy = y + 1 + i * 18;
                c.drawItem(s, x + 2, iy);
                c.drawItemInSlot(tr, s, x + 2, iy);
                if (s.isDamageable()) {
                    int left = s.getMaxDamage() - s.getDamage();
                    c.drawText(tr, String.valueOf(left), x + 24, iy + 4,
                            0xFF000000 | s.getItemBarColor(), true);
                }
            }
        });
    }

    private static Block potionBlock(MinecraftClient mc, VantaConfig cfg) {
        Collection<StatusEffectInstance> fx = mc.player.getStatusEffects();
        if (fx.isEmpty()) return null;
        List<StatusEffectInstance> list = new ArrayList<>(fx);
        int h = list.size() * 10 + 4;
        return new Block(h, (c, tr, x, y, accent) -> {
            bg(c, cfg, x, y, W, h);
            int ly = y + 3;
            for (StatusEffectInstance e : list) {
                String name = e.getEffectType().getName().getString();
                if (e.getAmplifier() > 0) name += " " + (e.getAmplifier() + 1);
                String time = e.getDuration() == -1 ? "--" : fmt(e.getDuration());
                int col = 0xFF000000 | e.getEffectType().getColor();
                c.drawText(tr, name, x + 4, ly, col, true);
                c.drawText(tr, time, x + W - 4 - tr.getWidth(time), ly, WHITE, true);
                ly += 10;
            }
        });
    }

    private static String fmt(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
