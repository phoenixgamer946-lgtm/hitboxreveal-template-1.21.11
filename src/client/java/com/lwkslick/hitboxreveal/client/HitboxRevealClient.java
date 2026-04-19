package com.lwkslick.hitboxreveal.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class HitboxRevealClient implements ClientModInitializer {

	public static final Map<UUID, Integer> revealedPlayers = new HashMap<>();
	public static final int REVEAL_TICKS = 60;

	@Override
	public void onInitializeClient() {

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient()) return ActionResult.PASS;
			if (entity instanceof PlayerEntity target) {
				revealedPlayers.put(target.getUuid(), REVEAL_TICKS);
			}
			return ActionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			Iterator<Map.Entry<UUID, Integer>> it = revealedPlayers.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<UUID, Integer> entry = it.next();
				if (entry.getValue() <= 0) {
					it.remove();
				} else {
					entry.setValue(entry.getValue() - 1);
				}
			}
		});

		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null || client.player == null) return;

			for (PlayerEntity player : client.world.getPlayers()) {
				if (!revealedPlayers.containsKey(player.getUuid())) continue;
				if (player == client.player) continue;

				double dist = client.player.distanceTo(player);
				int color = dist <= 3.0 ? 0xFFFF0000 : 0xFFFFFF00; // red or yellow

				HitboxRenderer.renderBox(context, player, color);
			}
		});
	}
}