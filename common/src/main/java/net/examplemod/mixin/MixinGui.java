package net.examplemod.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Mixin(Gui.class)
public class MixinGui {
//    @Inject(at = @At("HEAD"), method = "renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V")
//    private void renderExperienceBarInject(GuiGraphics guiGraphics, int i, CallbackInfo info) {
//        //System.out.println("Hello from example architectury common mixin!");
//        //guiGraphics.setColor(0f, 0f, 0f, 1f);
//    }

    private static File configFile;

    private static JsonObject expConfigJson = null;

    private static int COLOUR;

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private int screenWidth;
    @Shadow
    private int screenHeight;

    @Shadow
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void renderExperienceBar(GuiGraphics guiGraphics, int i) {

        if(expConfigJson == null) {
            configFile = new File(Paths.get(System.getProperty("user.dir")).toAbsolutePath().toString() + "/config/custom_exp.json");
            if (loadExpConfig()) {
                if (expConfigJson.has("hexcolor")) {
                    COLOUR = Integer.decode(expConfigJson.get("hexcolor").getAsString());
                } else {
                    COLOUR = 0xFF0000;
                }
            } else {
                COLOUR = 0xFF0000;
            }
        }

        int m;
        int l;
        this.minecraft.getProfiler().push("expBar");
        int j = this.minecraft.player.getXpNeededForNextLevel();
        if (j > 0) {
            int k = 182;
            l = (int)(this.minecraft.player.experienceProgress * 183.0f);
            m = this.screenHeight - 32 + 3;
            guiGraphics.blit(GUI_ICONS_LOCATION, i, m, 0, 64, 182, 5);
            if (l > 0) {
                guiGraphics.blit(GUI_ICONS_LOCATION, i, m, 0, 69, l, 5);
            }
        }
        this.minecraft.getProfiler().pop();
        if (this.minecraft.player.experienceLevel > 0) {
            this.minecraft.getProfiler().push("expLevel");
            String string = "" + this.minecraft.player.experienceLevel;
            l = (this.screenWidth - this.getFont().width(string)) / 2;
            m = this.screenHeight - 31 - 4;
            guiGraphics.drawString(this.getFont(), string, l + 1, m, 0, false);
            guiGraphics.drawString(this.getFont(), string, l - 1, m, 0, false);
            guiGraphics.drawString(this.getFont(), string, l, m + 1, 0, false);
            guiGraphics.drawString(this.getFont(), string, l, m - 1, 0, false);
            guiGraphics.drawString(this.getFont(), string, l, m, COLOUR, false);
            this.minecraft.getProfiler().pop();
        }
    }

    private boolean loadExpConfig() {
        try {
            Path filePath = configFile.toPath();
            if (Files.exists(filePath)) {
                String jsonString = new String(Files.readAllBytes(configFile.toPath()));
                expConfigJson = JsonParser.parseString(jsonString).getAsJsonObject();
            } else {
                expConfigJson = new JsonObject();
                expConfigJson.addProperty("hexcolor","#eb4034");
                String jsonString = expConfigJson.toString();
                Files.write(filePath, jsonString.getBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Shadow
    public Font getFont() {
        return this.minecraft.font;
    }

}