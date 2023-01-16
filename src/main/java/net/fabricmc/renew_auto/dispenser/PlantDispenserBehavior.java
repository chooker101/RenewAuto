package net.fabricmc.renew_auto.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.event.GameEvent;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.world.WorldEvents;
import java.util.List;

public class PlantDispenserBehavior extends FallibleItemDispenserBehavior {

   protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
      World world = pointer.getWorld();
      if (!world.isClient()) {
         Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
         BlockPos blockPos = pointer.getPos().offset(direction);
         if(this.tryFeedAnimal((ServerWorld)world, blockPos, stack)) {
            this.setSuccess(true);
         }
         else {
            Item item = stack.getItem();
            if (item instanceof BlockItem) {
               Direction direction2 = pointer.getWorld().isAir(blockPos.down()) ? direction : Direction.UP;
               try {
                  this.setSuccess(((BlockItem)item).place(new AutomaticItemPlacementContext((World)pointer.getWorld(), blockPos, direction, stack, direction2)).isAccepted());
               }
               catch (Exception exception) {
                  LOGGER.error("Error trying to place shulker box at {}", (Object)blockPos, (Object)exception);
               }
            }
         }
         
      }
      return stack;
   }

   private boolean tryFeedAnimal(ServerWorld world, BlockPos pos, ItemStack stack) {
      List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, new Box(pos), EntityPredicates.EXCEPT_SPECTATOR);
      for (LivingEntity livingEntity : list) {
         AnimalEntity animalEntity;
         if(livingEntity instanceof AnimalEntity) {
            animalEntity = (AnimalEntity)livingEntity;
            if (animalEntity.isBreedingItem(stack)) {
               int i = animalEntity.getBreedingAge();
               if (i == 0 && animalEntity.canEat()) {
                  stack.decrement(1);
                  animalEntity.setLoveTicks(600);
                  animalEntity.world.sendEntityStatus(animalEntity, (byte)18);
                  animalEntity.emitGameEvent(GameEvent.MOB_INTERACT, animalEntity.getCameraBlockPos());
                  return true;
               }
               if (animalEntity.isBaby()) {
                  stack.decrement(1);
                  animalEntity.growUp((int)((float)(-i / 20) * 0.1f), true);
                  animalEntity.emitGameEvent(GameEvent.MOB_INTERACT, animalEntity.getCameraBlockPos());
                  return true;
               }
           }
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