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
    public static boolean permanent       = false;
    public static boolean eyeHeightBox    = false;
    public static boolean lookVector      = false;
    public static int     colorGradientTop = 0xFF00FFFF;   // top gradient color (ARGB)
    public static float   lookVectorLength = 2.0f;
    public static float   lookVectorWidth  = 2.0f;
    public static float   satDefault       = 1.0f;
    public static float   satClose         = 1.0f;
    public static float   satCrit          = 1.0f;
    public static float   satGradientTop   = 1.0f;
    public static float   briDefault       = 1.0f;
    public static float   briClose         = 1.0f;
    public static float   briCrit          = 1.0f;
    public static float   briGradientTop   = 1.0f;
    public static float   closeRangeThreshold = 3.0f;
    public static boolean gradientEnabled     = true;
    public static boolean perStateGradient    = false;
    public static int     colorGradientTopClose = 0xFF00FFFF;
    public static int     colorGradientTopCrit  = 0xFF00FFFF;
    public static boolean fadeOut           = true;
    public static boolean pulse             = false;
    public static float   pulseSpeed        = 1.0f;
    public static boolean cornerOnly        = false;
    public static float   cornerLength      = 0.25f;
    public static boolean rangeIndicator    = false;
    public static int     colorRangeIndicator = 0xFF00FFFF;
    public static boolean selfReveal        = false;
    public static boolean selfRevealPermanent = false;

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
        boolean eyeHeightBox;
        boolean lookVector;
        int colorGradientTop;
        float lookVectorLength;
        float lookVectorWidth;
        float satDefault, satClose, satCrit, satGradientTop;
        float briDefault, briClose, briCrit, briGradientTop;
        float closeRangeThreshold;
        boolean gradientEnabled;
        boolean perStateGradient;
        int colorGradientTopClose;
        int colorGradientTopCrit;
        boolean fadeOut;
        boolean pulse;
        float pulseSpeed;
        boolean cornerOnly;
        float cornerLength;
        boolean rangeIndicator;
        int colorRangeIndicator;
        boolean selfReveal;
        boolean selfRevealPermanent;
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
            permanent         = d.permanent;
            eyeHeightBox      = d.eyeHeightBox;
            lookVector        = d.lookVector;
            colorGradientTop  = d.colorGradientTop != 0 ? d.colorGradientTop : 0xFF00FFFF;
            lookVectorLength  = d.lookVectorLength != 0 ? d.lookVectorLength : 2.0f;
            lookVectorWidth   = d.lookVectorWidth  != 0 ? d.lookVectorWidth  : 2.0f;
            satDefault       = d.satDefault != 0 ? d.satDefault : 1.0f;
            satClose         = d.satClose   != 0 ? d.satClose   : 1.0f;
            satCrit          = d.satCrit    != 0 ? d.satCrit    : 1.0f;
            satGradientTop   = d.satGradientTop != 0 ? d.satGradientTop : 1.0f;
            briDefault       = d.briDefault != 0 ? d.briDefault : 1.0f;
            briClose         = d.briClose   != 0 ? d.briClose   : 1.0f;
            briCrit          = d.briCrit    != 0 ? d.briCrit    : 1.0f;
            briGradientTop   = d.briGradientTop != 0 ? d.briGradientTop : 1.0f;
            closeRangeThreshold = d.closeRangeThreshold != 0 ? d.closeRangeThreshold : 3.0f;
            gradientEnabled      = d.gradientEnabled;
            perStateGradient     = d.perStateGradient;
            colorGradientTopClose = d.colorGradientTopClose != 0 ? d.colorGradientTopClose : 0xFF00FFFF;
            colorGradientTopCrit  = d.colorGradientTopCrit  != 0 ? d.colorGradientTopCrit  : 0xFF00FFFF;
            fadeOut              = d.fadeOut;
            pulse                = d.pulse;
            pulseSpeed           = d.pulseSpeed != 0 ? d.pulseSpeed : 1.0f;
            cornerOnly           = d.cornerOnly;
            cornerLength         = d.cornerLength != 0 ? d.cornerLength : 0.25f;
            rangeIndicator       = d.rangeIndicator;
            colorRangeIndicator  = d.colorRangeIndicator != 0 ? d.colorRangeIndicator : 0xFF00FFFF;
            selfReveal           = d.selfReveal;
            selfRevealPermanent  = d.selfRevealPermanent;
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
        d.permanent        = permanent;
        d.eyeHeightBox     = eyeHeightBox;
        d.lookVector       = lookVector;
        d.colorGradientTop = colorGradientTop;
        d.lookVectorLength = lookVectorLength;
        d.lookVectorWidth  = lookVectorWidth;
        d.satDefault       = satDefault;
        d.satClose         = satClose;
        d.satCrit          = satCrit;
        d.satGradientTop   = satGradientTop;
        d.briDefault       = briDefault;
        d.briClose         = briClose;
        d.briCrit          = briCrit;
        d.briGradientTop   = briGradientTop;
        d.closeRangeThreshold = closeRangeThreshold;
        d.gradientEnabled      = gradientEnabled;
        d.perStateGradient     = perStateGradient;
        d.colorGradientTopClose = colorGradientTopClose;
        d.colorGradientTopCrit  = colorGradientTopCrit;
        d.fadeOut              = fadeOut;
        d.pulse                = pulse;
        d.pulseSpeed           = pulseSpeed;
        d.cornerOnly           = cornerOnly;
        d.cornerLength         = cornerLength;
        d.rangeIndicator       = rangeIndicator;
        d.colorRangeIndicator  = colorRangeIndicator;
        d.selfReveal           = selfReveal;
        d.selfRevealPermanent  = selfRevealPermanent;
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(d, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}