package net.fabricmc.renew_auto;

//import net.minecraft.item.Items;
//import net.minecraft.block.DispenserBlock;
//import net.minecraft.block.dispenser.ShearsDispenserBehavior;

import net.fabricmc.api.ModInitializer;
//import net.fabricmc.api.ClientModInitializer;

public class RenewAutoInitialize implements ModInitializer {
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		System.out.println("RenewAuto is loaded.");
	}
}
