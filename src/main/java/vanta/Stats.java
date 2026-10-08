package vanta;

import java.util.ArrayDeque;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;

public final class Stats {
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();
    private static boolean lDown;
    private static boolean rDown;

    public static int combo;
    public static double lastReach;
    private static long lastHit;
    private static int prevHurt;

    public static double speed;
    private static double px;
    private static double pz;
    private static boolean hasPos;

    private Stats() {}

    public static int leftCps() {
        return LEFT.size();
    }

    public static int rightCps() {
        return RIGHT.size();
    }

    /** Her karede cagrilir, tiklamalari kenar tespitiyle sayar. */
    public static void poll(MinecraftClient mc) {
        if (mc.getWindow() == null) return;
        long w = mc.getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(w, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(w, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();
        boolean inGame = mc.currentScreen == null && mc.player != null;
        if (inGame && l && !lDown) LEFT.addLast(now);
        if (inGame && r && !rDown) RIGHT.addLast(now);
        lDown = l;
        rDown = r;
        trim(LEFT, now);
        trim(RIGHT, now);
    }

    private static void trim(ArrayDeque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000L) q.pollFirst();
    }

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null) {
            combo = 0;
            hasPos = false;
            speed = 0;
            return;
        }
        if (p.hurtTime > prevHurt) combo = 0;
        prevHurt = p.hurtTime;
        if (combo > 0 && System.currentTimeMillis() - lastHit > 3000L) combo = 0;

        if (hasPos) {
            double d = Math.hypot(p.getX() - px, p.getZ() - pz) * 20.0;
            speed = speed * 0.7 + d * 0.3;
        }
        px = p.getX();
        pz = p.getZ();
        hasPos = true;
    }

    public static void onHit(double reach) {
        lastReach = reach;
        combo++;
        lastHit = System.currentTimeMillis();
    }
}
