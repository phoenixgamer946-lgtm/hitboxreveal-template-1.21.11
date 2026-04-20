package com.lwkslick.hitboxreveal.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class HitboxRevealClient implements ClientModInitializer {

	public static final Map<UUID, Integer> revealedPlayers = new HashMap<>();

	private static KeyBinding toggleKey;
	private static KeyBinding configKey;

	@Override
	public void onInitializeClient() {

		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.hitboxreveal.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, KeyBinding.Category.MISC
		));
		configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.hitboxreveal.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, KeyBinding.Category.MISC
		));

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!(world instanceof net.minecraft.client.world.ClientWorld)) return ActionResult.PASS;
			if (!ModConfig.enabled) return ActionResult.PASS;
			if (entity instanceof PlayerEntity target) {
				revealedPlayers.put(target.getUuid(), ModConfig.revealTicks);
			}
			return ActionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Key: toggle
			while (toggleKey.wasPressed()) {
				ModConfig.enabled = !ModConfig.enabled;
				if (client.player != null) {
					client.player.sendMessage(
							Text.literal("HitboxReveal: " + (ModConfig.enabled ? "§aON" : "§cOFF")), true
					);
				}
			}
			// Key: config screen
			while (configKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new ConfigScreen(null));
				}
			}

			// Tick down revealed players
			if (!ModConfig.permanent) {
				Iterator<Map.Entry<UUID, Integer>> it = revealedPlayers.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<UUID, Integer> entry = it.next();
					if (entry.getValue() <= 0) it.remove();
					else entry.setValue(entry.getValue() - 1);
				}
			}
		});

		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (!ModConfig.enabled) return;
			if (client.world == null || client.player == null) return;

			for (PlayerEntity player : client.world.getPlayers()) {
				if (!revealedPlayers.containsKey(player.getUuid())) continue;
				if (player == client.player) continue;

				boolean critReady = !player.isOnGround()
						&& client.player.getAttackCooldownProgress(0f) >= 1.0f;

				double dist = client.player.distanceTo(player);

				int color;
				if (critReady) color = ModConfig.colorCrit;
				else if (dist <= 3.0) color = ModConfig.colorClose;
				else color = ModConfig.colorDefault;

				if (ModConfig.outline) HitboxRenderer.renderBox(context, player, color);
			}
		});
	}
}