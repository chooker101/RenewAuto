package net.fabricmc.renew_auto.extension;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.world.BlockView;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(SpawnerBlock.class)
public abstract class SpawnerBlockExtension extends BlockWithEntity {
    public SpawnerBlockExtension(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }
}
