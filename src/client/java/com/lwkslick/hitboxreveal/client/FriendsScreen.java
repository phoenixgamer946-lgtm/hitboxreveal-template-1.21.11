package com.lwkslick.hitboxreveal.client;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class FriendsScreen {

    public static Screen create(Screen parent) {
        var builder = YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Friends (Ignore List)"))
                .save(ModConfig::save);

        var category = ConfigCategory.createBuilder()
                .name(Text.literal("Friends"))
                .option(LabelOption.create(Text.literal("§7Players in this list will never have their hitbox revealed.")))
                .option(Option.<String>createBuilder()
                        .name(Text.literal("Add player"))
                        .description(OptionDescription.of(Text.literal("Type a username and press Add.")))
                        .binding("", () -> "", v -> {
                            String name = v.trim();
                            if (!name.isEmpty() && !ModConfig.friends.contains(name)) {
                                ModConfig.friends.add(name);
                                ModConfig.save();
                            }
                        })
                        .controller(opt -> StringControllerBuilder.create(opt))
                        .build());

        for (int i = 0; i < ModConfig.friends.size(); i++) {
            final int index = i;
            String name = ModConfig.friends.get(i);
            category.option(ButtonOption.createBuilder()
                    .name(Text.literal("§c[Remove] §f" + name))
                    .text(Text.literal("✗"))
                    .action((screen, opt) -> {
                        ModConfig.friends.remove(index);
                        ModConfig.save();
                        screen.close();
                    })
                    .build());
        }

        return builder.category(category.build()).build().generateScreen(parent);
    }
}