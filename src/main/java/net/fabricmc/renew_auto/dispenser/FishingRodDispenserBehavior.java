package net.fabricmc.renew_auto.dispenser;

import java.util.Random;
import net.minecraft.block.DispenserBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.world.WorldEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.fabricmc.renew_auto.DispenserFishingBobberEntity;

public class FishingRodDispenserBehavior extends FallibleItemDispenserBehavior {
    private DispenserFishingBobberEntity ownedBobber;

    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        World world = pointer.getWorld();
        if (!world.isClient()) {
            Direction direction = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
            double d = pointer.getX() + 0.5D * (double)direction.getOffsetX();
            double e = pointer.getY() + 0.5D * (double)direction.getOffsetY();
            double f = pointer.getZ() + 0.5D * (double)direction.getOffsetZ();
            Vec3d position = new Vec3d(d, e, f);
            
            if(ownedBobber == null){
                ownedBobber = new DispenserFishingBobberEntity(this, position, direction, world, EnchantmentHelper.getLure(stack), EnchantmentHelper.getLuckOfTheSea(stack));
                world.spawnEntity(ownedBobber);
                world.playSound((PlayerEntity)null, position.getX(), position.getY(), position.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            }
            else {
                ownedBobber.use(stack);
                world.playSound((PlayerEntity)null, position.getX(), position.getY(), position.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                if(EnchantmentHelper.getLevel(Enchantments.MENDING, stack) == 0) {
                    if(stack.damage(1, (Random)world.getRandom(), (ServerPlayerEntity)null)) {
                        stack.setCount(0);
                    }
                }
            }
            this.setSuccess(true);
        }
        return stack;
    }

    public void SetBobber(DispenserFishingBobberEntity bobber) {
        ownedBobber = bobber;
    }

    @Override
    protected void playSound(BlockPointer pointer) {
        if (!this.isSuccess()){
           pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_FAILS, pointer.getPos(), 0);
        }
    }
}
