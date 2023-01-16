package net.fabricmc.renew_auto;

import java.util.Iterator;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.screen.ScreenHandlerContext;

public class CraftingTableBlockEntity extends LootableContainerBlockEntity implements SidedInventory {
   private CraftingInventoryReplacement craftingInventory;
   private DefaultedList<ItemStack> filterItems;

   protected CraftingTableBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
      super(blockEntityType, blockPos, blockState);
      craftingInventory = new CraftingInventoryReplacement(3, 3);
      craftingInventory.SetOwner(this);
      filterItems = DefaultedList.ofSize(9, ItemStack.EMPTY);
   }

   public CraftingTableBlockEntity(BlockPos pos, BlockState state) {
      this(RenewAutoInitialize.CRAFTING_BLOCK_ENTITY, pos, state);
   }

   public int size() {
      return 9;
   }

   protected Text getContainerName() {
      return new TranslatableText(getCachedState().getBlock().getTranslationKey());
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

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if(this.craftingInventory.isEmpty()) {
         if (!this.deserializeLootTable(nbt)) {
            DefaultedList<ItemStack> temp = DefaultedList.ofSize(9, ItemStack.EMPTY);
            Inventories.readNbt(nbt, temp);

            Iterator<ItemStack> var1 = temp.iterator();
            ItemStack itemStack;
            do {
               if (!var1.hasNext()) {
                  return;
               }
            
               itemStack = (ItemStack)var1.next();
            } while(itemStack.isEmpty());

            
            craftingInventory.stacks = temp;
            FilterHelper.readNbt(nbt, filterItems);
         }
      }
   }

   public void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (!this.craftingInventory.isEmpty()) {
         Inventories.writeNbt(nbt, this.craftingInventory.stacks);
         FilterHelper.writeNbt(nbt, filterItems);
      }
   }

   protected DefaultedList<ItemStack> getInvStackList() {
      return this.craftingInventory.stacks;
   }

   protected void setInvStackList(DefaultedList<ItemStack> list) {
      this.craftingInventory.stacks = list;
   }

   protected ItemStack getCraftedOutput() {
      ItemStack itemStack = ItemStack.EMPTY;
      Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
      if (optional.isPresent()) {
         CraftingRecipe craftingRecipe = (CraftingRecipe)optional.get();
         itemStack = craftingRecipe.craft(craftingInventory);

         for(int i = 0; i < 9; ++i) {
            if(!getStack(i).isEmpty()){
               removeStack(i, 1);
            }
         }

         markDirty();
      }
      return itemStack;
   }

   protected ItemStack getCraftedStack() {
      ItemStack itemStack = ItemStack.EMPTY;
      Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
      if (optional.isPresent()) {
         CraftingRecipe craftingRecipe = (CraftingRecipe)optional.get();
         itemStack = craftingRecipe.getOutput();
      }
      return itemStack;
   }

   @Override
   public ItemStack getStack(int slot) {
      ItemStack itemStack = ItemStack.EMPTY;
      if(slot < 9)
      {
         itemStack = craftingInventory.getStack(slot);
      }
      else if(slot == 9)
      {
         itemStack = getCraftedStack();
      }
      return itemStack;
   }

   @Override
   public ItemStack removeStack(int slot) {
      ItemStack itemStack = ItemStack.EMPTY;
      if(slot < 9)
      {
         itemStack = craftingInventory.removeStack(slot);
      }
      else if(slot == 9)
      {
         itemStack = getCraftedOutput();
      }
      return itemStack;
   }

   @Override
   public ItemStack removeStack(int slot, int amount) {
      ItemStack itemStack = ItemStack.EMPTY;
      if(slot < 9)
      {
         itemStack = craftingInventory.removeStack(slot, amount);
      }
      else if(slot == 9)
      {
         itemStack = getCraftedOutput();
      }
      return itemStack;
   }

   @Override
   public void setStack(int slot, ItemStack stack) {
      if(slot < 9)
      {
         craftingInventory.setStack(slot, stack);
      }
   }

   public void setFilterItems(DefaultedList<ItemStack> filterItems) {
       this.filterItems = filterItems;
   }

   @Override
   public int[] getAvailableSlots(Direction direction) {
       // Just return an array of all slots
      int[] result;
      if(direction == Direction.DOWN) {
         result = new int[1];
         result[0] = 9;
         return result;
      }

      result = new int[getInvStackList().size()];
      for (int i = 0; i < result.length; i++) {
         result[i] = i;
      }

      return result;
   }
   
   @Override
   public boolean canInsert(int slot, ItemStack stack, Direction direction) {
      if(slot >= 9) {
         return false;
      }
      if(isFilterEmpty()) {
         return true;
      }
      else if(ItemStack.areItemsEqual(filterItems.get(slot), stack)) {
         int currentLowest = this.craftingInventory.stacks.get(slot).getCount();
         for(int i = 0; i < 9; i++) {
            if(ItemStack.areItemsEqual(filterItems.get(i), stack)) {
               if(this.craftingInventory.stacks.get(i).getCount() < currentLowest) {
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
      if(slot == 9) {
         if(isFilterEmpty()) {
            return true;
         }
         else {
            for(int i = 0; i < 9; i++) {
               if(!ItemStack.areItemsEqual(filterItems.get(i), this.craftingInventory.stacks.get(i))) {
                  return false;
               }
            }
            return true;
         }
      }
      return false;
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      var sh = new CraftingScreenHandlerExtension(syncId, playerInventory, ScreenHandlerContext.create(world, pos), craftingInventory, false);
      sh.onContentChanged(craftingInventory);
      return sh;
   }
}