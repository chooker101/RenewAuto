package net.fabricmc.renew_auto;

import java.util.Iterator;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.TranslatableText;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;

public class CraftingFilterItem extends Item implements Inventory {
    private static final Text TITLE = new TranslatableText("container.crafting");
    private DefaultedList<ItemStack> temporaryItemStacks;
    private boolean hasBeenUsedOnBlock = false;

    public CraftingFilterItem(Item.Settings settings) {
        super(settings);
        temporaryItemStacks = DefaultedList.ofSize(9, ItemStack.EMPTY);
    }

    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
           return  TypedActionResult.pass(user.getStackInHand(hand));
        } else {
            if(!hasBeenUsedOnBlock) {
                user.openHandledScreen(createScreenHandlerFactory(world, user.getBlockPos(), this));
            }
            hasBeenUsedOnBlock = false;
            return TypedActionResult.consume(user.getStackInHand(hand));
        }
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) {
            return ActionResult.PASS;
        } else {
            BlockPos blockPos = context.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isOf(Blocks.CRAFTING_TABLE))
            {
                CraftingTableBlockEntity craftingTableEntity = (CraftingTableBlockEntity)world.getBlockEntity(blockPos);

                NbtCompound nbtCompound = context.getStack().getNbt();
                boolean isEmpty = false;

                if(nbtCompound != null) {
                    DefaultedList<ItemStack> temp = DefaultedList.ofSize(9, ItemStack.EMPTY);
                    Inventories.readNbt(nbtCompound, temp);

                    Iterator<ItemStack> var1 = temp.iterator();
                    ItemStack itemStack;
                    do {
                       if (!var1.hasNext()) {
                        isEmpty = true;
                       }
                
                       itemStack = (ItemStack)var1.next();
                    } while(itemStack.isEmpty());

                    if(!isEmpty){
                        craftingTableEntity.setFilterItems(temp);
                        hasBeenUsedOnBlock = true;
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }
        return ActionResult.PASS;
    }

    public void setStack(int slot, ItemStack stack) {
        temporaryItemStacks.set(slot, stack);
    }

    public ItemStack removeStack(int slot) {
       return ItemStack.EMPTY;
    }

    public ItemStack removeStack(int slot, int amount) {
       return ItemStack.EMPTY;
    }

    public ItemStack getStack(int slot) {
       return ItemStack.EMPTY;
    }

    public boolean isEmpty() {
        Iterator<ItemStack> var1 = this.temporaryItemStacks.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
               return true;
            }

            itemStack = (ItemStack)var1.next();
        } while(itemStack.isEmpty());

        return false;
    }

    public void clear() {
        temporaryItemStacks = DefaultedList.ofSize(9, ItemStack.EMPTY);
    }

    public int size() {
        return 0;
    }

    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    public void markDirty() {
    }

    public void onClose(PlayerEntity player) {
        ItemStack stack = player.getStackInHand(player.getActiveHand());
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        if(!isEmpty()) {
            Inventories.writeNbt(nbtCompound, temporaryItemStacks);

            ItemStack lastCraftingResult = player.currentScreenHandler.slots.get(0).getStack();
            if (lastCraftingResult != null)
            {
                NbtCompound nbtCompoundSlot = new NbtCompound();
                nbtCompoundSlot.putByte("Slot", (byte)0);
                lastCraftingResult.writeNbt(nbtCompoundSlot);
                nbtCompound.put("Result", nbtCompoundSlot);
            }
            clear();
        }
        else {
            Inventories.writeNbt(nbtCompound, DefaultedList.ofSize(9, ItemStack.EMPTY));
            ItemStack lastCraftingResult = ItemStack.EMPTY;
            NbtCompound nbtCompoundSlot = new NbtCompound();
            nbtCompoundSlot.putByte("Slot", (byte)0);
            lastCraftingResult.writeNbt(nbtCompoundSlot);
            nbtCompound.put("Result", nbtCompoundSlot);
        }
    }

    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext options) {
        if (world != null) {
            NbtCompound nbtCompound = stack.getNbt();
            if (nbtCompound != null) {
                if (nbtCompound.contains("Items", 9)) {
                    ItemStack itemStack = ItemStack.EMPTY;
                    NbtCompound subNbt = nbtCompound.getCompound("Result");
                    itemStack = ItemStack.fromNbt(subNbt);
                    
                    if (!itemStack.isEmpty()) {
                        MutableText mutableText = itemStack.getName().shallowCopy();
                        tooltip.add(mutableText);
                    }
                }
            }
        }
    }

    public NamedScreenHandlerFactory createScreenHandlerFactory(World world, BlockPos pos, Inventory owner) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            ItemStack stack = player.getStackInHand(player.getActiveHand());
            
            CraftingInventoryReplacement craftingInventory = new CraftingInventoryReplacement(3, 3);
            craftingInventory.SetOwner(owner);
            NbtCompound nbtCompound = stack.getNbt();

            if(nbtCompound != null) {
                DefaultedList<ItemStack> temp = DefaultedList.ofSize(9, ItemStack.EMPTY);
                Inventories.readNbt(nbtCompound, temp);
                craftingInventory.stacks = temp;
            }
             
            var handler = new CraftingScreenHandlerExtension(syncId, inventory, ScreenHandlerContext.create(world, pos), craftingInventory, true);
            handler.onContentChanged(craftingInventory);
            return handler;
        }, TITLE);
    }
}