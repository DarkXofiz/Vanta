package vanta;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

public final class FpsBoost {
    private FpsBoost() {}

    public static void onStart(MinecraftClient client) {
        VantaConfig c = VantaConfig.get();
        if (c.autoBoost && !c.boostApplied) {
            apply(client);
            c.boostApplied = true;
            VantaConfig.save();
        }
    }

    public static void apply(MinecraftClient client) {
        GameOptions o = client.options;
        o.getGraphicsMode().setValue(GraphicsMode.FAST);
        o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        o.getParticles().setValue(ParticlesMode.DECREASED);
        o.getEntityShadows().setValue(false);
        o.getAo().setValue(false);
        o.getBiomeBlendRadius().setValue(1);
        o.getEntityDistanceScaling().setValue(0.75);
        o.getMaxFps().setValue(260);
        o.getEnableVsync().setValue(false);
        if (o.getViewDistance().getValue() > 12) o.getViewDistance().setValue(12);
        if (o.getSimulationDistance().getValue() > 8) o.getSimulationDistance().setValue(8);
        o.write();
    }
}
