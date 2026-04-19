package com.lwkslick.hitboxreveal.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class HitboxRenderer {

    public static void renderBox(WorldRenderContext context, Box box, float r, float g, float b) {
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d cam = client.gameRenderer.getCamera().getPos();

        MatrixStack matrices = new MatrixStack();
        matrices.translate(box.minX - cam.x, box.minY - cam.y, box.minZ - cam.z);
        Matrix4f mat = matrices.peek().getPositionMatrix();

        double dx = box.maxX - box.minX;
        double dy = box.maxY - box.minY;
        double dz = box.maxZ - box.minZ;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        drawBox(buf, mat, (float)dx, (float)dy, (float)dz, r, g, b);

        BuiltBuffer built = buf.build();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
            built.close();
        }
    }

    private static void drawBox(BufferBuilder buf, Matrix4f mat,
                                float dx, float dy, float dz,
                                float r, float g, float b) {
        line(buf, mat, 0, 0, 0, dx, 0, 0, r, g, b);
        line(buf, mat, dx, 0, 0, dx, 0, dz, r, g, b);
        line(buf, mat, dx, 0, dz, 0, 0, dz, r, g, b);
        line(buf, mat, 0, 0, dz, 0, 0, 0, r, g, b);
        line(buf, mat, 0, dy, 0, dx, dy, 0, r, g, b);
        line(buf, mat, dx, dy, 0, dx, dy, dz, r, g, b);
        line(buf, mat, dx, dy, dz, 0, dy, dz, r, g, b);
        line(buf, mat, 0, dy, dz, 0, dy, 0, r, g, b);
        line(buf, mat, 0, 0, 0, 0, dy, 0, r, g, b);
        line(buf, mat, dx, 0, 0, dx, dy, 0, r, g, b);
        line(buf, mat, dx, 0, dz, dx, dy, dz, r, g, b);
        line(buf, mat, 0, 0, dz, 0, dy, dz, r, g, b);
    }

    private static void line(BufferBuilder buf, Matrix4f mat,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float b) {
        buf.vertex(mat, x1, y1, z1).color(r, g, b, 1.0f);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, 1.0f);
    }
}