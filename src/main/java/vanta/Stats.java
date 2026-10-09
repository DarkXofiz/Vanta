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
    public static int kills;
    public static int deaths;
    private static long lastHit;
    private static int lastHitId = -1;
    private static int prevHurt;
    private static boolean wasDead;
    private static long sessionStart;

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
            sessionStart = 0;
            wasDead = false;
            return;
        }
        if (sessionStart == 0) sessionStart = System.currentTimeMillis();

        boolean dead = p.isDead();
        if (dead && !wasDead) deaths++;
        wasDead = dead;

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

    public static void onHit(double reach, int targetId) {
        lastReach = reach;
        combo++;
        lastHit = System.currentTimeMillis();
        lastHitId = targetId;
    }

    /** Yakindaki bir oyuncu oldugunde cagrilir, son vurulan hedefse kill sayilir. */
    public static void onKill(int targetId) {
        if (targetId == lastHitId && System.currentTimeMillis() - lastHit < 6000L) {
            kills++;
            lastHitId = -1;
        }
    }

    public static String sessionText() {
        if (sessionStart == 0) return "0:00";
        long s = (System.currentTimeMillis() - sessionStart) / 1000L;
        long h = s / 3600L;
        long m = (s % 3600L) / 60L;
        long sec = s % 60L;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, sec) : String.format("%d:%02d", m, sec);
    }

    public static void resetSession() {
        kills = 0;
        deaths = 0;
        combo = 0;
        lastReach = 0;
        sessionStart = System.currentTimeMillis();
    }
}
