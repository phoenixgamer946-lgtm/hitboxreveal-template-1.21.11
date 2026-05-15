package com.lwkslick.hitboxreveal.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PearlTrailRenderer {

    private static final RenderPipeline TRAIL_PIPELINE = RenderPipeline
            .builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation("pipeline/hitboxreveal_trail")
            .withDepthWrite(false)
            .withCull(false)
            .build();

    private static final RenderLayer TRAIL_LAYER = RenderLayer.of(
            "hitboxreveal_trail",
            RenderSetup.builder(TRAIL_PIPELINE).translucent().build()
    );

    private static final int CYLINDER_SEGMENTS = 12;

    public static void render(WorldRenderContext context,
                              Map<UUID, List<Vec3d>> trails) {
        if (!ModConfig.pearlTrailEnabled) return;

        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null) return;

        Vec3d cam = client.gameRenderer.getCamera().getCameraPos();
        MatrixStack matrices = context.matrices();
        VertexConsumer vc = consumers.getBuffer(TRAIL_LAYER);

        matrices.push();

        for (List<Vec3d> trail : trails.values()) {
            if (trail != null && trail.size() >= 2) {
                renderTrail(trail, cam, matrices, vc);
            }
        }

        matrices.pop();

        if (consumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

    private static void renderTrail(List<Vec3d> trail, Vec3d cam, MatrixStack matrices, VertexConsumer vc) {
        int sc = extractColor(ModConfig.pearlTrailColorStart);
        int ec = extractColor(ModConfig.pearlTrailColorEnd);
        float sr = ((sc >> 16) & 0xFF) / 255f;
        float sg = ((sc >> 8)  & 0xFF) / 255f;
        float sb = ((sc)       & 0xFF) / 255f;
        float er = ((ec >> 16) & 0xFF) / 255f;
        float eg = ((ec >> 8)  & 0xFF) / 255f;
        float eb = ((ec)       & 0xFF) / 255f;
        float radius = ModConfig.pearlTrailWidth * 0.05f;

        for (int i = 0; i < trail.size() - 1; i++) {
            float t0 = (float) i / (trail.size() - 1);
            float t1 = (float)(i + 1) / (trail.size() - 1);
            Vec3d start = trail.get(i).subtract(cam);
            Vec3d end   = trail.get(i + 1).subtract(cam);

            float r0 = sr + (er - sr) * t0, g0 = sg + (eg - sg) * t0, b0 = sb + (eb - sb) * t0;
            float r1 = sr + (er - sr) * t1, g1 = sg + (eg - sg) * t1, b1 = sb + (eb - sb) * t1;
            int a0 = (int)(ModConfig.pearlTrailOpacity * 255 * (1f - t0 * 0.3f));
            int a1 = (int)(ModConfig.pearlTrailOpacity * 255 * (1f - t1 * 0.3f));

            renderCylinder(start, end, matrices, vc,
                    (int)(r0*255), (int)(g0*255), (int)(b0*255), a0,
                    (int)(r1*255), (int)(g1*255), (int)(b1*255), a1,
                    radius);
        }
    }

    private static void renderCylinder(Vec3d start, Vec3d end, MatrixStack matrices, VertexConsumer vc,
                                       int r0, int g0, int b0, int a0,
                                       int r1, int g1, int b1, int a1,
                                       float radius) {
        Vec3d dir = end.subtract(start);
        if (dir.lengthSquared() < 1e-8) return;
        Vec3d d = dir.normalize();
        Vec3d p1 = findPerp(d);
        Vec3d p2 = d.crossProduct(p1).normalize();

        Vec3d[] sc = new Vec3d[CYLINDER_SEGMENTS];
        Vec3d[] ec = new Vec3d[CYLINDER_SEGMENTS];
        Vector3f[] normals = new Vector3f[CYLINDER_SEGMENTS];

        for (int i = 0; i < CYLINDER_SEGMENTS; i++) {
            double ang = 2.0 * Math.PI * i / CYLINDER_SEGMENTS;
            double cos = Math.cos(ang), sin = Math.sin(ang);
            Vec3d off = p1.multiply(cos * radius).add(p2.multiply(sin * radius));
            sc[i] = start.add(off);
            ec[i] = end.add(off);
            normals[i] = new Vector3f(
                    (float)(cos * p1.x + sin * p2.x),
                    (float)(cos * p1.y + sin * p2.y),
                    (float)(cos * p1.z + sin * p2.z)).normalize();
        }

        Matrix4f mat = matrices.peek().getPositionMatrix();
        for (int i = 0; i < CYLINDER_SEGMENTS; i++) {
            int ni = (i + 1) % CYLINDER_SEGMENTS;
            addVert(vc, mat, sc[i],  normals[i],  r0, g0, b0, a0);
            addVert(vc, mat, sc[ni], normals[ni], r0, g0, b0, a0);
            addVert(vc, mat, ec[ni], normals[ni], r1, g1, b1, a1);
            addVert(vc, mat, ec[i],  normals[i],  r1, g1, b1, a1);
        }
    }

    private static void addVert(VertexConsumer vc, Matrix4f mat, Vec3d pos, Vector3f normal, int r, int g, int b, int a) {
        vc.vertex(mat, (float)pos.x, (float)pos.y, (float)pos.z)
                .color(r, g, b, a)
                .normal(normal.x(), normal.y(), normal.z());
    }

    private static Vec3d findPerp(Vec3d v) {
        if (Math.abs(v.x) < 0.9)
            return new Vec3d(1, 0, 0).crossProduct(v).normalize();
        return new Vec3d(0, 1, 0).crossProduct(v).normalize();
    }

    private static int extractColor(int argb) {
        return argb & 0x00FFFFFF;
    }
}