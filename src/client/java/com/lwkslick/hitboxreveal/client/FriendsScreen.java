package com.lwkslick.hitboxreveal.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class FriendsScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget inputField;
    private static final int ENTRY_HEIGHT = 24;
    private static final int LIST_TOP = 80;

    public FriendsScreen(Screen parent) {
        super(Text.literal("Friends (Ignore List)"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        inputField = new TextFieldWidget(textRenderer, width / 2 - 100, 45, 180, 20, Text.literal(""));
        inputField.setMaxLength(64);
        inputField.setPlaceholder(Text.literal("Enter username..."));
        addDrawableChild(inputField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> {
            String name = inputField.getText().trim();
            if (!name.isEmpty() && !ModConfig.friends.contains(name)) {
                ModConfig.friends.add(name);
                ModConfig.save();
                inputField.setText("");
                rebuildList();
            }
        }).dimensions(width / 2 + 84, 45, 40, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> client.setScreen(parent))
                .dimensions(width / 2 - 50, height - 30, 100, 20).build());

        rebuildList();
    }

    private void rebuildList() {
        // Remove all existing remove-buttons (re-init handles this via clearChildren indirectly)
        // We rebuild by clearing and re-adding — safest approach
        clearChildren();

        // Re-add static widgets
        inputField = new TextFieldWidget(textRenderer, width / 2 - 100, 45, 180, 20, Text.literal(""));
        inputField.setMaxLength(64);
        inputField.setPlaceholder(Text.literal("Enter username..."));
        addDrawableChild(inputField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> {
            String name = inputField.getText().trim();
            if (!name.isEmpty() && !ModConfig.friends.contains(name)) {
                ModConfig.friends.add(name);
                ModConfig.save();
                inputField.setText("");
                rebuildList();
            }
        }).dimensions(width / 2 + 84, 45, 40, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> client.setScreen(parent))
                .dimensions(width / 2 - 50, height - 30, 100, 20).build());

        // Add a remove button per entry
        for (int i = 0; i < ModConfig.friends.size(); i++) {
            final int index = i;
            int y = LIST_TOP + i * ENTRY_HEIGHT;
            addDrawableChild(ButtonWidget.builder(Text.literal("✗"), btn -> {
                ModConfig.friends.remove(index);
                ModConfig.save();
                rebuildList();
            }).dimensions(width / 2 + 90, y, 20, 18).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer,
                Text.literal("Players in this list will never have their hitbox revealed."),
                width / 2 - 160, 30, 0xAAAAAA);

        for (int i = 0; i < ModConfig.friends.size(); i++) {
            int y = LIST_TOP + i * ENTRY_HEIGHT;
            context.fill(width / 2 - 120, y, width / 2 + 90, y + ENTRY_HEIGHT - 2, 0x22FFFFFF);
            context.drawTextWithShadow(textRenderer, Text.literal(ModConfig.friends.get(i)),
                    width / 2 - 110, y + 6, 0xFFFFFF);
        }
    }
}