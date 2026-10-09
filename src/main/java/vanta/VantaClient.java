package vanta;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class VantaClient implements ClientModInitializer {
    public static KeyBinding menuKey;
    public static KeyBinding hudKey;
    public static KeyBinding zoomKey;

    @Override
    public void onInitializeClient() {
        VantaConfig.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vanta.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.vanta"));
        hudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vanta.hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.vanta"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vanta.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.vanta"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                client.setScreen(new VantaScreen(client.currentScreen));
            }
            while (hudKey.wasPressed()) {
                VantaConfig c = VantaConfig.get();
                c.enabled = !c.enabled;
                VantaConfig.save();
            }
            Stats.tick(client);
            Visuals.tick(client);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && entity instanceof LivingEntity) {
                double reach = hit != null
                        ? player.getEyePos().distanceTo(hit.getPos())
                        : player.distanceTo(entity);
                Stats.onHit(reach, entity.getId());
                Visuals.onHit(world, entity);
            }
            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register(VantaHud::render);
        ClientLifecycleEvents.CLIENT_STARTED.register(FpsBoost::onStart);
    }
}
