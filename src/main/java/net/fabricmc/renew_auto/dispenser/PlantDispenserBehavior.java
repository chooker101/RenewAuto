package net.fabricmc.renew_auto.dispenser;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.world.WorldEvents;

public class PlantDispenserBehavior<T> extends FallibleItemDispenserBehavior {

   protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
      World world = pointer.getWorld();
      if (!world.isClient()) {
         BlockPos blockPos = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
         this.setSuccess(this.tryBreakBlock((ServerWorld)world, blockPos, stack));
      }
      return stack;
   }

   private boolean tryBreakBlock(ServerWorld world, BlockPos pos, ItemStack stack) {
      BlockState blockState = world.getBlockState(pos);
      Item item = stack.getItem();
      if (item instanceof BlockItem) {
        if (blockState.isAir()) {
            
            return true;
        }
      }
      return false;
   }

   @Override
   protected void playSound(BlockPointer pointer) {
      if (!this.isSuccess()){
         pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_FAILS, pointer.getPos(), 0);
      }
   }
}