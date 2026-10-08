package vanta;

import net.minecraft.util.math.MathHelper;

public final class Theme {
    public static final String[] NAMES = {"Mor", "Mavi", "Kırmızı", "Yeşil", "RGB"};
    private static final int[] COLORS = {0xFF8B5CF6, 0xFF3B82F6, 0xFFEF4444, 0xFF22C55E};

    private Theme() {}

    public static int accent() {
        int t = VantaConfig.get().theme;
        if (t >= 4) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000f;
            return 0xFF000000 | MathHelper.hsvToRgb(hue, 0.65f, 1f);
        }
        return COLORS[Math.max(0, Math.min(3, t))];
    }
}
