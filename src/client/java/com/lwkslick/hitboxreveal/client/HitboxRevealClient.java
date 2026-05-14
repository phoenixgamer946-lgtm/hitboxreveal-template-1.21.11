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
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HitboxRevealClient implements ClientModInitializer {

	public static final Map<UUID, Integer> revealedPlayers = new HashMap<>();
	// Pearl trail state
	public static final Map<UUID, List<Vec3d>> pearlTrails    = new HashMap<>();
	public static final Map<UUID, Long>        pearlLandingEffects = new HashMap<>();
	public static final Map<UUID, Vec3d>       landingPositions    = new HashMap<>();
	private static final Map<UUID, Vec3d>      pearlLastPos        = new HashMap<>();

	// Tracks all UUIDs currently being auto-revealed by solo auto-reveal
	public static final Set<UUID> soloAutoTargets = new HashSet<>();
	private static int soloAutoLingerTimer = 0;

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
				if (ModConfig.friends.contains(target.getName().getString())) return ActionResult.PASS;
				revealedPlayers.put(target.getUuid(), ModConfig.revealTicks);
				if (ModConfig.selfReveal && !ModConfig.selfRevealPermanent) {
					revealedPlayers.put(player.getUuid(), ModConfig.revealTicks);
				}
			}
			return ActionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// ── Pearl trail tracking ──────────────────────────────────────
			if (client.world != null && client.player != null) {
				Vec3d playerPos = new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ());
				Box searchBox = new Box(playerPos.subtract(200, 200, 200), playerPos.add(200, 200, 200));
				List<EnderPearlEntity> pearls = client.world.getEntitiesByClass(
						EnderPearlEntity.class, searchBox, e -> true);
				long now = System.currentTimeMillis();

				// Mark removed pearls
				for (UUID id : new ArrayList<>(pearlTrails.keySet())) {
					boolean stillActive = pearls.stream().anyMatch(p -> p.getUuid().equals(id) && !p.isRemoved());
					if (!stillActive && !pearlLandingEffects.containsKey(id)) {
						pearlLandingEffects.put(id, now);
						Vec3d lp = pearlLastPos.get(id);
						if (lp != null) {
							landingPositions.put(id, lp);
							if (ModConfig.pearlLandingParticles) {
								for (int i = 0; i < 30; i++) {
									client.particleManager.addParticle(
											net.minecraft.particle.ParticleTypes.PORTAL,
											lp.x + (Math.random()-0.5)*2,
											lp.y + Math.random()*2,
											lp.z + (Math.random()-0.5)*2,
											(Math.random()-0.5)*0.5, Math.random()*0.5, (Math.random()-0.5)*0.5);
								}
							}
						}
					}
				}

				// Expire landing effects
				long persistMs = ModConfig.pearlTrailPersistMs;
				long beamMs    = (long)(ModConfig.pearlBeamDuration * 1000);
				pearlLandingEffects.entrySet().removeIf(e -> now - e.getValue() > Math.max(persistMs, beamMs));
				pearlTrails.keySet().removeIf(id -> {
					if (pearlLandingEffects.containsKey(id) && now - pearlLandingEffects.get(id) > persistMs) {
						pearlLastPos.remove(id);
						return true;
					}
					return false;
				});
				landingPositions.keySet().removeIf(id ->
						pearlLandingEffects.containsKey(id) && now - pearlLandingEffects.get(id) > beamMs);

				// Track active pearls
				for (EnderPearlEntity pearl : pearls) {
					if (pearl.isRemoved()) continue;
					boolean isOwn = client.player.equals(pearl.getOwner());
					if (isOwn && !ModConfig.pearlTrailShowOwn) continue;

					UUID pid = pearl.getUuid();
					Vec3d cur = new Vec3d(pearl.getX(), pearl.getY(), pearl.getZ());
					Vec3d last = pearlLastPos.get(pid);
					if (last != null && last.distanceTo(cur) > 0.02) {
						List<Vec3d> trail = pearlTrails.computeIfAbsent(pid, k -> new ArrayList<>());
						trail.add(cur);
						if (trail.size() > ModConfig.pearlTrailMaxPoints) trail.remove(0);
					}
					pearlLastPos.put(pid, cur);
				}
			}
			// ── End pearl trail tracking ──────────────────────────────────
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

			// ── Solo auto-reveal ──────────────────────────────────────────
			if (ModConfig.soloAutoReveal && ModConfig.enabled && client.world != null && client.player != null) {
				java.util.List<PlayerEntity> nearby = new java.util.ArrayList<>();
				for (PlayerEntity p : client.world.getPlayers()) {
					if (p == client.player) continue;
					if (ModConfig.friends.contains(p.getName().getString())) continue;
					if (client.player.distanceTo(p) <= ModConfig.soloAutoRange) nearby.add(p);
				}

				if (nearby.size() >= 1 && nearby.size() <= ModConfig.soloAutoMaxPlayers) {
					Set<UUID> newTargets = new HashSet<>();
					for (PlayerEntity p : nearby) newTargets.add(p.getUuid());

					if (!newTargets.equals(soloAutoTargets)) {
						soloAutoTargets.clear();
						soloAutoTargets.addAll(newTargets);
						if (ModConfig.soloAutoActionBar && client.player != null)
							client.player.sendMessage(Text.literal("§eSolo mode: §aON (" + nearby.size() + ")"), true);
					}
					soloAutoLingerTimer = ModConfig.soloAutoLinger;
					for (UUID id : soloAutoTargets) revealedPlayers.put(id, Integer.MAX_VALUE);
				} else {
					if (!soloAutoTargets.isEmpty()) {
						if (soloAutoLingerTimer > 0) {
							soloAutoLingerTimer--;
							for (UUID id : soloAutoTargets) revealedPlayers.put(id, Integer.MAX_VALUE);
						} else {
							for (UUID id : soloAutoTargets) revealedPlayers.remove(id);
							if (ModConfig.soloAutoActionBar && client.player != null)
								client.player.sendMessage(Text.literal("§eSolo mode: §cLOST"), true);
							soloAutoTargets.clear();
						}
					}
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
				boolean isThirdPerson = !client.options.getPerspective().isFirstPerson();

				if (isSelf && ModConfig.selfReveal && ModConfig.selfRevealPermanent) {
					if (!isThirdPerson) continue;
					HitboxRenderer.renderBox(context, player, ModConfig.colorDefault, 1.0f);
					continue;
				}

				if (isSelf && (!ModConfig.selfReveal || !isThirdPerson || !revealedPlayers.containsKey(player.getUuid()))) continue;
				if (!revealedPlayers.containsKey(player.getUuid())) continue;

				boolean critReady = !player.isOnGround()
						&& client.player.getAttackCooldownProgress(0f) >= 1.0f;
				double dist = client.player.distanceTo(player);

				int color;
				if (isSelf) color = ModConfig.colorDefault;
				else if (critReady) color = ModConfig.colorCrit;
				else if (dist <= ModConfig.closeRangeThreshold) color = ModConfig.colorClose;
				else color = ModConfig.colorDefault;

				float alpha = 1.0f;
				if (ModConfig.fadeOut && !ModConfig.permanent) {
					int remaining = revealedPlayers.getOrDefault(player.getUuid(), 0);
					int fadeZone = Math.min(ModConfig.revealTicks, 40);
					if (remaining < fadeZone) alpha = (float) remaining / fadeZone;
				}
				if (ModConfig.pulse) {
					float pulse = 0.6f + 0.4f * (float) Math.sin(now / 1000.0 * ModConfig.pulseSpeed * Math.PI * 2);
					alpha *= pulse;
				}

				HitboxRenderer.renderBox(context, player, color, alpha);
			}

			// Range indicator
			// Pearl trail renderer
			PearlTrailRenderer.render(context, pearlTrails, pearlLandingEffects);
			if (ModConfig.rangeIndicator && client.player != null) {
				float pulseAlpha = 1.0f;
				if (ModConfig.soloAutoReveal && ModConfig.soloAutoRangeIndicatorPulse && !soloAutoTargets.isEmpty()) {
					pulseAlpha = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 300.0 * Math.PI * 2);
				}
				HitboxRenderer.renderRangeCircle(context, client.player, pulseAlpha);
			}

			// Entity hitboxes
			for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
				if (ModConfig.entityOnlyEnemy) {
					boolean isTntCart = entity instanceof TntMinecartEntity;
					boolean isBoat = entity instanceof BoatEntity || entity instanceof ChestBoatEntity;
					if (!isTntCart && !isBoat) {
						if (!(entity instanceof net.minecraft.entity.projectile.ProjectileEntity proj)) continue;
						if (proj.getOwner() == client.player) continue;
					}
				}

				if (ModConfig.pearlEnabled && entity instanceof EnderPearlEntity) {
					HitboxRenderer.renderEntityBox(context, entity, ModConfig.colorPearl, ModConfig.pearlSizeMulti, ModConfig.pearlOutlineOnly);
				} else if (ModConfig.arrowEnabled && entity instanceof ArrowEntity) {
					HitboxRenderer.renderEntityBox(context, entity, ModConfig.colorArrow, ModConfig.arrowSizeMulti, ModConfig.arrowOutlineOnly);
				} else if (ModConfig.windChargeEnabled && entity instanceof WindChargeEntity) {
					HitboxRenderer.renderEntityBox(context, entity, ModConfig.colorWindCharge, ModConfig.windChargesSizeMulti, ModConfig.windChargeOutlineOnly);
				} else if (ModConfig.tntMinecartEnabled && entity instanceof TntMinecartEntity) {
					HitboxRenderer.renderEntityBox(context, entity, ModConfig.colorTntMinecart, ModConfig.tntMinecartSizeMulti, ModConfig.tntMinecartOutlineOnly);
				} else if (ModConfig.fireballEnabled && entity instanceof FireballEntity) {
					HitboxRenderer.renderEntityBox(context, entity, ModConfig.colorFireball, ModConfig.fireballSizeMulti, ModConfig.fireballOutlineOnly);
				} else if (ModConfig.boatEnabled && (entity instanceof BoatEntity || entity instanceof ChestBoatEntity)) {
					HitboxRenderer.renderEntityBox(context, entity, ModConfig.colorBoat, ModConfig.boatSizeMulti, ModConfig.boatOutlineOnly);
				}
			}
		});

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client2) -> {
			revealedPlayers.clear();
			pearlTrails.clear();
			pearlLandingEffects.clear();
			landingPositions.clear();
			pearlLastPos.clear();
			soloAutoTargets.clear();
		});

		UpdateChecker.checkAsync();
	}
}