package net.fabricmc.renew_auto;

import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ScreenHandler;

public class CraftingScreenHandlerExtension extends AbstractRecipeScreenHandler<CraftingInventory> {
   public static final int field_30781 = 0;
   private final CraftingInventoryReplacement input;
   private final CraftingResultInventory result;
   private final ScreenHandlerContext context;
   private final PlayerEntity player;
   private boolean isFilter;

   public CraftingScreenHandlerExtension(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, ScreenHandlerContext.EMPTY, new CraftingInventoryReplacement(3, 3), false);
   }

   public CraftingScreenHandlerExtension(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CraftingInventoryReplacement craftingInventory, boolean isFilter) {
      super(ScreenHandlerType.CRAFTING, syncId);
      this.result = new CraftingResultInventory();
      this.context = context;
      this.player = playerInventory.player;
      this.input = craftingInventory;
      this.isFilter = isFilter;
      craftingInventory.SetHandler(this);
      if(!isFilter) {
         this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 124, 35));
      }
      else {
         this.addSlot(new CraftingResultSlotReplacement(playerInventory.player, this.input, this.result, 0, 124, 35));
      }

      int m;
      int l;
      for(m = 0; m < 3; ++m) {
         for(l = 0; l < 3; ++l) {
            this.addSlot(new Slot(this.input, l + m * 3, 30 + l * 18, 17 + m * 18));
         }
      }

      for(m = 0; m < 3; ++m) {
         for(l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
         }
      }

      for(m = 0; m < 9; ++m) {
         this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
      }

   }

   protected static void updateResult(ScreenHandler handler, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftingResultInventory resultInventory) {
      if (!world.isClient) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
         ItemStack itemStack = ItemStack.EMPTY;
         Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
         if (optional.isPresent()) {
            CraftingRecipe craftingRecipe = (CraftingRecipe)optional.get();
            if (resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe)) {
               itemStack = craftingRecipe.craft(craftingInventory);
            }
         }

         resultInventory.setStack(0, itemStack);
         handler.setPreviousTrackedSlot(0, itemStack);
         serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack));
      }
   }

   public void onContentChanged(Inventory inventory) {
      this.context.run((world, pos) -> {
         updateResult(this, world, this.player, this.input, this.result);
      });
   }

   public void populateRecipeFinder(RecipeMatcher finder) {
      this.input.provideRecipeInputs(finder);
   }

   public void clearCraftingSlots() {
   }

   public boolean matches(Recipe<? super CraftingInventory> recipe) {
      return recipe.matches(this.input, this.player.world);
   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.input.onClose(player);
   }

   public boolean canUse(PlayerEntity player) {
      if(isFilter) {
         return true;
      }
      else {
         return canUse(this.context, player, Blocks.CRAFTING_TABLE);
      }
   }

   public void setIsFilter(boolean isFilter) {
      this.isFilter = isFilter;
   }

   public ItemStack transferSlot(PlayerEntity player, int index) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         if (index == 0 && !this.isFilter) {
            this.context.run((world, pos) -> {
               itemStack2.getItem().onCraft(itemStack2, world, player);
            });
            if (!this.insertItem(itemStack2, 10, 46, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickTransfer(itemStack2, itemStack);
         } else if (index >= 10 && index < 46) {
            if (!this.insertItem(itemStack2, 1, 10, false)) {
               if (index < 37) {
                  if (!this.insertItem(itemStack2, 37, 46, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.insertItem(itemStack2, 10, 37, false)) {
                  return ItemStack.EMPTY;
               }
            }
         } else if (!this.insertItem(itemStack2, 10, 46, false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
            onContentChanged(this.input);
         } else {
            slot.markDirty();
         }

         if (itemStack2.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTakeItem(player, itemStack2);
         if (index == 0) {
            player.dropItem(itemStack2, false);
         }
      }

      return itemStack;
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
   }

   public int getCraftingResultSlotIndex() {
      return 0;
   }

   public int getCraftingWidth() {
      return this.input.getWidth();
   }

   public int getCraftingHeight() {
      return this.input.getHeight();
   }

   public int getCraftingSlotCount() {
      return 10;
   }

   public RecipeBookCategory getCategory() {
      return RecipeBookCategory.CRAFTING;
   }

   public boolean canInsertIntoSlot(int index) {
      return index != this.getCraftingResultSlotIndex();
   }
}
