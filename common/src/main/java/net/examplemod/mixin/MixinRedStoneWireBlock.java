package net.examplemod.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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
import java.util.Iterator;

@Mixin(RedStoneWireBlock.class)
public class MixinRedStoneWireBlock {
    private static Vec3[] COLOURS;
    private static File configFile;

    private static JsonObject redstoneConfigJson = null;

    private static Vec3[] generateColors(String hexColor) {
        Vec3[] colors = new Vec3[16];

        Color baseColor = Color.decode(hexColor);
        float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);

        for (int i = 0; i < 16; ++i) {
            float brightness = (float) i / (16 - 1);
            Color generatedColor = Color.getHSBColor(hsb[0], hsb[1], brightness);
            colors[i] = new Vec3(generatedColor.getRed() / 255.0, generatedColor.getGreen() / 255.0, generatedColor.getBlue() / 255.0);
        }

        return colors;
    }

    @Inject(method = "<init>", at=@At(value = "RETURN"))
    private void init(BlockBehaviour.Properties properties, CallbackInfo ci) {
        configFile = new File( Paths.get(System.getProperty("user.dir")).toAbsolutePath().toString() + "/config/custom_redstone.json");
        if(loadRedstoneConfig()) {
            if(redstoneConfigJson.has("hexcolor")) {
                COLOURS = generateColors(redstoneConfigJson.get("hexcolor").getAsString());
            } else {
                COLOURS = generateColors("#FF0000");
            }
        } else {
            COLOURS = generateColors("#FF0000");
        }
    }

    private boolean loadRedstoneConfig() {
        try {
            Path filePath = configFile.toPath();
            if (Files.exists(filePath)) {
                String jsonString = new String(Files.readAllBytes(configFile.toPath()));
                redstoneConfigJson = JsonParser.parseString(jsonString).getAsJsonObject();
            } else {
                redstoneConfigJson = new JsonObject();
                redstoneConfigJson.addProperty("hexcolor","#FF00FF");
                String jsonString = redstoneConfigJson.toString();
                Files.write(filePath, jsonString.getBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Overwrite
    public static int getColorForPower(int i) {
        Vec3 vec3 = COLOURS[i];
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

    @Overwrite
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        int i = (Integer)blockState.getValue(RedStoneWireBlock.POWER);
        if (i != 0) {
            Iterator var6 = Direction.Plane.HORIZONTAL.iterator();

            while(var6.hasNext()) {
                Direction direction = (Direction)var6.next();
                RedstoneSide redstoneSide = (RedstoneSide)blockState.getValue((Property)RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(direction));
                switch (redstoneSide) {
                    case UP:
                        ((RedStoneWireBlock)(Object)this).spawnParticlesAlongLine(level, randomSource, blockPos, COLOURS[i], direction, Direction.UP, -0.5F, 0.5F);
                    case SIDE:
                        ((RedStoneWireBlock)(Object)this).spawnParticlesAlongLine(level, randomSource, blockPos, COLOURS[i], Direction.DOWN, direction, 0.0F, 0.5F);
                        break;
                    case NONE:
                    default:
                        ((RedStoneWireBlock)(Object)this).spawnParticlesAlongLine(level, randomSource, blockPos, COLOURS[i], Direction.DOWN, direction, 0.0F, 0.3F);
                }
            }
        }
    }
}
