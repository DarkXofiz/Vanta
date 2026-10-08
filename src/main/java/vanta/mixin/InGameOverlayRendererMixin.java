package vanta.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vanta.VantaConfig;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    private static boolean vanta$pushed;

    @Inject(method = "renderFireOverlay", at = @At("HEAD"))
    private static void vanta$fireStart(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        vanta$pushed = VantaConfig.get().lowFire;
        if (vanta$pushed) {
            matrices.push();
            matrices.translate(0f, -0.3f, 0f);
        }
    }

    @Inject(method = "renderFireOverlay", at = @At("RETURN"))
    private static void vanta$fireEnd(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        if (vanta$pushed) {
            matrices.pop();
            vanta$pushed = false;
        }
    }
}
