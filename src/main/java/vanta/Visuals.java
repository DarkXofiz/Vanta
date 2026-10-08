package vanta;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Tamamen istemci tarafli gorsel efektler. */
public final class Visuals {
    public static final String[] PARTICLE_NAMES = {"Crit", "Büyülü", "Alev", "Kalp", "Totem", "Ruh Alevi"};
    public static final int[] COUNTS = {4, 8, 16, 32};

    private static final Set<Integer> DEAD = new HashSet<>();
    private static boolean hitboxApplied;
    private static boolean sprintForced;

    private Visuals() {}

    private static ParticleEffect particle(int i) {
        switch (i) {
            case 1: return ParticleTypes.ENCHANTED_HIT;
            case 2: return ParticleTypes.FLAME;
            case 3: return ParticleTypes.HEART;
            case 4: return ParticleTypes.TOTEM_OF_UNDYING;
            case 5: return ParticleTypes.SOUL_FIRE_FLAME;
            default: return ParticleTypes.CRIT;
        }
    }

    public static void onHit(World world, Entity target) {
        VantaConfig c = VantaConfig.get();
        if (!c.hitParticles || !world.isClient) return;
        ParticleEffect p = particle(c.hitParticleType);
        Random r = world.random;
        double w = target.getWidth();
        for (int i = 0; i < c.hitParticleCount; i++) {
            double x = target.getX() + (r.nextDouble() - 0.5) * w;
            double y = target.getBodyY(0.2 + r.nextDouble() * 0.7);
            double z = target.getZ() + (r.nextDouble() - 0.5) * w;
            world.addParticle(p, x, y, z,
                    (r.nextDouble() - 0.5) * 0.4, r.nextDouble() * 0.3, (r.nextDouble() - 0.5) * 0.4);
        }
    }

    private static void deathBurst(ClientWorld w, Entity e) {
        Random r = w.random;
        for (int i = 0; i < 40; i++) {
            w.addParticle(ParticleTypes.TOTEM_OF_UNDYING,
                    e.getX() + (r.nextDouble() - 0.5) * 0.6,
                    e.getBodyY(r.nextDouble()),
                    e.getZ() + (r.nextDouble() - 0.5) * 0.6,
                    (r.nextDouble() - 0.5) * 0.5, r.nextDouble() * 0.6, (r.nextDouble() - 0.5) * 0.5);
        }
        for (int i = 0; i < 12; i++) {
            w.addParticle(ParticleTypes.SOUL,
                    e.getX(), e.getBodyY(0.5), e.getZ(),
                    (r.nextDouble() - 0.5) * 0.2, 0.08 + r.nextDouble() * 0.1, (r.nextDouble() - 0.5) * 0.2);
        }
    }

    public static void tick(MinecraftClient mc) {
        ClientWorld w = mc.world;
        ClientPlayerEntity p = mc.player;
        if (w == null || p == null) {
            hitboxApplied = false;
            sprintForced = false;
            DEAD.clear();
            return;
        }
        VantaConfig c = VantaConfig.get();

        if (!hitboxApplied) {
            if (c.showHitboxes) mc.getEntityRenderDispatcher().setRenderHitboxes(true);
            hitboxApplied = true;
        }

        if (c.visualDay) w.setTimeOfDay(6000L);
        if (c.clearWeather) {
            w.setRainGradient(0f);
            w.setThunderGradient(0f);
        }

        autoSprint(mc, p, c);

        if (c.deathEffect || c.trails) {
            for (Entity e : w.getEntities()) {
                if (c.deathEffect && e instanceof PlayerEntity pl && pl != p) {
                    if (pl.isDead()) {
                        if (DEAD.add(pl.getId())) deathBurst(w, pl);
                    } else {
                        DEAD.remove(pl.getId());
                    }
                }
                if (c.trails) {
                    if (e instanceof EnderPearlEntity) {
                        w.addParticle(ParticleTypes.PORTAL, e.getX(), e.getY(), e.getZ(), 0, 0, 0);
                        w.addParticle(ParticleTypes.PORTAL, e.getX(), e.getY(), e.getZ(), 0, 0, 0);
                    } else if (e instanceof PersistentProjectileEntity
                            && e.getVelocity().lengthSquared() > 0.04) {
                        w.addParticle(ParticleTypes.CRIT, e.getX(), e.getY(), e.getZ(), 0, 0, 0);
                    }
                }
            }
            if (DEAD.size() > 256) DEAD.clear();
        }
    }

    private static void autoSprint(MinecraftClient mc, ClientPlayerEntity p, VantaConfig c) {
        if (c.autoSprint) {
            boolean ok = p.input.movementForward > 0 && !p.isSneaking() && !p.horizontalCollision
                    && !p.isUsingItem() && p.getHungerManager().getFoodLevel() > 6;
            if (ok) {
                mc.options.sprintKey.setPressed(true);
                sprintForced = true;
            }
        } else if (sprintForced) {
            mc.options.sprintKey.setPressed(false);
            sprintForced = false;
        }
    }
}
