package vanta;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/** HUD'u ve hedef kartını sürükleyerek taşıma ekranı. */
public class HudEditScreen extends Screen {
    public static boolean active;

    private final Screen parent;
    private int drag;

    public HudEditScreen(Screen parent) {
        super(Text.literal("HUD Düzenle"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        active = true;
        int bw = 90;
        addDrawableChild(ButtonWidget.builder(Text.literal("Sıfırla"), b -> {
            VantaConfig c = VantaConfig.get();
            c.hudX = 0;
            c.hudY = 0;
            c.targetX = 0;
            c.targetY = 0;
            VantaConfig.save();
        }).dimensions(width / 2 - bw - 2, height - 26, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Bitti"), b -> close())
                .dimensions(width / 2 + 2, height - 26, bw, 20).build());
    }

    private static void outline(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x - 1, y - 1, x + w + 1, y, col);
        c.fill(x - 1, y + h, x + w + 1, y + h + 1, col);
        c.fill(x - 1, y, x, y + h, col);
        c.fill(x + w, y, x + w + 1, y + h, col);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return w > 0 && mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int accent = Theme.accent();
        if (VantaHud.mainRw > 0) {
            outline(ctx, VantaHud.mainRx, VantaHud.mainRy, VantaHud.mainRw, VantaHud.mainRh,
                    drag == 1 ? 0xFFFFFFFF : accent);
            ctx.drawText(textRenderer, "HUD", VantaHud.mainRx + 2, Math.max(0, VantaHud.mainRy - 10), accent, true);
        }
        if (TargetHud.rw > 0) {
            outline(ctx, TargetHud.rx, TargetHud.ry, TargetHud.rw, TargetHud.rh,
                    drag == 2 ? 0xFFFFFFFF : accent);
            ctx.drawText(textRenderer, "Hedef HUD", TargetHud.rx + 2, Math.max(0, TargetHud.ry - 10), accent, true);
        }
        ctx.drawCenteredTextWithShadow(textRenderer, "Sürükleyerek taşı", width / 2, 8, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        if (button == 0) {
            if (inside(mx, my, TargetHud.rx, TargetHud.ry, TargetHud.rw, TargetHud.rh)) {
                drag = 2;
                return true;
            }
            if (inside(mx, my, VantaHud.mainRx, VantaHud.mainRy, VantaHud.mainRw, VantaHud.mainRh)) {
                drag = 1;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        VantaConfig c = VantaConfig.get();
        if (drag == 1 && button == 0) {
            c.hudX = MathHelper.clamp(c.hudX + (float) (dx / c.scale), -2000f, 2000f);
            c.hudY = MathHelper.clamp(c.hudY + (float) (dy / c.scale), -2000f, 2000f);
            return true;
        }
        if (drag == 2 && button == 0) {
            c.targetX = MathHelper.clamp(c.targetX + (float) dx, -2000f, 2000f);
            c.targetY = MathHelper.clamp(c.targetY + (float) dy, -2000f, 2000f);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (drag != 0) {
            drag = 0;
            VantaConfig.save();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public void removed() {
        active = false;
        VantaConfig.save();
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
