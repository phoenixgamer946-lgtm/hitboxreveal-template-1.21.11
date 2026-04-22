package com.lwkslick.hitboxreveal.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class HitboxRenderer {

    private static final RenderPipeline FILL_PIPELINE = RenderPipeline
            .builder(new RenderPipeline.Snippet[]{RenderPipelines.POSITION_COLOR_SNIPPET})
            .withLocation("pipeline/hitboxreveal_fill")
            .withDepthWrite(false)
            .withCull(false)
            .build();

    private static final RenderLayer FILL_LAYER = RenderLayer.of(
            "hitboxreveal_fill",
            RenderSetup.builder(FILL_PIPELINE).translucent().build()
    );

    public static void renderBox(WorldRenderContext context, PlayerEntity target, int argbColor, float alpha) {
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        MatrixStack matrices = context.matrices();
        Vec3d cam = client.gameRenderer.getCamera().getCameraPos();

        float tickProgress = client.getRenderTickCounter().getTickProgress(false);
        Vec3d lerpedPos = target.getLerpedPos(tickProgress);
        Vec3d entityPos = target.getEntityPos();
        Vec3d offset = lerpedPos.subtract(entityPos);
        Box box = target.getBoundingBox().offset(offset).offset(-cam.x, -cam.y, -cam.z);

        double dist = Math.sqrt(
                (box.minX + box.maxX) * 0.5 * (box.minX + box.maxX) * 0.5 +
                        (box.minY + box.maxY) * 0.5 * (box.minY + box.maxY) * 0.5 +
                        (box.minZ + box.maxZ) * 0.5 * (box.minZ + box.maxZ) * 0.5
        );
        float scaledLw = (float) Math.max(1.0f, ModConfig.lineWidth / (float)(dist * 0.3));

        float r = ((argbColor >> 16) & 0xFF) / 255f;
        float g = ((argbColor >> 8)  & 0xFF) / 255f;
        float b = ((argbColor)       & 0xFF) / 255f;
        float fa = ModConfig.fillOpacity * alpha;

        int topArgb = ModConfig.gradientEnabled
                ? (ModConfig.perStateGradient ? resolveGradientTop(argbColor) : ModConfig.colorGradientTop)
                : argbColor;
        float r2 = ((topArgb >> 16) & 0xFF) / 255f;
        float g2 = ((topArgb >> 8)  & 0xFF) / 255f;
        float b2 = ((topArgb)       & 0xFF) / 255f;

        matrices.push();

        // Fill
        if (fa > 0.01f) {
            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer fill = consumers.getBuffer(FILL_LAYER);
            for (Direction dir : Direction.values()) {
                boolean isTop = (dir == Direction.UP);
                float cr = isTop ? r2 : r;
                float cg = isTop ? g2 : g;
                float cb = isTop ? b2 : b;
                drawSide(mat, fill, dir,
                        (float)box.minX, (float)box.minY, (float)box.minZ,
                        (float)box.maxX, (float)box.maxY, (float)box.maxZ,
                        r, g, b, fa,
                        cr, cg, cb, fa);
            }
        }

        // Outline
        if (ModConfig.outline) {
            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer buf = consumers.getBuffer(RenderLayers.LINES);
            if (ModConfig.cornerOnly) {
                drawCorners(buf, mat,
                        (float)box.minX, (float)box.minY, (float)box.minZ,
                        (float)box.maxX, (float)box.maxY, (float)box.maxZ,
                        r, g, b, alpha, r2, g2, b2, alpha, scaledLw);
            } else {
                drawEdges(buf, mat,
                        (float)box.minX, (float)box.minY, (float)box.minZ,
                        (float)box.maxX, (float)box.maxY, (float)box.maxZ,
                        r, g, b, alpha, r2, g2, b2, alpha, scaledLw);
            }
        }

        // Eye height box
        if (ModConfig.eyeHeightBox) {
            double eyeY = box.minY + target.getStandingEyeHeight();
            double expand = 1.0E-4;
            Box eyeBox = new Box(
                    box.minX - expand, eyeY - 0.01, box.minZ - expand,
                    box.maxX + expand, eyeY + 0.01, box.maxZ + expand);
            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer buf = consumers.getBuffer(RenderLayers.LINES);
            drawEdges(buf, mat,
                    (float)eyeBox.minX, (float)eyeBox.minY, (float)eyeBox.minZ,
                    (float)eyeBox.maxX, (float)eyeBox.maxY, (float)eyeBox.maxZ,
                    r, g, b, alpha, r, g, b, alpha, scaledLw);
        }

        // Look vector
        if (ModConfig.lookVector) {
            Vec3d eyePos = lerpedPos.add(0, target.getStandingEyeHeight(), 0).subtract(cam);
            Vec3d lookDir = target.getRotationVec(tickProgress);
            Vec3d arrowEnd = eyePos.add(lookDir.multiply(ModConfig.lookVectorLength));
            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer buf = consumers.getBuffer(RenderLayers.LINES);
            float lw = ModConfig.lookVectorWidth;
            drawLine(buf, mat,
                    (float)eyePos.x, (float)eyePos.y, (float)eyePos.z,
                    (float)arrowEnd.x, (float)arrowEnd.y, (float)arrowEnd.z,
                    r, g, b, alpha, lw);
            Vec3d dir = lookDir.normalize();
            Quaternionf rot = new Quaternionf().rotationTo(new Vector3f(1, 0, 0), dir.toVector3f());
            float size = 0.2f;
            Vector3f[] fins = {
                    rot.transform(new Vector3f(-size, size, 0)),
                    rot.transform(new Vector3f(-size, 0, size)),
                    rot.transform(new Vector3f(-size, -size, 0)),
                    rot.transform(new Vector3f(-size, 0, -size))
            };
            for (Vector3f fin : fins) {
                drawLine(buf, mat,
                        (float)arrowEnd.x + fin.x, (float)arrowEnd.y + fin.y, (float)arrowEnd.z + fin.z,
                        (float)arrowEnd.x, (float)arrowEnd.y, (float)arrowEnd.z,
                        r, g, b, alpha, lw);
            }
        }

        matrices.pop();
    }

    public static void renderRangeCircle(WorldRenderContext context, PlayerEntity player) {
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        MatrixStack matrices = context.matrices();
        Vec3d cam = client.gameRenderer.getCamera().getCameraPos();

        float tickProgress = client.getRenderTickCounter().getTickProgress(false);
        Vec3d pos = player.getLerpedPos(tickProgress).subtract(cam);

        int argb = ModConfig.colorRangeIndicator;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8)  & 0xFF) / 255f;
        float b = ((argb)       & 0xFF) / 255f;
        float radius = ModConfig.closeRangeThreshold;
        int segments = 64;

        matrices.push();
        Matrix4f mat = matrices.peek().getPositionMatrix();
        VertexConsumer buf = consumers.getBuffer(RenderLayers.LINES);

        float y = (float)(pos.y);
        for (int i = 0; i < segments; i++) {
            double a1 = 2 * Math.PI * i / segments;
            double a2 = 2 * Math.PI * (i + 1) / segments;
            float x1 = (float)(pos.x + Math.cos(a1) * radius);
            float z1 = (float)(pos.z + Math.sin(a1) * radius);
            float x2 = (float)(pos.x + Math.cos(a2) * radius);
            float z2 = (float)(pos.z + Math.sin(a2) * radius);
            drawLine(buf, mat, x1, y, z1, x2, y, z2, r, g, b, 1.0f, 1.0f);
        }

        matrices.pop();
    }

    private static void drawSide(Matrix4f mat, VertexConsumer buf, Direction side,
                                 float minX, float minY, float minZ,
                                 float maxX, float maxY, float maxZ,
                                 float r, float g, float b, float a,
                                 float r2, float g2, float b2, float a2) {
        switch (side) {
            case DOWN  -> { buf.vertex((Matrix4fc)mat,minX,minY,minZ).color(r,g,b,a);     buf.vertex((Matrix4fc)mat,maxX,minY,minZ).color(r,g,b,a);     buf.vertex((Matrix4fc)mat,maxX,minY,maxZ).color(r,g,b,a);     buf.vertex((Matrix4fc)mat,minX,minY,maxZ).color(r,g,b,a);     }
            case UP    -> { buf.vertex((Matrix4fc)mat,minX,maxY,minZ).color(r2,g2,b2,a2); buf.vertex((Matrix4fc)mat,minX,maxY,maxZ).color(r2,g2,b2,a2); buf.vertex((Matrix4fc)mat,maxX,maxY,maxZ).color(r2,g2,b2,a2); buf.vertex((Matrix4fc)mat,maxX,maxY,minZ).color(r2,g2,b2,a2); }
            case NORTH -> { buf.vertex((Matrix4fc)mat,minX,minY,minZ).color(r,g,b,a);     buf.vertex((Matrix4fc)mat,minX,maxY,minZ).color(r2,g2,b2,a2); buf.vertex((Matrix4fc)mat,maxX,maxY,minZ).color(r2,g2,b2,a2); buf.vertex((Matrix4fc)mat,maxX,minY,minZ).color(r,g,b,a);     }
            case SOUTH -> { buf.vertex((Matrix4fc)mat,minX,minY,maxZ).color(r,g,b,a);     buf.vertex((Matrix4fc)mat,maxX,minY,maxZ).color(r,g,b,a);     buf.vertex((Matrix4fc)mat,maxX,maxY,maxZ).color(r2,g2,b2,a2); buf.vertex((Matrix4fc)mat,minX,maxY,maxZ).color(r2,g2,b2,a2); }
            case WEST  -> { buf.vertex((Matrix4fc)mat,minX,minY,minZ).color(r,g,b,a);     buf.vertex((Matrix4fc)mat,minX,minY,maxZ).color(r,g,b,a);     buf.vertex((Matrix4fc)mat,minX,maxY,maxZ).color(r2,g2,b2,a2); buf.vertex((Matrix4fc)mat,minX,maxY,minZ).color(r2,g2,b2,a2); }
            case EAST  -> { buf.vertex((Matrix4fc)mat,maxX,minY,minZ).color(r,g,b,a);     buf.vertex((Matrix4fc)mat,maxX,maxY,minZ).color(r2,g2,b2,a2); buf.vertex((Matrix4fc)mat,maxX,maxY,maxZ).color(r2,g2,b2,a2); buf.vertex((Matrix4fc)mat,maxX,minY,maxZ).color(r,g,b,a);     }
        }
    }

    private static void drawEdges(VertexConsumer buf, Matrix4f mat,
                                  float minX, float minY, float minZ,
                                  float maxX, float maxY, float maxZ,
                                  float r, float g, float b, float a,
                                  float r2, float g2, float b2, float a2,
                                  float lw) {
        line(buf,mat, minX,minY,minZ, maxX,minY,minZ, r,g,b,a, r,g,b,a, lw);
        line(buf,mat, maxX,minY,minZ, maxX,minY,maxZ, r,g,b,a, r,g,b,a, lw);
        line(buf,mat, maxX,minY,maxZ, minX,minY,maxZ, r,g,b,a, r,g,b,a, lw);
        line(buf,mat, minX,minY,maxZ, minX,minY,minZ, r,g,b,a, r,g,b,a, lw);
        line(buf,mat, minX,maxY,minZ, maxX,maxY,minZ, r2,g2,b2,a2, r2,g2,b2,a2, lw);
        line(buf,mat, maxX,maxY,minZ, maxX,maxY,maxZ, r2,g2,b2,a2, r2,g2,b2,a2, lw);
        line(buf,mat, maxX,maxY,maxZ, minX,maxY,maxZ, r2,g2,b2,a2, r2,g2,b2,a2, lw);
        line(buf,mat, minX,maxY,maxZ, minX,maxY,minZ, r2,g2,b2,a2, r2,g2,b2,a2, lw);
        line(buf,mat, minX,minY,minZ, minX,maxY,minZ, r,g,b,a, r2,g2,b2,a2, lw);
        line(buf,mat, maxX,minY,minZ, maxX,maxY,minZ, r,g,b,a, r2,g2,b2,a2, lw);
        line(buf,mat, maxX,minY,maxZ, maxX,maxY,maxZ, r,g,b,a, r2,g2,b2,a2, lw);
        line(buf,mat, minX,minY,maxZ, minX,maxY,maxZ, r,g,b,a, r2,g2,b2,a2, lw);
    }

    private static void drawCorners(VertexConsumer buf, Matrix4f mat,
                                    float minX, float minY, float minZ,
                                    float maxX, float maxY, float maxZ,
                                    float r, float g, float b, float a,
                                    float r2, float g2, float b2, float a2,
                                    float lw) {
        float cl = ModConfig.cornerLength;
        float xLen = Math.min(cl, (maxX - minX) / 2f);
        float yLen = Math.min(cl, (maxY - minY) / 2f);
        float zLen = Math.min(cl, (maxZ - minZ) / 2f);

        // 8 corners — each has 3 lines (x, y, z axis)
        float[][] corners = {
                {minX, minY, minZ},
                {maxX, minY, minZ},
                {minX, maxY, minZ},
                {maxX, maxY, minZ},
                {minX, minY, maxZ},
                {maxX, minY, maxZ},
                {minX, maxY, maxZ},
                {maxX, maxY, maxZ},
        };
        float[] xDirs = { 1, -1,  1, -1,  1, -1,  1, -1};
        float[] yDirs = { 1,  1, -1, -1,  1,  1, -1, -1};
        float[] zDirs = { 1,  1,  1,  1, -1, -1, -1, -1};

        for (int i = 0; i < 8; i++) {
            float cx = corners[i][0];
            float cy = corners[i][1];
            float cz = corners[i][2];
            boolean isTop = (cy == maxY);
            float cr = isTop ? r2 : r;
            float cg = isTop ? g2 : g;
            float cb = isTop ? b2 : b;
            float ca = isTop ? a2 : a;

            line(buf, mat, cx, cy, cz, cx + xDirs[i]*xLen, cy, cz, cr,cg,cb,ca, cr,cg,cb,ca, lw);
            line(buf, mat, cx, cy, cz, cx, cy + yDirs[i]*yLen, cz, r,g,b,a, r2,g2,b2,a2, lw);
            line(buf, mat, cx, cy, cz, cx, cy, cz + zDirs[i]*zLen, cr,cg,cb,ca, cr,cg,cb,ca, lw);
        }
    }

    private static void line(VertexConsumer buf, Matrix4f mat,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r1, float g1, float b1, float a1,
                             float r2, float g2, float b2, float a2,
                             float lw) {
        float nx = x2-x1, ny = y2-y1, nz = z2-z1;
        float len = (float)Math.sqrt(nx*nx + ny*ny + nz*nz);
        if (len == 0) len = 1;
        nx/=len; ny/=len; nz/=len;
        buf.vertex(mat,x1,y1,z1).color(r1,g1,b1,a1).lineWidth(lw).normal(nx,ny,nz);
        buf.vertex(mat,x2,y2,z2).color(r2,g2,b2,a2).lineWidth(lw).normal(nx,ny,nz);
    }

    private static void drawLine(VertexConsumer buf, Matrix4f mat,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float r, float g, float b, float a, float lw) {
        line(buf, mat, x1,y1,z1, x2,y2,z2, r,g,b,a, r,g,b,a, lw);
    }

    private static int resolveGradientTop(int argbColor) {
        if ((argbColor & 0x00FFFFFF) == (ModConfig.colorClose & 0x00FFFFFF)) return ModConfig.colorGradientTopClose;
        if ((argbColor & 0x00FFFFFF) == (ModConfig.colorCrit  & 0x00FFFFFF)) return ModConfig.colorGradientTopCrit;
        return ModConfig.colorGradientTop;
    }
}