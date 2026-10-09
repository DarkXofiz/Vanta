package vanta;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** Hedef bilgi kartı: yüz, isim, can, zırh, mesafe, ping, eşyalar. */
public final class TargetHud {
    public static final int W = 150;
    /** Son cizilen kart (ekran koordinati), editor icin. */
    public static int rx;
    public static int ry;
    public static int rw;
    public static int rh;

    private static LivingEntity last;
    private static long lastSeen;
    private static float shown = -1f;
    private static int shownId = -1;

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

    private TargetHud() {}

    public static void clear() {
        rx = ry = rw = rh = 0;
    }

    public static LivingEntity pick(MinecraftClient mc, VantaConfig cfg) {
        long now = System.currentTimeMillis();
        if (mc.targetedEntity instanceof LivingEntity t && t.isAlive()) {
            last = t;
            lastSeen = now;
            return t;
        }
        if (cfg.tLinger && last != null && last.isAlive() && !last.isRemoved()
                && now - lastSeen < 2000L) {
            return last;
        }
        last = null;
        return null;
    }

    private static int shade(int argb, float f) {
        int a = argb & 0xFF000000;
        int r = Math.min(255, (int) (((argb >> 16) & 255) * f));
        int g = Math.min(255, (int) (((argb >> 8) & 255) * f));
        int b = Math.min(255, (int) ((argb & 255) * f));
        return a | (r << 16) | (g << 8) | b;
    }

    private static void face(DrawContext c, TextRenderer tr, LivingEntity t, int x, int y, int size) {
        Identifier tex = null;
        if (t instanceof AbstractClientPlayerEntity ap) tex = ap.getSkinTexture();
        if (tex != null) {
            c.drawTexture(tex, x, y, size, size, 8f, 8f, 8, 8, 64, 64);
            c.drawTexture(tex, x, y, size, size, 40f, 8f, 8, 8, 64, 64);
        } else {
            c.fill(x, y, x + size, y + size, 0xFF2A2A38);
            String n = t.getName().getString();
            String l = n.isEmpty() ? "?" : n.substring(0, 1).toUpperCase();
            c.drawText(tr, l, x + (size - tr.getWidth(l)) / 2, y + (size - 8) / 2, 0xFFFFFFFF, false);
        }
    }

