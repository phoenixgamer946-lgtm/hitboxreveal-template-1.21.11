package com.lwkslick.hitboxreveal.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import java.net.URI;

public class ConfigScreen extends Screen {

    private final Screen parent;

    private HueSlider defaultHue, closeHue, critHue;
    private DurationSlider durationSlider;

    private int labelDefault, labelClose, labelCrit, labelSettings;

    public ConfigScreen(Screen parent) {
        super(Text.literal("HitboxReveal Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = 18;

        y += 12;
        labelDefault = y;
        y += 10;
        defaultHue = new HueSlider(cx - 100, y, 160, "Default", ModConfig.colorDefault);
        addDrawableChild(defaultHue);
        y += 28;

        labelClose = y;
        y += 10;
        closeHue = new HueSlider(cx - 100, y, 160, "Close", ModConfig.colorClose);
        addDrawableChild(closeHue);
        y += 28;

        labelCrit = y;
        y += 10;
        critHue = new HueSlider(cx - 100, y, 160, "Crit", ModConfig.colorCrit);
        addDrawableChild(critHue);
        y += 36;

        labelSettings = y;
        y += 12;

        durationSlider = new DurationSlider(cx - 100, y, 200, ModConfig.revealTicks);
        addDrawableChild(durationSlider);
        y += 28;

        // Permanent toggle
        addDrawableChild(ButtonWidget.builder(
                Text.literal(ModConfig.permanent ? "§aPermanent: ON" : "§7Permanent: OFF"),
                btn -> {
                    ModConfig.permanent = !ModConfig.permanent;
                    btn.setMessage(Text.literal(ModConfig.permanent ? "§aPermanent: ON" : "§7Permanent: OFF"));
                }
        ).dimensions(cx - 100, y, 200, 20).build());
        y += 24;

        // Reminder text drawn in render(), stored y for reference
        y += 12;

        // Outline toggle
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Outline: " + (ModConfig.outline ? "§a✔ ON" : "§c✘ OFF")),
                btn -> {
                    ModConfig.outline = !ModConfig.outline;
                    btn.setMessage(Text.literal("Outline: " + (ModConfig.outline ? "§a✔ ON" : "§c✘ OFF")));
                }
        ).dimensions(cx - 100, y, 200, 20).build());
        y += 28;

        // Done
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                btn -> close()
        ).dimensions(cx - 50, y, 100, 20).build());

        // Social buttons — bottom left
        int bx = 6;
        int by = height - 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("§2Modrinth"),
                btn -> openUrl("https://modrinth.com/user/lwkSlick")
        ).dimensions(bx, by, 60, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("§cYouTube"),
                btn -> openUrl("https://youtube.com/@lwkSlick")
        ).dimensions(bx + 64, by, 60, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("§9Discord"),
                btn -> openUrl("https://discord.gg/lwkSlick")
        ).dimensions(bx + 128, by, 60, 16).build());
    }

    private void openUrl(String url) {
        Util.getOperatingSystem().open(URI.create(url));
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        int cx = width / 2;

        ctx.drawCenteredTextWithShadow(textRenderer, "§e§lHitboxReveal §7Settings", cx, 7, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "§b── Colors ──", cx, 19, 0xFFFFFF);

        // Color row labels + preview boxes
        drawColorRow(ctx, cx, labelDefault, "§7Default §e(Normal)", defaultHue);
        drawColorRow(ctx, cx, labelClose,   "§7Close §c(≤3 blocks)", closeHue);
        drawColorRow(ctx, cx, labelCrit,    "§7Crit §d(Airborne + full cooldown)", critHue);

        ctx.drawCenteredTextWithShadow(textRenderer, "§b── Options ──", cx, labelSettings + 1, 0xFFFFFF);

        // Permanent reminder — drawn below the permanent button
        if (ModConfig.permanent) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    "§7Hitboxes won't expire — click again to disable",
                    cx, labelSettings + 57, 0xAAAAAA);
        }

        super.render(ctx, mx, my, delta);
    }

    private void drawColorRow(DrawContext ctx, int cx, int y, String label, HueSlider slider) {
        ctx.drawTextWithShadow(textRenderer, label, cx - 100, y + 1, 0xFFFFFF);
        // Color preview box — right of slider (slider ends at cx+60, box at cx+64)
        int color = slider.getArgb();
        ctx.fill(cx + 64, y + 10, cx + 84, y + 26, 0xFF000000 | (color & 0x00FFFFFF));
        ctx.fill(cx + 64, y + 10, cx + 84, y + 11, 0xFFFFFFFF);
        ctx.fill(cx + 64, y + 25, cx + 84, y + 26, 0xFFFFFFFF);
        ctx.fill(cx + 64, y + 10, cx + 65, y + 26, 0xFFFFFFFF);
        ctx.fill(cx + 83, y + 10, cx + 84, y + 26, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        ModConfig.colorDefault = defaultHue.getArgb();
        ModConfig.colorClose   = closeHue.getArgb();
        ModConfig.colorCrit    = critHue.getArgb();
        ModConfig.revealTicks  = durationSlider.getTicks();
        client.setScreen(parent);
    }

    // ── Hue Slider ───────────────────────────────────────────────────────────
    // Single slider 0–360 hue, full saturation/brightness. Preview updates live.

    static class HueSlider extends SliderWidget {
        private final String label;

        HueSlider(int x, int y, int width, String label, int argbColor) {
            super(x, y, width, 20, Text.literal(label), argbToHue(argbColor) / 360.0);
            this.label = label;
            updateMessage();
        }

        private static double argbToHue(int argb) {
            float r = ((argb >> 16) & 0xFF) / 255f;
            float g = ((argb >> 8)  & 0xFF) / 255f;
            float b = ((argb)       & 0xFF) / 255f;
            float max = Math.max(r, Math.max(g, b));
            float min = Math.min(r, Math.min(g, b));
            float delta = max - min;
            if (delta == 0) return 0;
            float hue;
            if (max == r)      hue = 60 * (((g - b) / delta) % 6);
            else if (max == g) hue = 60 * (((b - r) / delta) + 2);
            else               hue = 60 * (((r - g) / delta) + 4);
            if (hue < 0) hue += 360;
            return hue;
        }

        int getArgb() {
            float hue = (float)(value * 360);
            // HSV: S=1, V=1
            float c = 1f, x = c * (1 - Math.abs((hue / 60f) % 2 - 1));
            float r, g, b;
            int sector = (int)(hue / 60) % 6;
            switch (sector) {
                case 0 -> { r=c; g=x; b=0; }
                case 1 -> { r=x; g=c; b=0; }
                case 2 -> { r=0; g=c; b=x; }
                case 3 -> { r=0; g=x; b=c; }
                case 4 -> { r=x; g=0; b=c; }
                default-> { r=c; g=0; b=x; }
            }
            return 0xFF000000
                    | ((int)(r * 255) << 16)
                    | ((int)(g * 255) << 8)
                    |  (int)(b * 255);
        }

        @Override protected void updateMessage() {
            setMessage(Text.literal(label + " — Hue: " + (int)(value * 360) + "°"));
        }
        @Override protected void applyValue() {}
    }

    // ── Duration Slider ──────────────────────────────────────────────────────

    static class DurationSlider extends SliderWidget {
        // Steps in ticks: 1s to 120s
        private static final int[] STEPS = {
                20, 40, 60, 80, 100, 120, 140, 160, 200,
                240, 300, 400, 600, 800, 1200, 1600, 2000, 2400
        };

        DurationSlider(int x, int y, int width, int currentTicks) {
            super(x, y, width, 20, Text.literal(""), findStep(currentTicks));
            updateMessage();
        }

        private static double findStep(int ticks) {
            for (int i = 0; i < STEPS.length; i++)
                if (STEPS[i] == ticks) return i / (double)(STEPS.length - 1);
            return 2.0 / (STEPS.length - 1);
        }

        @Override protected void updateMessage() {
            int ticks = getTicks();
            String display = (ticks % 20 == 0) ? (ticks / 20) + "s" : ticks + " ticks";
            setMessage(Text.literal("Duration: " + display));
        }
        @Override protected void applyValue() {}
        int getTicks() { return STEPS[Math.min(Math.round((float)(value * (STEPS.length - 1))), STEPS.length - 1)]; }
    }
}