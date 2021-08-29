package net.fabricmc.renew_auto;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;


public class FilterHelper {
    public static NbtCompound writeNbt(NbtCompound nbt, DefaultedList<ItemStack> stacks) {
        NbtList nbtList = new NbtList();

        for(int i = 0; i < stacks.size(); ++i) {
           ItemStack itemStack = (ItemStack)stacks.get(i);
           if (!itemStack.isEmpty()) {
              NbtCompound nbtCompound = new NbtCompound();
              nbtCompound.putByte("Slot", (byte)i);
              itemStack.writeNbt(nbtCompound);
              nbtList.add(nbtCompound);
           }
        }

        if (!nbtList.isEmpty()) {
           nbt.put("Filters", nbtList);
        }

        return nbt;
    }

    public static void readNbt(NbtCompound nbt, DefaultedList<ItemStack> stacks) {
        NbtList nbtList = nbt.getList("Filters", 10);

        for(int i = 0; i < nbtList.size(); ++i) {
           NbtCompound nbtCompound = nbtList.getCompound(i);
           int j = nbtCompound.getByte("Slot") & 255;
           if (j >= 0 && j < stacks.size()) {
              stacks.set(j, ItemStack.fromNbt(nbtCompound));
           }
        }
    }
}