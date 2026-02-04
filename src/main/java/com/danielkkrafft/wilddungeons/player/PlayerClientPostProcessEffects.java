package com.danielkkrafft.wilddungeons.player;

import com.danielkkrafft.wilddungeons.WildDungeons;
import com.danielkkrafft.wilddungeons.entity.Offering;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

import java.util.List;

import static com.danielkkrafft.wilddungeons.WildDungeons.MODID;

@EventBusSubscriber(value = Dist.CLIENT)
public class PlayerClientPostProcessEffects {

    @SubscribeEvent
    public static void onPlayerTickAroundRift(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.isPaused()) return;

        double px = player.getX();
        double py = player.getEyeY();
        double pz = player.getZ();

        Vec3 look = player.getLookAngle();
        Vector3f viewDir = new Vector3f(
                (float) look.x,
                (float) look.y,
                (float) look.z
        ).normalize();

        float influence = 0f;

        List<Offering> rifts = player.level().getEntitiesOfClass(
                Offering.class,
                player.getBoundingBox().inflate(28F)
        );
        if (rifts.isEmpty()) {
            return;
        }

        for (Offering rift : rifts) {
            double rx = rift.getX();
            double ry = rift.getY() + rift.getBbHeight() * 0.5;
            double rz = rift.getZ();

            float dx = (float) (rx - px);
            float dy = (float) (ry - py);
            float dz = (float) (rz - pz);

            float distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > 28F * 28F) continue;

            float dist = (float) Math.sqrt(distSq);

            Vector3f riftDir = new Vector3f(dx, dy, dz);
            riftDir.normalize();

            float dot = viewDir.dot(riftDir);
            if (dot <= 0f) continue;

            float distanceFactor = 1f - (dist / 28F);

            influence += dot * distanceFactor;
        }

        ShaderProgram shader = VeilRenderSystem.setShader(WildDungeons.rl("rift"));
        if (shader!= null) {
            if (shader.getUniform("InfluenceR") != null) {
                shader.getUniform("InfluenceR").setFloat(Math.min(influence, 1f));
                //influence = Math.min(influence, 1f);
            } else {
                System.err.println("Could not find Uniform");

            }

            System.out.println(influence);
        } else {
            System.err.println("Could not find shader");

        }

    }




}
