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
import net.minecraft.util.Formatting;
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
import net.minecraft.screen.HopperScreenHandler;

public class HopperFilterItem extends Item implements Inventory {
    private static final Text TITLE = new TranslatableText("container.crafting");
    private DefaultedList<ItemStack> stacks;
    private boolean hasBeenUsedOnBlock = false;

    public HopperFilterItem(Item.Settings settings) {
        super(settings);
        stacks = DefaultedList.ofSize(5, ItemStack.EMPTY);
    }

    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            return  TypedActionResult.pass(user.getStackInHand(hand));
        } else {
            if (!hasBeenUsedOnBlock) {
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
            if (blockState.isOf(Blocks.HOPPER))
            {
                FilterEntityInterface hopperBlockEntity = (FilterEntityInterface)world.getBlockEntity(blockPos);

                NbtCompound nbtCompound = context.getStack().getNbt();
                boolean isEmpty = false;

                if(nbtCompound != null) {
                    DefaultedList<ItemStack> temp = DefaultedList.ofSize(5, ItemStack.EMPTY);
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
                        hopperBlockEntity.setFilterItems(temp);
                        hasBeenUsedOnBlock = true;
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }
        return ActionResult.PASS;
    }

    public void setStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
           stack.setCount(this.getMaxCountPerStack());
        }

        this.markDirty();
    }

    public ItemStack removeStack(int slot) {
        ItemStack itemStack = (ItemStack)this.stacks.get(slot);
        if (itemStack.isEmpty()) {
           return ItemStack.EMPTY;
        } else {
           this.stacks.set(slot, ItemStack.EMPTY);
           return itemStack;
        }
    }

    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.stacks, slot, amount);
        if (!itemStack.isEmpty()) {
           this.markDirty();
        }
  
        return itemStack;
    }

    public ItemStack getStack(int slot) {
        return slot >= 0 && slot < this.stacks.size() ? (ItemStack)this.stacks.get(slot) : ItemStack.EMPTY;
    }

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

    public void clear() {
        stacks = DefaultedList.ofSize(5, ItemStack.EMPTY);
    }

    public int size() {
        return 5;
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
            Inventories.writeNbt(nbtCompound, stacks);
            clear();
        }
        else {
            Inventories.writeNbt(nbtCompound, DefaultedList.ofSize(5, ItemStack.EMPTY));
        }
    }

    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext options) {
        NbtCompound nbtCompound = stack.getNbt();
        if (nbtCompound != null) {
           if (nbtCompound.contains("Items", 9)) {
              DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(5, ItemStack.EMPTY);
              Inventories.readNbt(nbtCompound, defaultedList);
              Iterator<ItemStack> tempStacks = defaultedList.iterator();
  
              while(tempStacks.hasNext()) {
                 ItemStack itemStack = (ItemStack)tempStacks.next();
                 if (!itemStack.isEmpty()) {
                    MutableText mutableText = itemStack.getName().shallowCopy();
                    tooltip.add(mutableText);
                 } 
                 else {
                    MutableText mutableText = new TranslatableText("Nothing");
                    tooltip.add(mutableText.formatted(Formatting.ITALIC));
                 }
              }
           }
        }
     }

    public NamedScreenHandlerFactory createScreenHandlerFactory(World world, BlockPos pos, Inventory owner) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            ItemStack stack = player.getStackInHand(player.getActiveHand());
            
            NbtCompound nbtCompound = stack.getNbt();

            if(nbtCompound != null) {
                DefaultedList<ItemStack> temp = DefaultedList.ofSize(5, ItemStack.EMPTY);
                Inventories.readNbt(nbtCompound, temp);
                stacks = temp;
            }
             
            var handler = new HopperScreenHandler(syncId, inventory, this);
            return handler;
        }, TITLE);
    }
}