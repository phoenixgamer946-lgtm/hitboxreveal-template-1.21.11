package com.lwkslick.hitboxreveal.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import net.minecraft.client.MinecraftClient;

public class HitboxRenderer {

    public static void renderBox(WorldRenderContext context, PlayerEntity target, int argbColor) {
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        MatrixStack matrices = context.matrices();
        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();

        Box box = target.getBoundingBox();

        float r = ((argbColor >> 16) & 0xFF) / 255f;
        float g = ((argbColor >> 8)  & 0xFF) / 255f;
        float b = ((argbColor)       & 0xFF) / 255f;
        float a = ((argbColor >> 24) & 0xFF) / 255f;

        matrices.push();
        matrices.translate(box.minX - cam.x, box.minY - cam.y, box.minZ - cam.z);

        Matrix4f mat = matrices.peek().getPositionMatrix();
        float dx = (float)(box.maxX - box.minX);
        float dy = (float)(box.maxY - box.minY);
        float dz = (float)(box.maxZ - box.minZ);

        VertexConsumer buf = consumers.getBuffer(RenderLayers.LINES);
        drawEdges(buf, mat, dx, dy, dz, r, g, b, a);

        matrices.pop();
    }

    private static void drawEdges(VertexConsumer buf, Matrix4f mat,
                                  float dx, float dy, float dz,
                                  float r, float g, float b, float a) {
        line(buf, mat, 0,  0,  0,  dx, 0,  0,  r, g, b, a);
        line(buf, mat, dx, 0,  0,  dx, 0,  dz, r, g, b, a);
        line(buf, mat, dx, 0,  dz, 0,  0,  dz, r, g, b, a);
        line(buf, mat, 0,  0,  dz, 0,  0,  0,  r, g, b, a);
        line(buf, mat, 0,  dy, 0,  dx, dy, 0,  r, g, b, a);
        line(buf, mat, dx, dy, 0,  dx, dy, dz, r, g, b, a);
        line(buf, mat, dx, dy, dz, 0,  dy, dz, r, g, b, a);
        line(buf, mat, 0,  dy, dz, 0,  dy, 0,  r, g, b, a);
        line(buf, mat, 0,  0,  0,  0,  dy, 0,  r, g, b, a);
        line(buf, mat, dx, 0,  0,  dx, dy, 0,  r, g, b, a);
        line(buf, mat, dx, 0,  dz, dx, dy, dz, r, g, b, a);
        line(buf, mat, 0,  0,  dz, 0,  dy, dz, r, g, b, a);
    }

    private static void line(VertexConsumer buf, Matrix4f mat,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        float nx = x2 - x1, ny = y2 - y1, nz = z2 - z1;
        float len = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
        if (len == 0) len = 1;
        nx /= len; ny /= len; nz /= len;
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz).lineWidth(ModConfig.lineWidth);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a).normal(nx, ny, nz).lineWidth(ModConfig.lineWidth);
    }
}