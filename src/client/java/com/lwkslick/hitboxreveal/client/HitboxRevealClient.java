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
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;

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
		ModConfig.load();

		KeyBinding.Category hitboxCategory = new KeyBinding.Category(net.minecraft.util.Identifier.of("hitboxreveal", "keycategory"));
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.hitboxreveal.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, hitboxCategory
		));
		configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.hitboxreveal.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, hitboxCategory
		));

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!(world instanceof net.minecraft.client.world.ClientWorld)) return ActionResult.PASS;
			if (!ModConfig.enabled) return ActionResult.PASS;
			if (entity instanceof PlayerEntity target) {
				revealedPlayers.put(target.getUuid(), ModConfig.revealTicks);
				// Self-reveal on-hit
				if (ModConfig.selfReveal && !ModConfig.selfRevealPermanent) {
					revealedPlayers.put(player.getUuid(), ModConfig.revealTicks);
				}
			}
			return ActionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.wasPressed()) {
				ModConfig.enabled = !ModConfig.enabled;
				if (client.player != null) {
					client.player.sendMessage(
							Text.literal("HitboxReveal: " + (ModConfig.enabled ? "§aON" : "§cOFF")), true
					);
				}
			}
			while (configKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(ConfigScreen.create(null));
				}
			}

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

			long now = System.currentTimeMillis();

			for (PlayerEntity player : client.world.getPlayers()) {
				boolean isSelf = player == client.player;

				// Self-reveal permanent
				MinecraftClient mc = MinecraftClient.getInstance();
				boolean isThirdPerson = mc.options.getPerspective().isFirstPerson() == false;

				if (isSelf && ModConfig.selfReveal && ModConfig.selfRevealPermanent) {
					if (!isThirdPerson) continue;
					HitboxRenderer.renderBox(context, player, ModConfig.colorDefault, 1.0f);
					continue;
				}

				if (isSelf && (!ModConfig.selfReveal || !isThirdPerson || !revealedPlayers.containsKey(player.getUuid()))) continue;

				// Skip self unless self-reveal on-hit is active

				if (!revealedPlayers.containsKey(player.getUuid())) continue;

				boolean critReady = !player.isOnGround()
						&& client.player.getAttackCooldownProgress(0f) >= 1.0f;
				double dist = client.player.distanceTo(player);

				int color;
				if (isSelf) color = ModConfig.colorDefault;
				else if (critReady) color = ModConfig.colorCrit;
				else if (dist <= ModConfig.closeRangeThreshold) color = ModConfig.colorClose;
				else color = ModConfig.colorDefault;

				// Fade alpha
				float alpha = 1.0f;
				if (ModConfig.fadeOut && !ModConfig.permanent) {
					int remaining = revealedPlayers.getOrDefault(player.getUuid(), 0);
					int fadeZone = Math.min(ModConfig.revealTicks, 40); // fade over last 2s
					if (remaining < fadeZone) {
						alpha = (float) remaining / fadeZone;
					}
				}

				// Pulse alpha (multiplied on top of fade)
				if (ModConfig.pulse) {
					float pulse = 0.6f + 0.4f * (float) Math.sin(now / 1000.0 * ModConfig.pulseSpeed * Math.PI * 2);
					alpha *= pulse;
				}

				HitboxRenderer.renderBox(context, player, color, alpha);
			}

			// Range indicator (drawn once around local player's attack range)
			if (ModConfig.rangeIndicator && client.player != null) {
				HitboxRenderer.renderRangeCircle(context, client.player);
			}

			// Entity hitboxes
			for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
				if (ModConfig.entityOnlyEnemy) {
					if (!(entity instanceof net.minecraft.entity.projectile.ProjectileEntity proj)) continue;
					if (proj.getOwner() == client.player) continue;
				}

				if (ModConfig.pearlEnabled && entity instanceof EnderPearlEntity) {
					HitboxRenderer.renderEntityBox(context, entity, ModConfig.colorPearl, ModConfig.pearlSizeMulti, ModConfig.pearlOutlineOnly);
				} else if (ModConfig.arrowEnabled && entity instanceof ArrowEntity) {
					HitboxRenderer.renderEntityBox(context, entity, ModConfig.colorArrow, ModConfig.arrowSizeMulti, ModConfig.arrowOutlineOnly);
				} else if (ModConfig.windChargeEnabled && entity instanceof WindChargeEntity) {
					HitboxRenderer.renderEntityBox(context, entity, ModConfig.colorWindCharge, ModConfig.windChargesSizeMulti, ModConfig.windChargeOutlineOnly);
				}
			}
		});

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client2) -> {
			revealedPlayers.clear();
		});

		UpdateChecker.checkAsync();
	}
}