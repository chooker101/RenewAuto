package net.fabricmc.renew_auto.extension;

import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.util.ItemScatterer;
import net.fabricmc.renew_auto.CraftingTableBlockEntity;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(CraftingTableBlock.class)
public abstract class CraftingTableBlockExtension extends Block implements BlockEntityProvider {
   
    CraftingTableBlockExtension(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState());
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
       return new CraftingTableBlockEntity(pos, state);
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (state.getBlock() != newState.getBlock()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof CraftingTableBlockEntity) {
             ItemScatterer.spawn(world, pos, (CraftingTableBlockEntity)blockEntity);
             world.updateComparators(pos,this);
         }
         //super.onStateReplaced(state, world, pos, newState, moved);
      }
    }

    public boolean hasComparatorOutput(BlockState state) {
       return true;
    }
    
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
       return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
    }

    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
       //super.onSyncedBlockEvent(state, world, pos, type, data);
       BlockEntity blockEntity = world.getBlockEntity(pos);
       return blockEntity == null ? false : blockEntity.onSyncedBlockEvent(type, data);
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
       BlockEntity blockEntity = world.getBlockEntity(pos);
       return blockEntity instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory)blockEntity : null;
    }
}