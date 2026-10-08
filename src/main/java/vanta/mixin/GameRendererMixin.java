package vanta.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vanta.VantaConfig;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = {"bobViewWhenHurt", "tiltViewWhenHurt"}, at = @At("HEAD"), cancellable = true)
    private void vanta$noHurtShake(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (VantaConfig.get().noHurtShake) {
            ci.cancel();
        }
    }
}
