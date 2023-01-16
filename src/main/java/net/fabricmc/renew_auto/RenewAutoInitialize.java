package net.fabricmc.renew_auto;

import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.BlockPlacementDispenserBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.block.entity.BlockEntityType;
import net.fabricmc.renew_auto.dispenser.FishingRodDispenserBehavior;
import net.fabricmc.renew_auto.dispenser.ToolDispenserBehavior;
import net.fabricmc.renew_auto.dispenser.PlantDispenserBehavior;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

public class RenewAutoInitialize implements ModInitializer {
	public static final Identifier CRAFTING_EXT = new Identifier("renew_auto", "crafting_ext");
	public static final Identifier CRAFTING_ENTITY = new Identifier("renew_auto", "crafting_entity");
	public static final ScreenHandlerType<CraftingScreenHandlerExtension> CRAFTING_SCREEN_EXTENSION = ScreenHandlerRegistry.registerSimple(CRAFTING_EXT, CraftingScreenHandlerExtension::new);
	public static final BlockEntityType<CraftingTableBlockEntity> CRAFTING_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, CRAFTING_ENTITY, FabricBlockEntityTypeBuilder.create(CraftingTableBlockEntity::new, Blocks.CRAFTING_TABLE).build());
	public static final Block COPPER_PIPE = new CopperPipeBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool());
	public static final BlockEntityType<CopperPipeBlockEntity> COPPER_PIPE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier("renew_auto", "copper_pipe_entity"), FabricBlockEntityTypeBuilder.create(CopperPipeBlockEntity::new, COPPER_PIPE).build());
 
	public static final FabricEntityTypeBuilder<DispenserFishingBobberEntity> DISPENSER_FISHING_BOBBER_BUILDER = FabricEntityTypeBuilder.create(SpawnGroup.MISC, DispenserFishingBobberEntity::new);
	public static final EntityType<DispenserFishingBobberEntity> FISHING_BOBBER_ENTITY = Registry.register(
		Registry.ENTITY_TYPE, 
		new Identifier("renew_auto", "fishing_bobber_entity"),
        DISPENSER_FISHING_BOBBER_BUILDER.dimensions(EntityDimensions.fixed(0.75f, 0.75f)).build()
    );
	

	public static FabricItemSettings FILTER_SETTINGS = new FabricItemSettings();
	static {
		FILTER_SETTINGS.group(ItemGroup.MISC);
		FILTER_SETTINGS.maxCount(1);
		FILTER_SETTINGS.rarity(Rarity.UNCOMMON);
	}
	public static final Item CRAFTING_FILTER = new CraftingFilterItem(FILTER_SETTINGS);
	public static final Item HOPPER_FILTER = new HopperFilterItem(FILTER_SETTINGS);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		DispenserBlock.registerBehavior(Items.WOODEN_SWORD, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.WOODEN_SHOVEL, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.WOODEN_PICKAXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.WOODEN_AXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.WOODEN_HOE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.STONE_SWORD, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.STONE_SHOVEL, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.STONE_PICKAXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.STONE_AXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.STONE_HOE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.GOLDEN_SWORD, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.GOLDEN_SHOVEL, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.GOLDEN_PICKAXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.GOLDEN_AXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.GOLDEN_HOE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.IRON_SWORD, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.IRON_SHOVEL, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.IRON_PICKAXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.IRON_AXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.IRON_HOE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.DIAMOND_SWORD, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.DIAMOND_SHOVEL, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.DIAMOND_PICKAXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.DIAMOND_AXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.DIAMOND_HOE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.NETHERITE_SWORD, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.NETHERITE_SHOVEL, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.NETHERITE_PICKAXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.NETHERITE_AXE, new ToolDispenserBehavior());
		DispenserBlock.registerBehavior(Items.NETHERITE_HOE, new ToolDispenserBehavior());

		DispenserBlock.registerBehavior(Items.FISHING_ROD, new FishingRodDispenserBehavior());

		DispenserBlock.registerBehavior(Items.OAK_SAPLING, new BlockPlacementDispenserBehavior());
		DispenserBlock.registerBehavior(Items.SPRUCE_SAPLING, new BlockPlacementDispenserBehavior());
		DispenserBlock.registerBehavior(Items.BIRCH_SAPLING, new BlockPlacementDispenserBehavior());
		DispenserBlock.registerBehavior(Items.JUNGLE_SAPLING, new BlockPlacementDispenserBehavior());
		DispenserBlock.registerBehavior(Items.ACACIA_SAPLING, new BlockPlacementDispenserBehavior());
		DispenserBlock.registerBehavior(Items.DARK_OAK_SAPLING, new BlockPlacementDispenserBehavior());

		DispenserBlock.registerBehavior(Items.WHEAT, new PlantDispenserBehavior());
		DispenserBlock.registerBehavior(Items.BEETROOT, new PlantDispenserBehavior());
		DispenserBlock.registerBehavior(Items.WHEAT_SEEDS, new PlantDispenserBehavior());
		DispenserBlock.registerBehavior(Items.PUMPKIN_SEEDS, new PlantDispenserBehavior());
		DispenserBlock.registerBehavior(Items.MELON_SEEDS, new PlantDispenserBehavior());
		DispenserBlock.registerBehavior(Items.BEETROOT_SEEDS, new PlantDispenserBehavior());
		DispenserBlock.registerBehavior(Items.CARROT, new PlantDispenserBehavior());
		DispenserBlock.registerBehavior(Items.POTATO, new PlantDispenserBehavior());
		DispenserBlock.registerBehavior(Items.NETHER_SPROUTS, new BlockPlacementDispenserBehavior());

		Registry.register(Registry.BLOCK, new Identifier("renew_auto", "copper_pipe"), COPPER_PIPE);

		Registry.register(Registry.ITEM, new Identifier("renew_auto", "crafting_filter"), CRAFTING_FILTER);
		Registry.register(Registry.ITEM, new Identifier("renew_auto", "hopper_filter"), HOPPER_FILTER);
		Registry.register(Registry.ITEM, new Identifier("renew_auto", "copper_pipe"), new BlockItem(COPPER_PIPE, new Item.Settings().group(ItemGroup.REDSTONE)));
		
		System.out.println("RenewAuto is loaded.");
	}
}