    public static void draw(DrawContext c, MinecraftClient mc, TextRenderer tr, LivingEntity t,
                            int accent, int scrW, int scrH) {
        VantaConfig cfg = VantaConfig.get();
        boolean shadow = VantaHud.shadow;
        float ts = cfg.targetScale;
        boolean self = t == mc.player;

        List<ItemStack> items = new ArrayList<>();
        if (cfg.tItems) {
            for (EquipmentSlot s : SLOTS) {
                ItemStack st = t.getEquippedStack(s);
                if (!st.isEmpty()) items.add(st);
            }
        }
        int h = 36 + (items.isEmpty() ? 0 : 18);
        int pw = Math.round(W * ts);
        int sx = Math.round(scrW / 2f - pw / 2f + cfg.targetX);
        int sy = Math.round(scrH / 2f + 24 + cfg.targetY);
        sx = MathHelper.clamp(sx, 0, Math.max(0, scrW - pw));
        sy = MathHelper.clamp(sy, 0, Math.max(0, scrH - Math.round(h * ts)));
        rx = sx;
        ry = sy;
        rw = pw;
        rh = Math.round(h * ts);

        float hp = t.getHealth();
        float max = Math.max(1f, t.getMaxHealth());
        float abs = t.getAbsorptionAmount();
        if (!cfg.tAnimate || shownId != t.getId()) {
            shown = hp;
            shownId = t.getId();
        } else {
            shown += (hp - shown) * 0.2f;
        }

        float my = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        float diff = my - (hp + abs);
        int statusCol = diff > 0.5f ? 0xFF55FF55 : diff < -0.5f ? 0xFFFF5555 : 0xFFFFFF55;
        String status = diff > 0.5f ? "Önde" : diff < -0.5f ? "Geride" : "Eşit";
        boolean showStatus = cfg.tStatus && !self;

        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(sx, sy, 0f);
        m.scale(ts, ts, 1f);

        if (VantaHud.bgTop >>> 24 != 0) c.fillGradient(0, 0, W, h, VantaHud.bgTop, VantaHud.bgBot);
        c.fill(0, 0, W, 1, accent);
        c.fill(0, 0, 2, h, showStatus ? statusCol : accent);

        int tx = cfg.tFace ? 34 : 7;
        if (cfg.tFace) face(c, tr, t, 6, 5, 24);

        // isim ve can sayisi
        String hs = "";
        if (cfg.tHealthNum) {
            hs = String.format("%.1f", hp);
            if (abs > 0) hs += " +" + String.format("%.1f", abs);
        }
        int hsW = tr.getWidth(hs);
        int nameCol = 0xFF000000 | t.getTeamColorValue();
        String name = tr.trimToWidth(t.getName().getString(), W - 5 - tx - hsW - (hsW > 0 ? 4 : 0));
        c.drawText(tr, name, tx, 5, nameCol, shadow);
        if (hsW > 0) {
            float ratio = MathHelper.clamp(hp / max, 0f, 1f);
            int hc = ratio > 0.6f ? 0xFF55FF55 : ratio > 0.3f ? 0xFFFFFF55 : 0xFFFF5555;
            c.drawText(tr, hs, W - 5 - hsW, 5, hc, shadow);
        }

        // can cubugu
        int bx = tx;
        int by = 16;
        int bw = W - 5 - tx;
        int bh = 7;
        float ratio = MathHelper.clamp(hp / max, 0f, 1f);
        int col = ratio > 0.6f ? 0xFF55FF55 : ratio > 0.3f ? 0xFFFFFF55 : 0xFFFF5555;
        c.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xA0000000);
        c.fill(bx, by, bx + bw, by + bh, 0xFF1B1B25);
        int wShown = Math.round(bw * MathHelper.clamp(shown / max, 0f, 1f));
        int wReal = Math.round(bw * ratio);
        if (wShown > wReal) c.fill(bx + wReal, by, bx + wShown, by + bh, 0xB0FFFFFF);
        if (wReal > 0) c.fillGradient(bx, by, bx + wReal, by + bh, shade(col, 1.15f), shade(col, 0.65f));
        if (abs > 0) {
            int wAbs = Math.round(bw * MathHelper.clamp(abs / max, 0f, 1f));
            c.fill(bx, by, bx + wAbs, by + 2, 0xFFFFD700);
        }

        // bilgi satiri
        List<String> tok = new ArrayList<>();
        List<Integer> tcol = new ArrayList<>();
        if (cfg.tDistance) {
            tok.add(String.format("%.1fm", mc.player.distanceTo(t)));
            tcol.add(0xFFFFFFFF);
        }
        if (cfg.tPing && t instanceof PlayerEntity pe && mc.getNetworkHandler() != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(pe.getUuid());
            if (e != null) {
                int ms = e.getLatency();
                tok.add(ms + "ms");
                tcol.add(ms <= 80 ? 0xFF55FF55 : ms <= 160 ? 0xFFFFFF55 : 0xFFFF5555);
            }
        }
        if (cfg.tArmorPts) {
            tok.add("Zırh " + t.getArmor());
            tcol.add(0xFF55FFFF);
        }
        if (showStatus) {
            tok.add(status);
            tcol.add(statusCol);
        }
        m.push();
        m.translate(tx, 26f, 0f);
        m.scale(0.8f, 0.8f, 1f);
        float avail = (W - 5 - tx) / 0.8f;
        int cur = 0;
        for (int i = 0; i < tok.size(); i++) {
            int w = tr.getWidth(tok.get(i));
            if (cur + w > avail) break;
            c.drawText(tr, tok.get(i), cur, 0, tcol.get(i), shadow);
            cur += w + 7;
        }
        m.pop();

        // esyalar
        for (int i = 0; i < items.size(); i++) {
            ItemStack s = items.get(i);
            int ix = 6 + i * 18;
            c.drawItem(s, ix, 37);
            c.drawItemInSlot(tr, s, ix, 37);
        }

        m.pop();
    }
}
