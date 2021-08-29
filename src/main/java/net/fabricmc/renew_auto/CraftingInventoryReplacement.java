package net.fabricmc.renew_auto;

import java.util.Iterator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;

public class CraftingInventoryReplacement extends CraftingInventory {
   public DefaultedList<ItemStack> stacks;
   private final int width;
   private final int height;
   private ScreenHandler handler;
   private Inventory owner;

   public CraftingInventoryReplacement(int width, int height) {
      super(null, width, height);
      this.stacks = DefaultedList.ofSize(width * height, ItemStack.EMPTY);
      this.width = width;
      this.height = height;
      this.owner = null;
   }

   public void SetHandler(ScreenHandler handler)
   {
      this.handler = handler;
   }

   public void SetOwner(Inventory owner)
   {
      this.owner = owner;
   }

   @Override
   public void onClose(PlayerEntity player) {
      if(this.owner != null) {
         for (int i = 0; i < stacks.size(); i++) {
            this.owner.setStack(i, stacks.get(i));
         }
         this.owner.onClose(player);
      }
   }

   @Override
   public int size() {
      return this.stacks.size();
   }

   @Override
   public boolean isEmpty() {
      Iterator<ItemStack> var1 = this.stacks.iterator();

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
   public ItemStack getStack(int slot) {
      return slot >= this.size() ? ItemStack.EMPTY : (ItemStack)this.stacks.get(slot);
   }

   @Override
   public ItemStack removeStack(int slot) {
      ItemStack itemStack = Inventories.removeStack(this.stacks, slot);
      if(this.handler != null) {
         this.handler.onContentChanged(this);
      }
      return itemStack;
   }

   @Override
   public ItemStack removeStack(int slot, int amount) {
      ItemStack itemStack = Inventories.splitStack(this.stacks, slot, amount);
      if (!itemStack.isEmpty()) {
         if(this.handler != null) {
            this.handler.onContentChanged(this);
         }
      }

      return itemStack;
   }

   @Override
   public void setStack(int slot, ItemStack stack) {
      if(!ItemStack.areEqual(stack, this.stacks.get(slot))) {
         this.stacks.set(slot, stack);
         if(this.handler != null) {
            this.handler.onContentChanged(this);
         }
      }
   }

   @Override
   public void markDirty() {
      if(this.owner != null) {
         this.owner.markDirty();
      }
   }

   @Override
   public boolean canPlayerUse(PlayerEntity player) {
      return true;
   }

   @Override
   public void clear() {
      this.stacks.clear();
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public void provideRecipeInputs(RecipeMatcher finder) {
      Iterator<ItemStack> var2 = this.stacks.iterator();

      while(var2.hasNext()) {
         ItemStack itemStack = (ItemStack)var2.next();
         finder.addUnenchantedInput(itemStack);
      }

   }
}
