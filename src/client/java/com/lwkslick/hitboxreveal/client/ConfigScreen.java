package com.lwkslick.hitboxreveal.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {

    private final Screen parent;

    // Collapsible color rows
    private ColorRow rowDefault, rowClose, rowCrit, rowGradientTop;
    private boolean expandedDefault = false;
    private boolean expandedClose   = false;
    private boolean expandedCrit    = false;
    private boolean expandedGradTop = false;

    // Option sliders
    private DurationSlider durationSlider;
    private LineWidthSlider lineWidthSlider;
    private FillOpacitySlider fillOpacitySlider;
    private CloseRangeSlider closeRangeSlider;
    private LookVectorLengthSlider lookVectorLengthSlider;
    private LookVectorWidthSlider lookVectorWidthSlider;

    // Toggle buttons we need refs to for conditional hiding
    private ButtonWidget permanentBtn, lookVectorBtn, outlineBtn, eyeBoxBtn;

    // All widgets that need layout on rebuild
    private final List<Runnable> layoutTasks = new ArrayList<>();

    public ConfigScreen(Screen parent) {
        super(Text.literal("HitboxReveal Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();
        layoutTasks.clear();
        buildLayout();
    }

    private void buildLayout() {
        int cx = width / 2;
        int[] y = {30};

        // ── Colors ──
        y[0] += 12;
        ctx_label(cx, y[0], "§b── Colors ──");
        y[0] += 12;

        rowDefault    = addColorRow("§7Default §e(Normal)",             ModConfig.colorDefault,    ModConfig.satDefault,    ModConfig.briDefault,    cx, y, expandedDefault);
        rowClose      = addColorRow("§7Close §c(≤ threshold)",          ModConfig.colorClose,      ModConfig.satClose,      ModConfig.briClose,      cx, y, expandedClose);
        rowCrit       = addColorRow("§7Crit §d(Airborne+full cooldown)", ModConfig.colorCrit,       ModConfig.satCrit,       ModConfig.briCrit,       cx, y, expandedCrit);
        rowGradientTop= addColorRow("§7Gradient §b(Top color)",         ModConfig.colorGradientTop,ModConfig.satGradientTop,ModConfig.briGradientTop,cx, y, expandedGradTop);

        // ── Options ──
        y[0] += 4;
        ctx_label(cx, y[0], "§b── Options ──");
        y[0] += 12;

        // Duration slider (hidden when permanent)
        durationSlider = new DurationSlider(cx - 100, y[0], 200, ModConfig.revealTicks);
        addDrawableChild(durationSlider);
        durationSlider.visible = !ModConfig.permanent;
        if (!ModConfig.permanent) y[0] += 24;

        // Permanent toggle
        permanentBtn = addDrawableChild(ButtonWidget.builder(
                Text.literal(ModConfig.permanent ? "§aPermanent: ON" : "§7Permanent: OFF"),
                btn -> {
                    ModConfig.permanent = !ModConfig.permanent;
                    btn.setMessage(Text.literal(ModConfig.permanent ? "§aPermanent: ON" : "§7Permanent: OFF"));
                    rebuild();
                }
        ).dimensions(cx - 100, y[0], 200, 20).build());
        y[0] += 24;

        // Close range threshold
        closeRangeSlider = new CloseRangeSlider(cx - 100, y[0], 200, ModConfig.closeRangeThreshold);
        addDrawableChild(closeRangeSlider);
        y[0] += 24;

        // Line width
        lineWidthSlider = new LineWidthSlider(cx - 100, y[0], 200, ModConfig.lineWidth);
        addDrawableChild(lineWidthSlider);
        y[0] += 24;

        // Fill opacity
        fillOpacitySlider = new FillOpacitySlider(cx - 100, y[0], 200, ModConfig.fillOpacity);
        addDrawableChild(fillOpacitySlider);
        y[0] += 24;

        // Outline toggle
        outlineBtn = addDrawableChild(ButtonWidget.builder(
                Text.literal("Outline: " + (ModConfig.outline ? "§a✔ ON" : "§c✘ OFF")),
                btn -> {
                    ModConfig.outline = !ModConfig.outline;
                    btn.setMessage(Text.literal("Outline: " + (ModConfig.outline ? "§a✔ ON" : "§c✘ OFF")));
                }
        ).dimensions(cx - 100, y[0], 200, 20).build());
        y[0] += 24;

        // Eye height box toggle
        eyeBoxBtn = addDrawableChild(ButtonWidget.builder(
                Text.literal("Eye Box: " + (ModConfig.eyeHeightBox ? "§a✔ ON" : "§c✘ OFF")),
                btn -> {
                    ModConfig.eyeHeightBox = !ModConfig.eyeHeightBox;
                    btn.setMessage(Text.literal("Eye Box: " + (ModConfig.eyeHeightBox ? "§a✔ ON" : "§c✘ OFF")));
                }
        ).dimensions(cx - 100, y[0], 200, 20).build());
        y[0] += 24;

        // Look vector toggle
        lookVectorBtn = addDrawableChild(ButtonWidget.builder(
                Text.literal("Look Vector: " + (ModConfig.lookVector ? "§a✔ ON" : "§c✘ OFF")),
                btn -> {
                    ModConfig.lookVector = !ModConfig.lookVector;
                    btn.setMessage(Text.literal("Look Vector: " + (ModConfig.lookVector ? "§a✔ ON" : "§c✘ OFF")));
                    rebuild();
                }
        ).dimensions(cx - 100, y[0], 200, 20).build());
        y[0] += 24;

        // Look vector sliders (hidden when look vector is OFF)
        lookVectorLengthSlider = new LookVectorLengthSlider(cx - 100, y[0], 200, ModConfig.lookVectorLength);
        lookVectorWidthSlider  = new LookVectorWidthSlider(cx - 100, y[0] + 24, 200, ModConfig.lookVectorWidth);
        addDrawableChild(lookVectorLengthSlider);
        addDrawableChild(lookVectorWidthSlider);
        lookVectorLengthSlider.visible = ModConfig.lookVector;
        lookVectorWidthSlider.visible  = ModConfig.lookVector;
        if (ModConfig.lookVector) y[0] += 48;

        y[0] += 4;

        // Done
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"), btn -> close()
        ).dimensions(cx - 50, y[0], 100, 20).build());

        // Social buttons
        int bx = 6, by = height - 24;
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

    /** Saves current slider values, then rebuilds layout (for conditional show/hide) */
    private void rebuild() {
        saveToConfig();
        init();
    }

    private void ctx_label(int cx, int y, String text) {
        // labels are drawn in render(); we just track the y via the int[] trick
    }

    // ── Color Row helper ─────────────────────────────────────────────────────

    private ColorRow addColorRow(String label, int argb, float sat, float bri, int cx, int[] y, boolean expanded) {
        ColorRow row = new ColorRow(label, argb, sat, bri);
        row.expanded = expanded;

        row.expandBtn = addDrawableChild(ButtonWidget.builder(
                Text.literal(row.expanded ? "▼" : "▶"),
                btn -> {
                    row.expanded = !row.expanded;
                    // persist before rebuild wipes the row
                    if (row == rowDefault)     expandedDefault = row.expanded;
                    else if (row == rowClose)  expandedClose   = row.expanded;
                    else if (row == rowCrit)   expandedCrit    = row.expanded;
                    else                       expandedGradTop = row.expanded;
                    btn.setMessage(Text.literal(row.expanded ? "▼" : "▶"));
                    rebuild();
                }
        ).dimensions(cx - 120, y[0], 16, 20).build());

        row.hueSlider = new HueSlider(cx - 100, y[0], 160, label, argb, sat, bri);
        addDrawableChild(row.hueSlider);
        row.labelY = y[0];
        y[0] += 24;

        if (row.expanded) {
            row.satSlider = new SatSlider(cx - 100, y[0], 160, sat);
            addDrawableChild(row.satSlider);
            y[0] += 24;
            row.briSlider = new BriSlider(cx - 100, y[0], 160, bri);
            addDrawableChild(row.briSlider);
            y[0] += 24;
        }

        return row;
    }

    static class ColorRow {
        String label;
        boolean expanded = false;
        int labelY;
        ButtonWidget expandBtn;
        HueSlider hueSlider;
        SatSlider satSlider;
        BriSlider briSlider;

        ColorRow(String label, int argb, float sat, float bri) {
            this.label = label;
        }

        int getArgb() {
            float h = hueSlider != null ? hueSlider.getHue() : 0;
            float s = satSlider != null ? satSlider.getSat() : 1f;
            float v = briSlider != null ? briSlider.getBri() : 1f;
            return hsvToArgb(h, s, v);
        }
        float getSat() { return satSlider != null ? satSlider.getSat() : 1f; }
        float getBri() { return briSlider != null ? briSlider.getBri() : 1f; }

        static int hsvToArgb(float h, float s, float v) {
            float c = v * s;
            float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
            float m = v - c;
            float r, g, b;
            int sector = (int)(h / 60) % 6;
            switch (sector) {
                case 0 -> { r=c; g=x; b=0; }
                case 1 -> { r=x; g=c; b=0; }
                case 2 -> { r=0; g=c; b=x; }
                case 3 -> { r=0; g=x; b=c; }
                case 4 -> { r=x; g=0; b=c; }
                default-> { r=c; g=0; b=x; }
            }
            return 0xFF000000
                    | ((int)((r+m)*255) << 16)
                    | ((int)((g+m)*255) << 8)
                    |  (int)((b+m)*255);
        }
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        int cx = width / 2;

        ctx.drawCenteredTextWithShadow(textRenderer, "§6§llwkSlick§e§l's HitboxReveal", cx, 5, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "§7Config Menu", cx, 16, 0xAAAAAA);
        ctx.drawCenteredTextWithShadow(textRenderer, "§b── Colors ──", cx, 30, 0xFFFFFF);

        // Draw color preview boxes and labels
        drawColorRowLabel(ctx, cx, rowDefault);
        drawColorRowLabel(ctx, cx, rowClose);
        drawColorRowLabel(ctx, cx, rowCrit);
        drawColorRowLabel(ctx, cx, rowGradientTop);

        // Options label — find it between color rows and duration
        int optY = rowGradientTop.labelY + (rowGradientTop.expanded ? 48 : 0) + 28;
        ctx.drawCenteredTextWithShadow(textRenderer, "§b── Options ──", cx, optY, 0xFFFFFF);

        super.render(ctx, mx, my, delta);
    }

    private void drawColorRowLabel(DrawContext ctx, int cx, ColorRow row) {
        if (row == null) return;
        // Color preview box
        int color = row.getArgb();
        int px = cx + 64, py = row.labelY;
        ctx.fill(px, py, px + 20, py + 20, 0xFF000000 | (color & 0x00FFFFFF));
        ctx.fill(px, py, px + 20, py + 1, 0xFFFFFFFF);
        ctx.fill(px, py + 19, px + 20, py + 20, 0xFFFFFFFF);
        ctx.fill(px, py, px + 1, py + 20, 0xFFFFFFFF);
        ctx.fill(px + 19, py, px + 20, py + 20, 0xFFFFFFFF);
    }

    // ── Save / Close ─────────────────────────────────────────────────────────

    private void saveToConfig() {
        if (rowDefault != null)     { ModConfig.colorDefault    = rowDefault.getArgb();    ModConfig.satDefault    = rowDefault.getSat();    ModConfig.briDefault    = rowDefault.getBri(); }
        if (rowClose != null)       { ModConfig.colorClose      = rowClose.getArgb();      ModConfig.satClose      = rowClose.getSat();      ModConfig.briClose      = rowClose.getBri(); }
        if (rowCrit != null)        { ModConfig.colorCrit       = rowCrit.getArgb();       ModConfig.satCrit       = rowCrit.getSat();       ModConfig.briCrit       = rowCrit.getBri(); }
        if (rowGradientTop != null) { ModConfig.colorGradientTop= rowGradientTop.getArgb();ModConfig.satGradientTop= rowGradientTop.getSat();ModConfig.briGradientTop= rowGradientTop.getBri(); }
        if (durationSlider != null)         ModConfig.revealTicks           = durationSlider.getTicks();
        if (closeRangeSlider != null)       ModConfig.closeRangeThreshold   = closeRangeSlider.getRange();
        if (lineWidthSlider != null)        ModConfig.lineWidth             = lineWidthSlider.getLineWidth();
        if (fillOpacitySlider != null)      ModConfig.fillOpacity           = fillOpacitySlider.getOpacity();
        if (lookVectorLengthSlider != null) ModConfig.lookVectorLength      = lookVectorLengthSlider.getLength();
        if (lookVectorWidthSlider != null)  ModConfig.lookVectorWidth       = lookVectorWidthSlider.getVectorWidth();
    }

    @Override
    public void close() {
        saveToConfig();
        ModConfig.save();
        assert client != null;
        client.setScreen(parent);
    }

    private void openUrl(String url) {
        Util.getOperatingSystem().open(URI.create(url));
    }

    // ── Sliders ───────────────────────────────────────────────────────────────

    static class HueSlider extends SliderWidget {
        private final String label;
        HueSlider(int x, int y, int width, String label, int argb, float sat, float bri) {
            super(x, y, width, 20, Text.literal(label), argbToHue(argb) / 360.0);
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
        float getHue() { return (float)(value * 360); }
        @Override protected void updateMessage() { setMessage(Text.literal(label.replaceAll("§.", "") + " Hue: " + (int)(value * 360) + "°")); }
        @Override protected void applyValue() {}
    }

    static class SatSlider extends SliderWidget {
        SatSlider(int x, int y, int width, float current) {
            super(x, y, width, 20, Text.literal(""), current);
            updateMessage();
        }
        float getSat() { return (float) value; }
        @Override protected void updateMessage() { setMessage(Text.literal(String.format("Saturation: %.0f%%", value * 100))); }
        @Override protected void applyValue() {}
    }

    static class BriSlider extends SliderWidget {
        BriSlider(int x, int y, int width, float current) {
            super(x, y, width, 20, Text.literal(""), current);
            updateMessage();
        }
        float getBri() { return (float) value; }
        @Override protected void updateMessage() { setMessage(Text.literal(String.format("Brightness: %.0f%%", value * 100))); }
        @Override protected void applyValue() {}
    }

    static class DurationSlider extends SliderWidget {
        private static final int[] STEPS = { 20,40,60,80,100,120,140,160,200,240,300,400,600,800,1200,1600,2000,2400 };
        DurationSlider(int x, int y, int width, int currentTicks) {
            super(x, y, width, 20, Text.literal(""), findStep(currentTicks));
            updateMessage();
        }
        private static double findStep(int ticks) {
            for (int i = 0; i < STEPS.length; i++) if (STEPS[i] == ticks) return i / (double)(STEPS.length - 1);
            return 2.0 / (STEPS.length - 1);
        }
        @Override protected void updateMessage() {
            int t = getTicks();
            setMessage(Text.literal("Duration: " + (t % 20 == 0 ? t/20 + "s" : t + " ticks")));
        }
        @Override protected void applyValue() {}
        int getTicks() { return STEPS[Math.min(Math.round((float)(value * (STEPS.length - 1))), STEPS.length - 1)]; }
    }

    static class CloseRangeSlider extends SliderWidget {
        private static final float MIN = 1f, MAX = 8f;
        CloseRangeSlider(int x, int y, int width, float current) {
            super(x, y, width, 20, Text.literal(""), (current - MIN) / (MAX - MIN));
            updateMessage();
        }
        float getRange() { return MIN + (float)(value * (MAX - MIN)); }
        @Override protected void updateMessage() { setMessage(Text.literal(String.format("Close Range: %.1f blocks", getRange()))); }
        @Override protected void applyValue() {}
    }

    static class LineWidthSlider extends SliderWidget {
        private static final float MIN = 2f, MAX = 8f;
        LineWidthSlider(int x, int y, int width, float current) {
            super(x, y, width, 20, Text.literal(""), (current - MIN) / (MAX - MIN));
            updateMessage();
        }
        float getLineWidth() { return MIN + (float)(value * (MAX - MIN)); }
        @Override protected void updateMessage() { setMessage(Text.literal(String.format("Line Width: %.1f", getLineWidth()))); }
        @Override protected void applyValue() {}
    }

    static class FillOpacitySlider extends SliderWidget {
        FillOpacitySlider(int x, int y, int width, float current) {
            super(x, y, width, 20, Text.literal(""), current);
            updateMessage();
        }
        float getOpacity() { return (float) value; }
        @Override protected void updateMessage() { setMessage(Text.literal(String.format("Fill Opacity: %.0f%%", value * 100))); }
        @Override protected void applyValue() {}
    }

    static class LookVectorLengthSlider extends SliderWidget {
        private static final float MIN = 0.5f, MAX = 10f;
        LookVectorLengthSlider(int x, int y, int width, float current) {
            super(x, y, width, 20, Text.literal(""), (current - MIN) / (MAX - MIN));
            updateMessage();
        }
        float getLength() { return MIN + (float)(value * (MAX - MIN)); }
        @Override protected void updateMessage() { setMessage(Text.literal(String.format("Vector Length: %.1f", getLength()))); }
        @Override protected void applyValue() {}
    }

    static class LookVectorWidthSlider extends SliderWidget {
        private static final float MIN = 2f, MAX = 8f;
        LookVectorWidthSlider(int x, int y, int width, float current) {
            super(x, y, width, 20, Text.literal(""), (current - MIN) / (MAX - MIN));
            updateMessage();
        }
        public float getVectorWidth() { return MIN + (float)(value * (MAX - MIN)); }
        @Override protected void updateMessage() { setMessage(Text.literal(String.format("Vector Width: %.1f", getVectorWidth()))); }
        @Override protected void applyValue() {}
    }
}