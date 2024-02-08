package net.examplemod.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Mixin(Item.class)
public abstract class MixinItem {

    private static int COLOUR;
    private static File configFile;

    private static JsonObject itemConfigJson = null;

    @Final
    @Shadow
    private int maxDamage;

    @Inject(method = "<init>", at=@At(value = "RETURN"))
    private void init(Item.Properties properties, CallbackInfo ci) {
        configFile = new File( Paths.get(System.getProperty("user.dir")).toAbsolutePath().toString() + "/config/custom_durability.json");
        if(loadItemConfig()) {
            if(itemConfigJson.has("hexcolor")) {
                COLOUR = Integer.decode(itemConfigJson.get("hexcolor").getAsString());
            } else {
                COLOUR = 0xFF0000;
            }
        } else {
            COLOUR = 0xFF0000;
        }
    }


    @Overwrite
    public int getBarColor(ItemStack itemStack) {
        //float f = Math.max(0.0f, ((float)this.maxDamage - (float)itemStack.getDamageValue()) / (float)this.maxDamage);
        return COLOUR; //Mth.hsvToRgb(f / 3.0f, 1.0f, 1.0f);
    }

    private boolean loadItemConfig() {
        try {
            Path filePath = configFile.toPath();
            if (Files.exists(filePath)) {
                String jsonString = new String(Files.readAllBytes(configFile.toPath()));
                itemConfigJson = JsonParser.parseString(jsonString).getAsJsonObject();
            } else {
                itemConfigJson = new JsonObject();
                itemConfigJson.addProperty("hexcolor","#eb4034");
                String jsonString = itemConfigJson.toString();
                Files.write(filePath, jsonString.getBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
