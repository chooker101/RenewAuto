package net.fabricmc.renew_auto;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface FilterEntityInterface {
    void setFilterItems(DefaultedList<ItemStack> filterItems);
}