package net.examplemod.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ExperienceOrbRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Mixin(ExperienceOrbRenderer.class)
public abstract class MixinExperienceOrbRenderer extends EntityRenderer<ExperienceOrb> {

    private static Color COLOUR;
    private static File configFile;

    private static JsonObject expOrbConfigJson = null;

    @Shadow
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");
    @Shadow
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

    protected MixinExperienceOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "<init>", at=@At(value = "RETURN"))
    private void init(EntityRendererProvider.Context context, CallbackInfo ci) {
        configFile = new File( Paths.get(System.getProperty("user.dir")).toAbsolutePath().toString() + "/config/custom_exporb.json");
        if(loadExpOrbConfig()) {
            if(expOrbConfigJson.has("hexcolor")) {
                COLOUR = generateColors(expOrbConfigJson.get("hexcolor").getAsString());
            } else {
                COLOUR = generateColors("#FF0000");
            }
        } else {
            COLOUR = generateColors("#FF0000");
        }
    }

    @Overwrite
    public void render(ExperienceOrb experienceOrb, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        int j = experienceOrb.getIcon();
        float h = (float)(j % 4 * 16 + 0) / 64.0f;
        float k = (float)(j % 4 * 16 + 16) / 64.0f;
        float l = (float)(j / 4 * 16 + 0) / 64.0f;
        float m = (float)(j / 4 * 16 + 16) / 64.0f;
        float n = 1.0f;
        float o = 0.5f;
        float p = 0.25f;
        float q = 255.0f;
        float r = ((float)experienceOrb.tickCount + g) / 2.0f;
        int s = (int)((Mth.sin(r + 0.0f) + 1.0f) * 0.5f * 255.0f);
        int t = 255;
        int u = (int)((Mth.sin(r + 4.1887903f) + 1.0f) * 0.1f * 255.0f);
        poseStack.translate(0.0f, 0.1f, 0.0f);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        float v = 0.3f;
        poseStack.scale(0.3f, 0.3f, 0.3f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        vertex(vertexConsumer, matrix4f, matrix3f, -0.5f, -0.25f, COLOUR.getRed(), COLOUR.getGreen(), COLOUR.getBlue(), h, m, i);
        vertex(vertexConsumer, matrix4f, matrix3f, 0.5f, -0.25f, COLOUR.getRed(), COLOUR.getGreen(), COLOUR.getBlue(), k, m, i);
        vertex(vertexConsumer, matrix4f, matrix3f, 0.5f, 0.75f, COLOUR.getRed(), COLOUR.getGreen(), COLOUR.getBlue(), k, l, i);
        vertex(vertexConsumer, matrix4f, matrix3f, -0.5f, 0.75f, COLOUR.getRed(), COLOUR.getGreen(), COLOUR.getBlue(), h, l, i);
        poseStack.popPose();
        super.render(experienceOrb, f, g, poseStack, multiBufferSource, i);
    }

    @Shadow
    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, float f, float g, int i, int j, int k, float h, float l, int m) {
        vertexConsumer.vertex(matrix4f, f, g, 0.0f).color(i, j, k, 128).uv(h, l).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(m).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
    }

    private static Color generateColors(String hexColor) {
        Color baseColor = Color.decode(hexColor);

        return baseColor;
    }

    private boolean loadExpOrbConfig() {
        try {
            Path filePath = configFile.toPath();
            if (Files.exists(filePath)) {
                String jsonString = new String(Files.readAllBytes(configFile.toPath()));
                expOrbConfigJson = JsonParser.parseString(jsonString).getAsJsonObject();
            } else {
                expOrbConfigJson = new JsonObject();
                expOrbConfigJson.addProperty("hexcolor","#eb4034");
                String jsonString = expOrbConfigJson.toString();
                Files.write(filePath, jsonString.getBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
