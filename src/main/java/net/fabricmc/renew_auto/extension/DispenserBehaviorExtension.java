package net.fabricmc.renew_auto.extension;

import net.minecraft.item.Items;
import net.minecraft.block.DispenserBlock;
import net.minecraft.Bootstrap;
//import net.minecraft.block.dispenser.DispenserBehavior;
//import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.ShearsDispenserBehavior;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(Bootstrap.class)
public abstract class DispenserBehaviorExtension {
    @Shadow @Final
    private static volatile boolean initialized;

	@Inject(method = "initialize", at = @At("HEAD"))
	private static void initializeExt(CallbackInfo info) {
        if (!initialized) {
		    //DispenserBlock.registerBehavior(Items.DIAMOND_PICKAXE.asItem(), new ShearsDispenserBehavior());
        }
	}
}
