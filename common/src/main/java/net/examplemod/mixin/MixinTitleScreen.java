//package net.examplemod.mixin;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.screens.TitleScreen;
//import net.minecraft.core.particles.DustColorTransitionOptions;
//import net.minecraft.world.level.block.RedStoneWireBlock;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(TitleScreen.class)
//public class MixinTitleScreen {
//    @Inject(at = @At("HEAD"), method = "init()V")
//    private void init(CallbackInfo info) {
//        System.out.println("Hello from example architectury common mixin!");
//    }
//
//    //RedStoneWireBlock
//}