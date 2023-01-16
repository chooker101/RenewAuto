package net.fabricmc.renew_auto.extension;

import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.nbt.NbtCompound;

import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import net.fabricmc.renew_auto.FilterHelper;
import net.fabricmc.renew_auto.FilterEntityInterface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.callback.*;
import org.spongepowered.asm.mixin.injection.*;

@Pseudo
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityExtension extends LootableContainerBlockEntity implements Hopper, SidedInventory, FilterEntityInterface {
    private DefaultedList<ItemStack> filterItems = DefaultedList.ofSize(5, ItemStack.EMPTY);

    public HopperBlockEntityExtension(BlockPos pos, BlockState state) {
         super(BlockEntityType.HOPPER, pos, state);
    }

    public void setFilterItems(DefaultedList<ItemStack> filterItems) {
        this.filterItems = filterItems;
    }
    
   @Inject(method = "readNbt", at = @At("RETURN"))
   public void readNbtExtension(NbtCompound nbt, CallbackInfo ci) {
      FilterHelper.readNbt(nbt, filterItems);
   }

   @Inject(method = "writeNbt", at = @At("RETURN"))
   public void writeNbtExtension(NbtCompound nbt, CallbackInfo ci) {
      nbt = FilterHelper.writeNbt(nbt, filterItems);
   }

    @Override
    public int[] getAvailableSlots(Direction direction) {
        // Just return an array of all slots
        int[] result;
        result = new int[this.size()];
        for (int i = 0; i < result.length; i++) {
           result[i] = i;
        }
        
        return result;
    }

    protected boolean isFilterEmpty() {
       Iterator<ItemStack> var1 = filterItems.iterator();
       ItemStack itemStack;
       do {
          if (!var1.hasNext()) {
             return true;
          }
       
          itemStack = (ItemStack)var1.next();
       } while(itemStack.isEmpty());

       return false;
    }
   
    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction direction) {
        if(slot >= 5) {
           return false;
        }
        if(isFilterEmpty()) {
           return true;
        }
        else if(ItemStack.areItemsEqual(filterItems.get(slot), stack)) {
            int currentLowest = this.getStack(slot).getCount();
            for(int i = 0; i < 5; i++) {
                if(ItemStack.areItemsEqual(filterItems.get(i), stack)) {
                    if(this.getStack(i).getCount() < currentLowest) {
                        return false;
                    }
                }
            }
            return true;
        }
        else {
           return false;
        }
    }
   
   @Override
   public boolean canExtract(int slot, ItemStack stack, Direction direction) {
      if(slot < 5) {
         return true;
      }
      return false;
   }
}