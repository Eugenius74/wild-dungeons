package com.danielkkrafft.wilddungeons.api.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30C;

import java.util.*;

@EventBusSubscriber(value = Dist.CLIENT)
public class LevelRenderHandler {

    public static Matrix4f MATRIX4F = null;
    public static MultiBufferSource.BufferSource DELAYED_RENDER = null;
    public static RenderTarget DEPTH_CACHE;
    public static float FOG_START = 0;

    @SubscribeEvent
    public static void onLevelRender(RenderLevelStageEvent event) {
        if (!ShadersIntegration.shouldApply()) {
            standardDelayedRender(event);
        } else {
            standardDelayedRender(event); //Fallback In Case solution is one day found
        }
    }

    public static void standardDelayedRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            copyDepthBuffer(DEPTH_CACHE);
            Matrix4f last = new Matrix4f(RenderSystem.getModelViewMatrix());
            if (MATRIX4F != null) RenderSystem.getModelViewMatrix().set(MATRIX4F);
            for (RenderType renderType : DelayedRenderTypes.TRANSLUCENT_ENTITY) endBatch(renderType);
            RenderSystem.getModelViewMatrix().set(last);
            for (RenderType renderType : DelayedRenderTypes.TRANSLUCENT_PARTICLES) endBatch(renderType);
            if (MATRIX4F != null) RenderSystem.getModelViewMatrix().set(MATRIX4F);
            for (RenderType renderType : DelayedRenderTypes.ADDITIVE_ENTITY) endBatch(renderType);
            RenderSystem.getModelViewMatrix().set(last);
            for (RenderType renderType : DelayedRenderTypes.ADDITIVE_PARTICLES) endBatch(renderType);
        }
    }

    public static void shadersDelayedRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            copyDepthBuffer(DEPTH_CACHE);
            RenderSystem.setShaderFogStart(FOG_START);
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().identity();
            if (MATRIX4F != null) RenderSystem.getModelViewStack().mul(MATRIX4F);
            RenderSystem.applyModelViewMatrix();
            for (RenderType renderType : DelayedRenderTypes.TRANSLUCENT_PARTICLES) endBatch(renderType);
            RenderSystem.getModelViewStack().popMatrix();
            RenderSystem.applyModelViewMatrix();
            for (RenderType renderType : DelayedRenderTypes.TRANSLUCENT_ENTITY) endBatch(renderType);
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().identity();
            if (MATRIX4F != null) RenderSystem.getModelViewStack().mul(MATRIX4F);
            RenderSystem.applyModelViewMatrix();
            for (RenderType renderType : DelayedRenderTypes.ADDITIVE_PARTICLES) endBatch(renderType);
            RenderSystem.getModelViewStack().popMatrix();
            RenderSystem.applyModelViewMatrix();
            for (RenderType renderType : DelayedRenderTypes.ADDITIVE_ENTITY) endBatch(renderType);
            FogRenderer.setupNoFog();
        }
    }

    public static void endBatch(RenderType renderType) {
        getDelayedRender().endBatch(renderType);
    }

    public static void copyDepthBuffer(RenderTarget tempRenderTarget) {
        setupDepthBuffer();
        enableStencil();
        if (tempRenderTarget == null) return;
        RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        tempRenderTarget.copyDepthFrom(mainRenderTarget);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mainRenderTarget.frameBufferId);
    }

    public static void setupDepthBuffer() {
        if (DEPTH_CACHE == null) {
            DEPTH_CACHE = new TextureTarget(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height, true, Minecraft.ON_OSX);
        }
    }

    public static void enableStencil() {
        if (Minecraft.getInstance().getMainRenderTarget().isStencilEnabled()) {
            DEPTH_CACHE.enableStencil();
        }
    }

    /*

    public static void resize(int width, int height) {
        if (DEPTH_CACHE != null) {
            DEPTH_CACHE.resize(width, height, Minecraft.ON_OSX);
        }
    }

     */

    public static MultiBufferSource.BufferSource getDelayedRender() {
        if (DELAYED_RENDER == null) {
            SequencedMap<RenderType, ByteBufferBuilder> buffers = new LinkedHashMap<>();
            for (RenderType type : DelayedRenderTypes.ALL_ENTITY) {
                buffers.put(type, new ByteBufferBuilder(isLargeSizeBuffer() ? 2097152 : type.bufferSize()));
            }

            DELAYED_RENDER = MultiBufferSource.immediateWithBuffers(buffers,new ByteBufferBuilder(256));
        }
        return DELAYED_RENDER;
    }

    public static boolean isLargeSizeBuffer() {
        return ModList.get().isLoaded("embeddium") || ModList.get().isLoaded("rubidium") || ModList.get().isLoaded("sodium");
    }

}

