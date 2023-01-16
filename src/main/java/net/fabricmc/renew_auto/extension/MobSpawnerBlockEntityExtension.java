package net.fabricmc.renew_auto.extension;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(MobSpawnerBlockEntity.class)
public abstract class MobSpawnerBlockEntityExtension extends BlockEntity {
    public MobSpawnerBlockEntityExtension(BlockPos pos, BlockState state) {
        super(BlockEntityType.MOB_SPAWNER, pos, state);
    }

    @Override
    public boolean copyItemDataRequiresOperator() {
        return false;
    }
}
