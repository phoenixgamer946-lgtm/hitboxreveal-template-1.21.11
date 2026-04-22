package com.lwkslick.hitboxreveal.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class UpdateChecker {

    private static final String MOD_ID = "hitboxreveal";
    private static final String API_URL =
            "https://api.modrinth.com/v2/project/" + MOD_ID + "/version";

    public static void checkAsync() {
        Thread thread = new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(API_URL);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", MOD_ID + "/update-check");
                conn.setRequestProperty("Accept", "application/json");
                if (conn.getResponseCode() != 200) return;
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(),
                                java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                String body = sb.toString();
                String marker = "\"version_number\":\"";
                int start = body.indexOf(marker);
                if (start == -1) return;
                start += marker.length();
                int end = body.indexOf("\"", start);
                if (end == -1) return;
                String latest = body.substring(start, end);
                String current = FabricLoader.getInstance()
                        .getModContainer(MOD_ID)
                        .map(c -> c.getMetadata().getVersion().getFriendlyString())
                        .orElse("0.0.0");
                if (!latest.equals(current)) {
                    scheduleMessage("§e[HitboxReveal] §fUpdate available: §7v" + current
                            + " §f→ §av" + latest
                            + " §f— modrinth.com/mod/" + MOD_ID);
                }
            } catch (Exception ignored) {}
        }, MOD_ID + "-update-check");
        thread.setDaemon(true);
        thread.start();
    }

    private static void scheduleMessage(String text) {
        Thread waiter = new Thread(() -> {
            try {
                MinecraftClient mc = MinecraftClient.getInstance();
                for (int i = 0; i < 200; i++) {
                    Thread.sleep(100);
                    if (mc.player != null && mc.world != null) {
                        mc.execute(() -> mc.player.sendMessage(
                                Text.literal(text), false));
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }, MOD_ID + "-update-msg");
        waiter.setDaemon(true);
        waiter.start();
    }
}