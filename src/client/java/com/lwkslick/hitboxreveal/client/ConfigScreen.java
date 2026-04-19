package com.lwkslick.hitboxreveal.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private ColorSlider defaultR, defaultG, defaultB;
    private ColorSlider closeR,   closeG,   closeB;
    private ColorSlider critR,    critG,    critB;
    private DurationSlider durationSlider;
    private SimpleSlider thicknessSlider;

    // label positions stored for render()
    private int labelDefault, labelClose, labelCrit, labelSettings;

    public ConfigScreen(Screen parent) {
        super(Text.literal("HitboxReveal Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = 18;

        // ── Section: Colors ──────────────────────────
        y += 12; // space for section heading drawn in render()
        labelDefault = y;

        // Default color
        y += 10;
        int dr = (ModConfig.colorDefault >> 16) & 0xFF;
        int dg = (ModConfig.colorDefault >>  8) & 0xFF;
        int db =  ModConfig.colorDefault        & 0xFF;
        defaultR = addRGB(cx - 152, y, "Red",   dr);
        defaultG = addRGB(cx -  52, y, "Green", dg);
        defaultB = addRGB(cx +  48, y, "Blue",  db);
        y += 26;

        labelClose = y;
        y += 10;
        int cr = (ModConfig.colorClose >> 16) & 0xFF;
        int cg = (ModConfig.colorClose >>  8) & 0xFF;
        int cb =  ModConfig.colorClose        & 0xFF;
        closeR = addRGB(cx - 152, y, "Red",   cr);
        closeG = addRGB(cx -  52, y, "Green", cg);
        closeB = addRGB(cx +  48, y, "Blue",  cb);
        y += 26;

        labelCrit = y;
        y += 10;
        int xr = (ModConfig.colorCrit >> 16) & 0xFF;
        int xg = (ModConfig.colorCrit >>  8) & 0xFF;
        int xb =  ModConfig.colorCrit        & 0xFF;
        critR = addRGB(cx - 152, y, "Red",   xr);
        critG = addRGB(cx -  52, y, "Green", xg);
        critB = addRGB(cx +  48, y, "Blue",  xb);
        y += 32;

        // ── Section: Settings ────────────────────────
        labelSettings = y;
        y += 12;

        durationSlider = new DurationSlider(cx - 100, y, 200, ModConfig.revealTicks);
        addDrawableChild(durationSlider);
        y += 24;

        thicknessSlider = new SimpleSlider(cx - 100, y, 200, "Line Width", ModConfig.lineWidth, 0.5f, 4.0f);
        addDrawableChild(thicknessSlider);
        y += 24;

        // Outline toggle — full width
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Outline: " + (ModConfig.outline ? "✔ ON" : "✘ OFF")),
                btn -> {
                    ModConfig.outline = !ModConfig.outline;
                    btn.setMessage(Text.literal("Outline: " + (ModConfig.outline ? "✔ ON" : "✘ OFF")));
                }
        ).dimensions(cx - 100, y, 200, 20).build());
        y += 28;

        // Done
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                btn -> close()
        ).dimensions(cx - 50, y, 100, 20).build());
    }

    private ColorSlider addRGB(int x, int y, String label, int value) {
        ColorSlider s = new ColorSlider(x, y, 96, label, value);
        addDrawableChild(s);
        return s;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);

        int cx = width / 2;

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer, "§e§lHitboxReveal §7Settings", cx, 7, 0xFFFFFF);

        // Section headings
        ctx.drawCenteredTextWithShadow(textRenderer, "§b── Colors ──", cx, 19, 0xFFFFFF);

        // Color row labels  (drawn just above each slider row)
        ctx.drawTextWithShadow(textRenderer, "§7Default §e(Normal)", cx - 152, labelDefault + 1, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "§7Close §c(≤3 blocks)", cx - 152, labelClose + 1, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "§7Crit §d(Airborne + full cooldown)", cx - 152, labelCrit + 1, 0xFFFFFF);

        ctx.drawCenteredTextWithShadow(textRenderer, "§b── Options ──", cx, labelSettings + 1, 0xFFFFFF);

        super.render(ctx, mx, my, delta);

        // Bottom-left info panel (like your reference image)
        int bx = 4, by = height - 30;
        ctx.fill(bx - 2, by - 2, bx + 140, by + 22, 0xAA000000);
        ctx.drawTextWithShadow(textRenderer, "§eHitboxReveal §7v1.0.0", bx, by,      0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "§7by §flwkSlick",          bx, by + 10, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "§7[G] Toggle  [B] Config",  bx, by + 20, 0xAAAAAA);
    }

    @Override
    public void close() {
        ModConfig.colorDefault = buildColor(defaultR, defaultG, defaultB);
        ModConfig.colorClose   = buildColor(closeR,   closeG,   closeB);
        ModConfig.colorCrit    = buildColor(critR,    critG,    critB);
        ModConfig.revealTicks  = durationSlider.getTicks();
        ModConfig.lineWidth    = thicknessSlider.getFloat();
        client.setScreen(parent);
    }

    private int buildColor(ColorSlider r, ColorSlider g, ColorSlider b) {
        return 0xFF000000 | (r.getInt() << 16) | (g.getInt() << 8) | b.getInt();
    }

    // ── Slider inner classes ─────────────────────────────────────────────────

    static class ColorSlider extends SliderWidget {
        ColorSlider(int x, int y, int width, String label, int value) {
            super(x, y, width, 20, Text.literal(label + ": " + value), value / 255.0);
        }
        @Override protected void updateMessage() {
            String label = getMessage().getString().split(":")[0];
            setMessage(Text.literal(label + ": " + getInt()));
        }
        @Override protected void applyValue() {}
        int getInt() { return (int)(value * 255); }
    }

    static class DurationSlider extends SliderWidget {
        private static final int[] STEPS = {20, 40, 60, 100, 200, 400};
        DurationSlider(int x, int y, int width, int currentTicks) {
            super(x, y, width, 20, Text.literal("Duration: " + currentTicks/20 + "s"), findStep(currentTicks));
        }
        private static double findStep(int ticks) {
            for (int i = 0; i < STEPS.length; i++) if (STEPS[i] == ticks) return i / (double)(STEPS.length - 1);
            return 2.0 / (STEPS.length - 1);
        }
        @Override protected void updateMessage() {
            setMessage(Text.literal("Duration: " + getTicks()/20 + "s"));
        }
        @Override protected void applyValue() {}
        int getTicks() { return STEPS[Math.round((float)(value * (STEPS.length - 1)))]; }
    }

    static class SimpleSlider extends SliderWidget {
        private final String label;
        private final float min, max;
        SimpleSlider(int x, int y, int width, String label, float current, float min, float max) {
            super(x, y, width, 20, Text.literal(label), (current - min) / (max - min));
            this.label = label; this.min = min; this.max = max;
            updateMessage();
        }
        @Override protected void updateMessage() {
            setMessage(Text.literal(label + ": " + String.format("%.2f", getFloat())));
        }
        @Override protected void applyValue() {}
        float getFloat() { return min + (float)value * (max - min); }
    }
}