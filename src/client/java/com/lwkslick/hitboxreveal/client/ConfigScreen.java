package com.lwkslick.hitboxreveal.client;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;

public class ConfigScreen {

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("HitboxReveal"))
                .save(ModConfig::save)

                // ── Options ─────────────────────────────────────────────────────
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Options"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enabled"))
                                .description(OptionDescription.of(Text.literal("Master toggle for HitboxReveal.")))
                                .binding(true, () -> ModConfig.enabled, v -> ModConfig.enabled = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Permanent"))
                                .description(OptionDescription.of(Text.literal("Show hitboxes permanently instead of fading after duration.")))
                                .binding(false, () -> ModConfig.permanent, v -> ModConfig.permanent = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .option(Option.<Integer>createBuilder()
                                .name(Text.literal("Duration"))
                                .description(OptionDescription.of(Text.literal("How long the hitbox stays visible after a hit (in ticks). 20 ticks = 1 second.")))
                                .binding(60, () -> ModConfig.revealTicks, v -> ModConfig.revealTicks = v)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(20, 2400).step(20))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Outline"))
                                .description(OptionDescription.of(Text.literal("Render the hitbox outline.")))
                                .binding(true, () -> ModConfig.outline, v -> ModConfig.outline = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .option(Option.<Float>createBuilder()
                                .name(Text.literal("Line width"))
                                .description(OptionDescription.of(Text.literal("Thickness of the hitbox outline.")))
                                .binding(2.0f, () -> ModConfig.lineWidth, v -> ModConfig.lineWidth = v)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(2.0f, 8.0f).step(0.5f))
                                .build())

                        .option(Option.<Float>createBuilder()
                                .name(Text.literal("Fill opacity"))
                                .description(OptionDescription.of(Text.literal("Opacity of the hitbox fill. 0 = invisible, 1 = fully opaque.")))
                                .binding(0.25f, () -> ModConfig.fillOpacity, v -> ModConfig.fillOpacity = v)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.05f).valueFormatter(f -> Text.literal(String.format("%.0f%%", f * 100))))
                                .build())

                        .option(Option.<Float>createBuilder()
                                .name(Text.literal("Close range threshold"))
                                .description(OptionDescription.of(Text.literal("Distance in blocks at which the close-range color activates.")))
                                .binding(3.0f, () -> ModConfig.closeRangeThreshold, v -> ModConfig.closeRangeThreshold = v)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(1.0f, 8.0f).step(0.1f).valueFormatter(f -> Text.literal(String.format("%.1f blocks", f))))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Eye height box"))
                                .description(OptionDescription.of(Text.literal("Render a separate box at the target's eye height.")))
                                .binding(false, () -> ModConfig.eyeHeightBox, v -> ModConfig.eyeHeightBox = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Look vector"))
                                .description(OptionDescription.of(Text.literal("Render a line showing where the target is looking.")))
                                .binding(false, () -> ModConfig.lookVector, v -> ModConfig.lookVector = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .option(Option.<Float>createBuilder()
                                .name(Text.literal("Look vector length"))
                                .description(OptionDescription.of(Text.literal("Length of the look vector line in blocks.")))
                                .binding(2.0f, () -> ModConfig.lookVectorLength, v -> ModConfig.lookVectorLength = v)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.5f, 10.0f).step(0.5f))
                                .build())

                        .option(Option.<Float>createBuilder()
                                .name(Text.literal("Look vector width"))
                                .description(OptionDescription.of(Text.literal("Thickness of the look vector line.")))
                                .binding(2.0f, () -> ModConfig.lookVectorWidth, v -> ModConfig.lookVectorWidth = v)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(2.0f, 8.0f).step(0.5f))
                                .build())

                        .build())

                // ── Colors ──────────────────────────────────────────────────────
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Colors"))

                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Default color"))
                                .description(OptionDescription.of(Text.literal("Hitbox color when target is marked normally.")))
                                .binding(new Color(ModConfig.colorDefault, true), () -> new Color(ModConfig.colorDefault, true), v -> ModConfig.colorDefault = v.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                                .build())

                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Close range color"))
                                .description(OptionDescription.of(Text.literal("Hitbox color when target is within close range threshold.")))
                                .binding(new Color(ModConfig.colorClose, true), () -> new Color(ModConfig.colorClose, true), v -> ModConfig.colorClose = v.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                                .build())

                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Crit color"))
                                .description(OptionDescription.of(Text.literal("Hitbox color when target is airborne with full attack cooldown (crit-ready).")))
                                .binding(new Color(ModConfig.colorCrit, true), () -> new Color(ModConfig.colorCrit, true), v -> ModConfig.colorCrit = v.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                                .build())

                        .build())

                // ── Gradient ─────────────────────────────────────────────────────
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Gradient"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Gradient enabled"))
                                .description(OptionDescription.of(Text.literal("Toggle the gradient fill on hitboxes.")))
                                .binding(true, () -> ModConfig.gradientEnabled, v -> ModConfig.gradientEnabled = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Gradient top color"))
                                .description(OptionDescription.of(Text.literal("Top color of the hitbox gradient. Used for all states unless per-state gradients is on.")))
                                .binding(new Color(ModConfig.colorGradientTop, true), () -> new Color(ModConfig.colorGradientTop, true), v -> ModConfig.colorGradientTop = v.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Per-state gradients"))
                                .description(OptionDescription.of(Text.literal("Use different gradient top colors for each hitbox state (default, close range, crit).")))
                                .binding(false, () -> ModConfig.perStateGradient, v -> ModConfig.perStateGradient = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Gradient top (default)"))
                                .description(OptionDescription.of(Text.literal("Top gradient color for the default state. Only used when per-state gradients is on.")))
                                .binding(new Color(ModConfig.colorGradientTop, true), () -> new Color(ModConfig.colorGradientTop, true), v -> ModConfig.colorGradientTop = v.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                                .build())

                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Gradient top (close range)"))
                                .description(OptionDescription.of(Text.literal("Top gradient color for the close range state. Only used when per-state gradients is on.")))
                                .binding(new Color(ModConfig.colorGradientTopClose, true), () -> new Color(ModConfig.colorGradientTopClose, true), v -> ModConfig.colorGradientTopClose = v.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                                .build())

                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Gradient top (crit)"))
                                .description(OptionDescription.of(Text.literal("Top gradient color for the crit state. Only used when per-state gradients is on.")))
                                .binding(new Color(ModConfig.colorGradientTopCrit, true), () -> new Color(ModConfig.colorGradientTopCrit, true), v -> ModConfig.colorGradientTopCrit = v.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                                .build())

                        .build())

                .build()
                .generateScreen(parent);
    }
}