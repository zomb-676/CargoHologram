//package com.github.zomb_676.cargo_hologram.mixin.compact;
//
//import com.github.zomb_676.cargo_hologram.ui.CargoBlurScreen;
//import net.minecraft.client.gui.screens.Screen;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.Slice;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
////@Mixin(targets = "icyllis.modernui.mc.BlurHandler", remap = false)
//public class ModernUIBlurHandleMixin {
//    @Inject(method = "blur", at = @At(value = "CONSTANT", args = "floatValue=false", ordinal = 0), slice = @Slice(
////            to= @At(value = "CONSTANT", args = "classValue=icyllis.modernui.mc.MuiScreen", ordinal = 0)
////            to= @At(value = "JUMP", opcode = Opcodes.IFEQ,ordinal = 0)
//    ), cancellable = true)
//    private void a(Screen nextScreen, CallbackInfo ci) {
//        if (nextScreen instanceof CargoBlurScreen cargoBlurScreen) {
//            if (cargoBlurScreen.getBlurType() != CargoBlurScreen.BlurType.MODERNUI) {
//                ci.cancel();
//            }
//        }
//    }
//}
