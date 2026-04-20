package com.lwkslick.hitboxreveal.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class ModConfig {
    public static boolean enabled     = true;
    public static int colorDefault    = 0xFFFFFF00;
    public static int colorClose      = 0xFFFF0000;
    public static int colorCrit       = 0xFFAA00FF;
    public static int revealTicks     = 60;
    public static boolean outline     = true;
    public static float lineWidth     = 2.0f;
    public static float fillOpacity   = 0.25f;
    public static boolean permanent   = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("hitboxreveal.json");

    // Internal data class for serialization
    private static class Data {
        boolean enabled;
        int colorDefault, colorClose, colorCrit, revealTicks;
        boolean outline;
        float lineWidth;
        float fillOpacity;
        boolean permanent;
    }

    public static void load() {
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) { save(); return; }
        try (Reader r = new FileReader(file)) {
            Data d = GSON.fromJson(r, Data.class);
            if (d == null) return;
            enabled      = d.enabled;
            colorDefault = d.colorDefault;
            colorClose   = d.colorClose;
            colorCrit    = d.colorCrit;
            revealTicks  = d.revealTicks;
            outline      = d.outline;
            lineWidth    = d.lineWidth;
            fillOpacity  = d.fillOpacity;
            permanent    = d.permanent;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        Data d = new Data();
        d.enabled      = enabled;
        d.colorDefault = colorDefault;
        d.colorClose   = colorClose;
        d.colorCrit    = colorCrit;
        d.revealTicks  = revealTicks;
        d.outline      = outline;
        d.lineWidth    = lineWidth;
        d.fillOpacity  = fillOpacity;
        d.permanent    = permanent;
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(d, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}