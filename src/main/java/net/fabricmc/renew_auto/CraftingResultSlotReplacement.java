package net.fabricmc.renew_auto;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

// A gutted class for the Crafting Filter.
// It was the easiest way I could find to not let the player craft, while the item shows.
public class CraftingResultSlotReplacement extends Slot {
   public CraftingResultSlotReplacement(PlayerEntity player, CraftingInventory input, Inventory inventory, int index, int x, int y) {
      super(inventory, index, x, y);
   }

   public boolean canInsert(ItemStack stack) {
      return false;
   }

   public ItemStack takeStack(int amount) {
      return ItemStack.EMPTY;
   }

   protected void onCrafted(ItemStack stack, int amount) {
   }

   protected void onTake(int amount) {
   }

   protected void onCrafted(ItemStack stack) {
   }

   public void onTakeItem(PlayerEntity player, ItemStack stack) {
   }
}
