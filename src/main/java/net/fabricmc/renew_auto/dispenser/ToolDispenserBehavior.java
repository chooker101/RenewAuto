package net.fabricmc.renew_auto.dispenser;

import java.util.Vector;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.world.WorldEvents;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.tag.BlockTags;
import net.minecraft.fluid.FluidState;

public class ToolDispenserBehavior extends FallibleItemDispenserBehavior {
   private float currentBreakingProgress = 0.0F;

   protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
      World world = pointer.getWorld();
      if (!world.isClient()) {
         BlockPos blockPos = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
         this.setSuccess(this.tryBreakBlock((ServerWorld)world, blockPos, stack));
      }
      return stack;
   }

   private boolean isTreeBlock(BlockState blockState)
   {
      return blockState.isIn(BlockTags.LOGS) || blockState.isIn(BlockTags.LEAVES);
   }

   private void recursivelyBreakTree(ServerWorld world, Vector<Vector<BlockPos>> list, BlockPos pos, int depth)
   {
      BlockPos up = pos.up();
      BlockPos down = pos.down();
      BlockPos north = pos.north();
      BlockPos east = pos.east();
      BlockPos south = pos.south();
      BlockPos west = pos.west();

      boolean upExist = false;
      boolean downExist = false;
      boolean northExist = false;
      boolean eastExist = false;
      boolean southExist = false;
      boolean westExist = false;

      boolean newBlocks = false;

      while(depth <= 50) {
         for (int i = 0; i < list.get(depth).size(); i++) {
            pos = list.get(depth).get(i);

            upExist = false;
            downExist = false;
            northExist = false;
            eastExist = false;
            southExist = false;
            westExist = false;

            up = pos.up();
            down = pos.down();
            north = pos.north();
            east = pos.east();
            south = pos.south();
            west = pos.west();

            if(isTreeBlock(world.getBlockState(up))) {
               Vector<BlockPos> lowerDepthList;
               if(depth - 1 >= 0) {
                  lowerDepthList = list.get(depth - 1);
               }
               else {
                  lowerDepthList = list.get(depth);
               }
   
               for (int j = 0; j < lowerDepthList.size(); j++) {
                  if(up.equals(lowerDepthList.get(j))) {
                     upExist = true;
                  }
               }
               
               if(list.size() >= depth + 2) {
                  Vector<BlockPos> upperDepthList = list.get(depth + 1);
                  for (int j = 0; j < upperDepthList.size(); j++) {
                     if(up.equals(upperDepthList.get(j))) {
                        upExist = true;
                     }
                  }
               }
   
               if(!upExist) {
                  if(list.size() < depth + 2) {
                     list.add(new Vector<BlockPos>());
                  }
                  list.get(depth + 1).add(up);
                  newBlocks = true;
               }
            }
   
            if(isTreeBlock(world.getBlockState(down))) {
               Vector<BlockPos> lowerDepthList;
               if(depth - 1 >= 0) {
                  lowerDepthList = list.get(depth - 1);
               }
               else {
                  lowerDepthList = list.get(depth);
               }
   
               for (int j = 0; j < lowerDepthList.size(); j++) {
                  if(down.equals(lowerDepthList.get(j))) {
                     downExist = true;
                  }
               }
               
               if(list.size() >= depth + 2) {
                  Vector<BlockPos> upperDepthList = list.get(depth + 1);
                  for (int j = 0; j < upperDepthList.size(); j++) {
                     if(down.equals(upperDepthList.get(j))) {
                        downExist = true;
                     }
                  }
               }

               if(!downExist) {
                  if(list.size() < depth + 2) {
                     list.add(new Vector<BlockPos>());
                  }
                  list.get(depth + 1).add(down);
                  newBlocks = true;
               }
            }
   
            if(isTreeBlock(world.getBlockState(north))) {
               Vector<BlockPos> lowerDepthList;
               if(depth - 1 >= 0) {
                  lowerDepthList = list.get(depth - 1);
               }
               else {
                  lowerDepthList = list.get(depth);
               }
   
               for (int j = 0; j < lowerDepthList.size(); j++) {
                  if(north.equals(lowerDepthList.get(j))) {
                     northExist = true;
                  }
               }

               if(list.size() >= depth + 2) {
                  Vector<BlockPos> upperDepthList = list.get(depth + 1);
                  for (int j = 0; j < upperDepthList.size(); j++) {
                     if(north.equals(upperDepthList.get(j))) {
                        northExist = true;
                     }
                  }
               }

               if(!northExist) {
                  if(list.size() < depth + 2) {
                     list.add(new Vector<BlockPos>());
                  }
                  list.get(depth + 1).add(north);
                  newBlocks = true;
               }
            }
   
            if(isTreeBlock(world.getBlockState(east))) {
               Vector<BlockPos> lowerDepthList;
               if(depth - 1 >= 0) {
                  lowerDepthList = list.get(depth - 1);
               }
               else {
                  lowerDepthList = list.get(depth);
               }
   
               for (int j = 0; j < lowerDepthList.size(); j++) {
                  if(east.equals(lowerDepthList.get(j))) {
                     eastExist = true;
                  }
               }

               if(list.size() >= depth + 2) {
                  Vector<BlockPos> upperDepthList = list.get(depth + 1);
                  for (int j = 0; j < upperDepthList.size(); j++) {
                     if(east.equals(upperDepthList.get(j))) {
                        eastExist = true;
                     }
                  }
               }

               if(!eastExist) {
                  if(list.size() < depth + 2) {
                     list.add(new Vector<BlockPos>());
                  }
                  list.get(depth + 1).add(east);
                  newBlocks = true;
               }
            }
   
            if(isTreeBlock(world.getBlockState(south))) {
               Vector<BlockPos> lowerDepthList;
               if(depth - 1 >= 0) {
                  lowerDepthList = list.get(depth - 1);
               }
               else {
                  lowerDepthList = list.get(depth);
               }
   
               for (int j = 0; j < lowerDepthList.size(); j++) {
                  if(south.equals(lowerDepthList.get(j))) {
                     southExist = true;
                  }
               }
  
               if(list.size() >= depth + 2) {
                  Vector<BlockPos> upperDepthList = list.get(depth + 1);
                  for (int j = 0; j < upperDepthList.size(); j++) {
                     if(south.equals(upperDepthList.get(j))) {
                        southExist = true;
                     }
                  }
               }

               if(!southExist) {
                  if(list.size() < depth + 2) {
                     list.add(new Vector<BlockPos>());
                  }
                  list.get(depth + 1).add(south);
                  newBlocks = true;
               }
            }
   
            if(isTreeBlock(world.getBlockState(west))) {
               Vector<BlockPos> lowerDepthList;
               if(depth - 1 >= 0) {
                  lowerDepthList = list.get(depth - 1);
               }
               else {
                  lowerDepthList = list.get(depth);
               }
   
               for (int j = 0; j < lowerDepthList.size(); j++) {
                  if(west.equals(lowerDepthList.get(j))) {
                     westExist = true;
                  }
               }
               
               if(list.size() >= depth + 2) {
                  Vector<BlockPos> upperDepthList = list.get(depth + 1);
                  for (int j = 0; j < upperDepthList.size(); j++) {
                     if(west.equals(upperDepthList.get(j))) {
                        westExist = true;
                     }
                  }
               }

               if(!westExist) {
                  if(list.size() < depth + 2) {
                     list.add(new Vector<BlockPos>());
                  }
                  list.get(depth + 1).add(west);
                  newBlocks = true;
               }
            }
         }
         if(!newBlocks) {
            break;
         }
         if(depth < 50) {
            depth++;
         }
         newBlocks = false;
      }
   }

   private boolean tryBreakBlock(ServerWorld world, BlockPos pos, ItemStack stack) {
      BlockState blockState = world.getBlockState(pos);
      if (blockState != null) {
         if (blockState.isAir()){
            return false;
         }

         float toolMiningSpeed = stack.getMiningSpeedMultiplier(blockState);

         if (toolMiningSpeed > 1.0F) {
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (i > 0 && !stack.isEmpty()) {
               toolMiningSpeed += (float)(i * i + 1);
            }
         }

         float hardness = blockState.getHardness(world, pos);
         if (hardness == -1.0F) {
            return false;
         } else {
            this.currentBreakingProgress += toolMiningSpeed / hardness / 30.0F;
         }

         BlockSoundGroup blockSoundGroup = blockState.getSoundGroup();
         world.playSound(null, pos, blockSoundGroup.getHitSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 8.0F, blockSoundGroup.getPitch() * 0.5F);

         if (this.currentBreakingProgress >= 1.0F) {
            if(isTreeBlock(blockState)) {
               Vector<Vector<BlockPos>> treeList = new Vector<Vector<BlockPos>>();
               treeList.add(new Vector<BlockPos>());
               treeList.get(0).add(pos);
               recursivelyBreakTree(world, treeList, pos, 0);

               FluidState fluidState;
               BlockEntity blockEntity;
               boolean bl;
               BlockState treeBlockState;
               
               world.syncWorldEvent(null, WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(blockState));
               for (Vector<BlockPos> vector : treeList) {
                  for (BlockPos treePos : vector) {
                     treeBlockState = world.getBlockState(treePos);
                     fluidState = world.getFluidState(treePos);
                     blockEntity = treeBlockState.hasBlockEntity() ? world.getBlockEntity(treePos) : null;
                     Block.dropStacks(treeBlockState, world, treePos, blockEntity, null, ItemStack.EMPTY);
                     bl = world.setBlockState(treePos, fluidState.getBlockState(), Block.NOTIFY_ALL, 512);
                     if (bl) {
                        world.emitGameEvent(null, GameEvent.BLOCK_DESTROY, treePos);
                     }
                  }
               }
            }
            else {
               world.breakBlock(pos, true);
            }
            this.currentBreakingProgress = 0.0F;
            if(stack.damage(1, (Random)world.getRandom(), (ServerPlayerEntity)null)) {
               stack.setCount(0);
            }
         }
         world.setBlockBreakingInfo(this.hashCode(), pos, (int)(this.currentBreakingProgress * 10.0F) - 1);
         return true;
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