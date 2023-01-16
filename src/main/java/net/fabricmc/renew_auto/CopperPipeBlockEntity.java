package net.fabricmc.renew_auto;

import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;

public class CopperPipeBlockEntity extends LootableContainerBlockEntity implements Hopper {
   private DefaultedList<ItemStack> inventory;
   private int transferCooldown;
   private long lastTickTime;

   public CopperPipeBlockEntity(BlockPos pos, BlockState state) {
      super(RenewAutoInitialize.COPPER_PIPE_BLOCK_ENTITY, pos, state);
      this.inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
      this.transferCooldown = -1;
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
      if (!this.deserializeLootTable(nbt)) {
         Inventories.readNbt(nbt, this.inventory);
      }

      this.transferCooldown = nbt.getInt("TransferCooldown");
   }

   public void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (!this.serializeLootTable(nbt)) {
         Inventories.writeNbt(nbt, this.inventory);
      }

      nbt.putInt("TransferCooldown", this.transferCooldown);
   }

   public int size() {
      return this.inventory.size();
   }

   public ItemStack removeStack(int slot, int amount) {
      this.checkLootInteraction((PlayerEntity)null);
      return Inventories.splitStack(this.getInvStackList(), slot, amount);
   }

   public void setStack(int slot, ItemStack stack) {
      this.checkLootInteraction((PlayerEntity)null);
      this.getInvStackList().set(slot, stack);
      if (stack.getCount() > this.getMaxCountPerStack()) {
         stack.setCount(this.getMaxCountPerStack());
      }

   }

   protected Text getContainerName() {
      return new TranslatableText(getCachedState().getBlock().getTranslationKey());
   }

   public static void serverTick(World world, BlockPos pos, BlockState state, CopperPipeBlockEntity blockEntity) {
      --blockEntity.transferCooldown;
      blockEntity.lastTickTime = world.getTime();
      if (!blockEntity.needsCooldown()) {
         blockEntity.setCooldown(0);
         insertAndExtract(world, pos, state, blockEntity, () -> {
            return extract((World)world, (Hopper)blockEntity);
         });
      }

   }

   private static boolean insertAndExtract(World world, BlockPos pos, BlockState state, CopperPipeBlockEntity blockEntity, BooleanSupplier booleanSupplier) {
      if (world.isClient) {
         return false;
      } else {
         if (!blockEntity.needsCooldown() && (Boolean)state.get(CopperPipeBlock.ENABLED)) {
            boolean bl = false;
            if (!blockEntity.isEmpty()) {
               bl = insert(world, pos, state, blockEntity);
            }

            if (!blockEntity.isFull()) {
               bl |= booleanSupplier.getAsBoolean();
            }

            if (bl) {
               blockEntity.setCooldown(8);
               markDirty(world, pos, state);
               return true;
            }
         }

         return false;
      }
   }

   private boolean isFull() {
      Iterator<ItemStack> var1 = this.inventory.iterator();

      ItemStack itemStack;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         itemStack = (ItemStack)var1.next();
      } while(!itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxCount());

      return false;
   }

   private static boolean insert(World world, BlockPos pos, BlockState state, Inventory inventory) {
      Inventory inventory2 = getOutputInventory(world, pos, state);
      if (inventory2 == null) {
         return false;
      } else {
         Direction direction = ((Direction)state.get(CopperPipeBlock.FACING)).getOpposite();
         if (isInventoryFull(inventory2, direction)) {
            return false;
         } else {
            for(int i = 0; i < inventory.size(); ++i) {
               if (!inventory.getStack(i).isEmpty()) {
                  ItemStack itemStack = inventory.getStack(i).copy();
                  ItemStack itemStack2 = transfer(inventory, inventory2, inventory.removeStack(i, 1), direction);
                  if (itemStack2.isEmpty()) {
                     inventory2.markDirty();
                     return true;
                  }

                  inventory.setStack(i, itemStack);
               }
            }

            return false;
         }
      }
   }

   private static IntStream getAvailableSlots(Inventory inventory, Direction side) {
      return inventory instanceof SidedInventory ? IntStream.of(((SidedInventory)inventory).getAvailableSlots(side)) : IntStream.range(0, inventory.size());
   }

   private static boolean isInventoryFull(Inventory inventory, Direction direction) {
      return getAvailableSlots(inventory, direction).allMatch((i) -> {
         ItemStack itemStack = inventory.getStack(i);
         return itemStack.getCount() >= itemStack.getMaxCount();
      });
   }

   private static boolean isInventoryEmpty(Inventory inv, Direction facing) {
      return getAvailableSlots(inv, facing).allMatch((i) -> {
         return inv.getStack(i).isEmpty();
      });
   }

   public static boolean extract(World world, Hopper hopper) {
      Inventory inventory = getInputInventory(world, hopper);
      if (inventory != null) {
         Direction direction = Direction.DOWN;
         return isInventoryEmpty(inventory, direction) ? false : getAvailableSlots(inventory, direction).anyMatch((i) -> {
            return extract(hopper, inventory, i, direction);
         });
      }
      return false;
   }

   private static boolean extract(Hopper hopper, Inventory inventory, int slot, Direction side) {
      ItemStack itemStack = inventory.getStack(slot);
      if (!itemStack.isEmpty() && canExtract(inventory, itemStack, slot, side)) {
         ItemStack itemStack2 = itemStack.copy();
         ItemStack itemStack3 = transfer(inventory, hopper, inventory.removeStack(slot, 1), (Direction)null);
         if (itemStack3.isEmpty()) {
            inventory.markDirty();
            return true;
         }

         inventory.setStack(slot, itemStack2);
      }

      return false;
   }

   public static boolean extract(Inventory inventory, ItemEntity itemEntity) {
      boolean bl = false;
      ItemStack itemStack = itemEntity.getStack().copy();
      ItemStack itemStack2 = transfer((Inventory)null, inventory, itemStack, (Direction)null);
      if (itemStack2.isEmpty()) {
         bl = true;
         itemEntity.discard();
      } else {
         itemEntity.setStack(itemStack2);
      }

      return bl;
   }

   public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
      if (to instanceof SidedInventory && side != null) {
         SidedInventory sidedInventory = (SidedInventory)to;
         int[] is = sidedInventory.getAvailableSlots(side);

         for(int i = 0; i < is.length && !stack.isEmpty(); ++i) {
            stack = transfer(from, to, stack, is[i], side);
         }
      } else {
         int j = to.size();

         for(int k = 0; k < j && !stack.isEmpty(); ++k) {
            stack = transfer(from, to, stack, k, side);
         }
      }

      return stack;
   }

   private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
      if (!inventory.isValid(slot, stack)) {
         return false;
      } else {
         return !(inventory instanceof SidedInventory) || ((SidedInventory)inventory).canInsert(slot, stack, side);
      }
   }

   private static boolean canExtract(Inventory inv, ItemStack stack, int slot, Direction facing) {
      return !(inv instanceof SidedInventory) || ((SidedInventory)inv).canExtract(slot, stack, facing);
   }

   private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction direction) {
      ItemStack itemStack = to.getStack(slot);
      if (canInsert(to, stack, slot, direction)) {
         boolean bl = false;
         boolean bl2 = to.isEmpty();
         if (itemStack.isEmpty()) {
            to.setStack(slot, stack);
            stack = ItemStack.EMPTY;
            bl = true;
         } else if (canMergeItems(itemStack, stack)) {
            int i = stack.getMaxCount() - itemStack.getCount();
            int j = Math.min(stack.getCount(), i);
            stack.decrement(j);
            itemStack.increment(j);
            bl = j > 0;
         }

         if (bl) {
            if (bl2 && to instanceof CopperPipeBlockEntity) {
                CopperPipeBlockEntity hopperBlockEntity = (CopperPipeBlockEntity)to;
               if (!hopperBlockEntity.isDisabled()) {
                  int k = 0;
                  if (from instanceof CopperPipeBlockEntity) {
                    CopperPipeBlockEntity hopperBlockEntity2 = (CopperPipeBlockEntity)from;
                     if (hopperBlockEntity.lastTickTime >= hopperBlockEntity2.lastTickTime) {
                        k = 1;
                     }
                  }

                  hopperBlockEntity.setCooldown(8 - k);
               }
            }

            to.markDirty();
         }
      }

      return stack;
   }

   @Nullable
   private static Inventory getOutputInventory(World world, BlockPos pos, BlockState state) {
      Direction direction = (Direction)state.get(CopperPipeBlock.FACING);
      return getInventoryAt(world, pos.offset(direction));
   }

   @Nullable
   private static Inventory getInputInventory(World world, Hopper hopper) {
      Inventory inv = null;
      BlockPos pos = new BlockPos(hopper.getHopperX(), hopper.getHopperY(), hopper.getHopperZ());
      Direction facing = world.getBlockState(pos).get(CopperPipeBlock.FACING);

      if(facing == Direction.NORTH) {
         inv = getInventoryAt(world, hopper.getHopperX(), hopper.getHopperY(), hopper.getHopperZ() + 1.0D);
      }
      if(facing == Direction.SOUTH) {
         inv = getInventoryAt(world, hopper.getHopperX(), hopper.getHopperY(), hopper.getHopperZ() - 1.0D);
      }
      if(facing == Direction.EAST) {
         inv = getInventoryAt(world, hopper.getHopperX() - 1.0D, hopper.getHopperY(), hopper.getHopperZ());
      }
      if(facing == Direction.WEST) {
         inv = getInventoryAt(world, hopper.getHopperX() + 1.0D, hopper.getHopperY(), hopper.getHopperZ());
      }
      if(facing == Direction.UP) {
         inv = getInventoryAt(world, hopper.getHopperX(), hopper.getHopperY() - 1.0D, hopper.getHopperZ());
      }
      if(facing == Direction.DOWN) {
         inv = getInventoryAt(world, hopper.getHopperX(), hopper.getHopperY() + 1.0D, hopper.getHopperZ());
      }

      return inv;
   }

   @Nullable
   public static Inventory getInventoryAt(World world, BlockPos pos) {
      return getInventoryAt(world, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D);
   }

   @Nullable
   private static Inventory getInventoryAt(World world, double x, double y, double z) {
      Inventory inventory = null;
      BlockPos blockPos = new BlockPos(x, y, z);
      BlockState blockState = world.getBlockState(blockPos);
      Block block = blockState.getBlock();
      if (block instanceof InventoryProvider) {
         inventory = ((InventoryProvider)block).getInventory(blockState, world, blockPos);
      } else if (blockState.hasBlockEntity()) {
         BlockEntity blockEntity = world.getBlockEntity(blockPos);
         if (blockEntity instanceof Inventory) {
            inventory = (Inventory)blockEntity;
            if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
               inventory = ChestBlock.getInventory((ChestBlock)block, blockState, world, blockPos, true);
            }
         }
      }

      if (inventory == null) {
         List<Entity> list = world.getOtherEntities((Entity)null, new Box(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntityPredicates.VALID_INVENTORIES);
         if (!list.isEmpty()) {
            inventory = (Inventory)list.get(world.random.nextInt(list.size()));
         }
      }

      return (Inventory)inventory;
   }

   private static boolean canMergeItems(ItemStack first, ItemStack second) {
      if (!first.isOf(second.getItem())) {
         return false;
      } else if (first.getDamage() != second.getDamage()) {
         return false;
      } else if (first.getCount() > first.getMaxCount()) {
         return false;
      } else {
         return ItemStack.areNbtEqual(first, second);
      }
   }

   public double getHopperX() {
      return (double)this.pos.getX() + 0.5D;
   }

   public double getHopperY() {
      return (double)this.pos.getY() + 0.5D;
   }

   public double getHopperZ() {
      return (double)this.pos.getZ() + 0.5D;
   }

   private void setCooldown(int cooldown) {
      this.transferCooldown = cooldown;
   }

   private boolean needsCooldown() {
      return this.transferCooldown > 0;
   }

   private boolean isDisabled() {
      return this.transferCooldown > 8;
   }

   protected DefaultedList<ItemStack> getInvStackList() {
      return this.inventory;
   }

   protected void setInvStackList(DefaultedList<ItemStack> list) {
      this.inventory = list;
   }

   public static void onEntityCollided(World world, BlockPos pos, BlockState state, Entity entity, CopperPipeBlockEntity blockEntity) {
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new HopperScreenHandler(syncId, playerInventory, this);
   }
}
