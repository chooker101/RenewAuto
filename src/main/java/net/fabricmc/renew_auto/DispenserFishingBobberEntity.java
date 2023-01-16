package net.fabricmc.renew_auto;

import com.google.common.base.MoreObjects;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.GameRules;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.renew_auto.dispenser.FishingRodDispenserBehavior;

public class DispenserFishingBobberEntity extends Entity {
   private final Random velocityRandom;
   private boolean caughtFish;
   private int outOfOpenWaterTicks;
   private static final TrackedData<Integer> HOOK_ENTITY_ID;
   private static final TrackedData<Boolean> CAUGHT_FISH;
   private int removalTimer;
   private int hookCountdown;
   private int waitCountdown;
   private int fishTravelCountdown;
   private float fishAngle;
   private boolean inOpenWater;
   @Nullable
   private Entity hookedEntity;
   private DispenserFishingBobberEntity.State state;
   private final int luckOfTheSeaLevel;
   private final int lureLevel;
   private Vec3d ownerPosition;
   private FishingRodDispenserBehavior dispOwner;

   //Projectile 
   @Nullable
   private UUID ownerUuid;
   @Nullable
   private Entity owner;
   private boolean leftOwner;
   private boolean shot;

   private DispenserFishingBobberEntity(EntityType<? extends Entity> type, World world, int lureLevel, int luckOfTheSeaLevel) {
      super(type, world);
      this.velocityRandom = new Random();
      this.inOpenWater = true;
      this.state = DispenserFishingBobberEntity.State.FLYING;
      this.ignoreCameraFrustum = true;
      this.luckOfTheSeaLevel = Math.max(0, lureLevel);
      this.lureLevel = Math.max(0, luckOfTheSeaLevel);
   }

   public DispenserFishingBobberEntity(EntityType<? extends Entity> entityType, World world) {
      this(entityType, world, 0, 0);
   }

   public DispenserFishingBobberEntity(FishingRodDispenserBehavior owner, Vec3d position, Direction direction, World world, int lureLevel, int luckOfTheSeaLevel) {
      this(RenewAutoInitialize.FISHING_BOBBER_ENTITY, world, lureLevel, luckOfTheSeaLevel);
      double x = (double)direction.getOffsetX();
      double y = (double)((float)direction.getOffsetY() + 0.1F);
      double z = (double)direction.getOffsetZ();
      this.setVelocity(x, y, z, 1.1F, 6.0F);
      this.setYaw((float)(MathHelper.atan2(x, z) * 57.2957763671875D));
      this.setPitch((float)(MathHelper.atan2(y, Math.sqrt(x * x + z * z)) * 57.2957763671875D));
      ownerPosition = position;
      this.setPosition(ownerPosition.getX(), ownerPosition.getY(), ownerPosition.getZ());
      dispOwner = owner;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(HOOK_ENTITY_ID, 0);
      this.getDataTracker().startTracking(CAUGHT_FISH, false);
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (HOOK_ENTITY_ID.equals(data)) {
         int i = (Integer)this.getDataTracker().get(HOOK_ENTITY_ID);
         this.hookedEntity = i > 0 ? this.world.getEntityById(i - 1) : null;
      }

      if (CAUGHT_FISH.equals(data)) {
         this.caughtFish = (Boolean)this.getDataTracker().get(CAUGHT_FISH);
         if (this.caughtFish) {
            this.setVelocity(this.getVelocity().x, (double)(-0.4F * MathHelper.nextFloat(this.velocityRandom, 0.6F, 1.0F)), this.getVelocity().z);
         }
      }

      super.onTrackedDataSet(data);
   }

   public boolean shouldRender(double distance) {
      return distance < 4096.0D;
   }

   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
   }

   public void tick() {
      this.velocityRandom.setSeed(this.getUuid().getLeastSignificantBits() ^ this.world.getTime());
      pTick();
      if (this.onGround) {
         ++this.removalTimer;
         if (this.removalTimer >= 1200) {
            this.discard();
            return;
         }
      } else {
         this.removalTimer = 0;
      }

      if (!this.world.isClient && dispOwner == null) {
         this.discard();
         return;
      }

      float f = 0.0F;
      BlockPos blockPos = this.getBlockPos();
      FluidState fluidState = this.world.getFluidState(blockPos);
      if (fluidState.isIn(FluidTags.WATER)) {
         f = fluidState.getHeight(this.world, blockPos);
      }

      boolean bl = f > 0.0F;
      if (this.state == DispenserFishingBobberEntity.State.FLYING) {
         if (this.hookedEntity != null) {
            this.setVelocity(Vec3d.ZERO);
            this.state = DispenserFishingBobberEntity.State.HOOKED_IN_ENTITY;
            return;
         }

         if (bl) {
            this.setVelocity(this.getVelocity().multiply(0.3D, 0.2D, 0.3D));
            this.state = DispenserFishingBobberEntity.State.BOBBING;
            return;
         }

         this.checkForCollision();
      } else {
         if (this.state == DispenserFishingBobberEntity.State.HOOKED_IN_ENTITY) {
            if (this.hookedEntity != null) {
               if (!this.hookedEntity.isRemoved() && this.hookedEntity.world.getRegistryKey() == this.world.getRegistryKey()) {
                  this.setPosition(this.hookedEntity.getX(), this.hookedEntity.getBodyY(0.8D), this.hookedEntity.getZ());
               } else {
                  this.updateHookedEntityId((Entity)null);
                  this.state = DispenserFishingBobberEntity.State.FLYING;
               }
            }

            return;
         }

         if (this.state == DispenserFishingBobberEntity.State.BOBBING) {
            Vec3d vec3d = this.getVelocity();
            double d = this.getY() + vec3d.y - (double)blockPos.getY() - (double)f;
            if (Math.abs(d) < 0.01D) {
               d += Math.signum(d) * 0.1D;
            }

            this.setVelocity(vec3d.x * 0.9D, vec3d.y - d * (double)this.random.nextFloat() * 0.2D, vec3d.z * 0.9D);
            if (this.hookCountdown <= 0 && this.fishTravelCountdown <= 0) {
               this.inOpenWater = true;
            } else {
               this.inOpenWater = this.inOpenWater && this.outOfOpenWaterTicks < 10 && this.isOpenOrWaterAround(blockPos);
            }

            if (bl) {
               this.outOfOpenWaterTicks = Math.max(0, this.outOfOpenWaterTicks - 1);
               if (this.caughtFish) {
                  this.setVelocity(this.getVelocity().add(0.0D, -0.1D * (double)this.velocityRandom.nextFloat() * (double)this.velocityRandom.nextFloat(), 0.0D));
               }

               if (!this.world.isClient) {
                  this.tickFishingLogic(blockPos);
               }
               
            } else {
               this.outOfOpenWaterTicks = Math.min(10, this.outOfOpenWaterTicks + 1);
            }
         }
      }

      if (!fluidState.isIn(FluidTags.WATER)) {
         this.setVelocity(this.getVelocity().add(0.0D, -0.03D, 0.0D));
      }

      this.move(MovementType.SELF, this.getVelocity());
      this.updateRotation();
      if (this.state == DispenserFishingBobberEntity.State.FLYING && (this.onGround || this.horizontalCollision)) {
         this.setVelocity(Vec3d.ZERO);
      }

      this.setVelocity(this.getVelocity().multiply(0.92D));
      this.refreshPosition();
      
   }

   private void checkForCollision() {
      HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
      this.onCollision(hitResult);
   }

   protected boolean canHit(Entity entity) {
      return pCanHit(entity) || entity.isAlive() && entity instanceof ItemEntity;
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      if (!this.world.isClient) {
         this.updateHookedEntityId(entityHitResult.getEntity());
      }

   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      this.setVelocity(this.getVelocity().normalize().multiply(blockHitResult.squaredDistanceTo(this)));
   }

   private void updateHookedEntityId(@Nullable Entity entity) {
      this.hookedEntity = entity;
      this.getDataTracker().set(HOOK_ENTITY_ID, entity == null ? 0 : entity.getId() + 1);
   }

   private void tickFishingLogic(BlockPos pos) {
      ServerWorld serverWorld = (ServerWorld)this.world;
      int i = 1;
      BlockPos blockPos = pos.up();
      if (this.random.nextFloat() < 0.25F && this.world.hasRain(blockPos)) {
         ++i;
      }

      if (this.random.nextFloat() < 0.5F && !this.world.isSkyVisible(blockPos)) {
         --i;
      }

      if (this.hookCountdown > 0) {
         --this.hookCountdown;
         if (this.hookCountdown <= 0) {
            this.waitCountdown = 0;
            this.fishTravelCountdown = 0;
            this.getDataTracker().set(CAUGHT_FISH, false);
         }
      } else {
         float n;
         float o;
         float p;
         double q;
         double r;
         double s;
         BlockState blockState2;
         if (this.fishTravelCountdown > 0) {
            this.fishTravelCountdown -= i;
            if (this.fishTravelCountdown > 0) {
               this.fishAngle = (float)((double)this.fishAngle + this.random.nextGaussian() * 4.0D);
               n = this.fishAngle * 0.017453292F;
               o = MathHelper.sin(n);
               p = MathHelper.cos(n);
               q = this.getX() + (double)(o * (float)this.fishTravelCountdown * 0.1F);
               r = (double)((float)MathHelper.floor(this.getY()) + 1.0F);
               s = this.getZ() + (double)(p * (float)this.fishTravelCountdown * 0.1F);
               blockState2 = serverWorld.getBlockState(new BlockPos(q, r - 1.0D, s));
               if (blockState2.isOf(Blocks.WATER)) {
                  if (this.random.nextFloat() < 0.15F) {
                     serverWorld.spawnParticles(ParticleTypes.BUBBLE, q, r - 0.10000000149011612D, s, 1, (double)o, 0.1D, (double)p, 0.0D);
                  }

                  float k = o * 0.04F;
                  float l = p * 0.04F;
                  serverWorld.spawnParticles(ParticleTypes.FISHING, q, r, s, 0, (double)l, 0.01D, (double)(-k), 1.0D);
                  serverWorld.spawnParticles(ParticleTypes.FISHING, q, r, s, 0, (double)(-l), 0.01D, (double)k, 1.0D);
               }
            } else {
               this.playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
               double m = this.getY() + 0.5D;
               serverWorld.spawnParticles(ParticleTypes.BUBBLE, this.getX(), m, this.getZ(), (int)(1.0F + this.getWidth() * 20.0F), (double)this.getWidth(), 0.0D, (double)this.getWidth(), 0.20000000298023224D);
               serverWorld.spawnParticles(ParticleTypes.FISHING, this.getX(), m, this.getZ(), (int)(1.0F + this.getWidth() * 20.0F), (double)this.getWidth(), 0.0D, (double)this.getWidth(), 0.20000000298023224D);
               this.hookCountdown = MathHelper.nextInt(this.random, 20, 40);
               this.getDataTracker().set(CAUGHT_FISH, true);
            }
         } else if (this.waitCountdown > 0) {
            this.waitCountdown -= i;
            n = 0.15F;
            if (this.waitCountdown < 20) {
               n = (float)((double)n + (double)(20 - this.waitCountdown) * 0.05D);
            } else if (this.waitCountdown < 40) {
               n = (float)((double)n + (double)(40 - this.waitCountdown) * 0.02D);
            } else if (this.waitCountdown < 60) {
               n = (float)((double)n + (double)(60 - this.waitCountdown) * 0.01D);
            }

            if (this.random.nextFloat() < n) {
               o = MathHelper.nextFloat(this.random, 0.0F, 360.0F) * 0.017453292F;
               p = MathHelper.nextFloat(this.random, 25.0F, 60.0F);
               q = this.getX() + (double)(MathHelper.sin(o) * p * 0.1F);
               r = (double)((float)MathHelper.floor(this.getY()) + 1.0F);
               s = this.getZ() + (double)(MathHelper.cos(o) * p * 0.1F);
               blockState2 = serverWorld.getBlockState(new BlockPos(q, r - 1.0D, s));
               if (blockState2.isOf(Blocks.WATER)) {
                  serverWorld.spawnParticles(ParticleTypes.SPLASH, q, r, s, 2 + this.random.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D);
               }
            }

            if (this.waitCountdown <= 0) {
               this.fishAngle = MathHelper.nextFloat(this.random, 0.0F, 360.0F);
               this.fishTravelCountdown = MathHelper.nextInt(this.random, 20, 80);
            }
         } else {
            this.waitCountdown = MathHelper.nextInt(this.random, 100, 600);
            this.waitCountdown -= this.lureLevel * 20 * 5;
         }
      }

   }

   private boolean isOpenOrWaterAround(BlockPos pos) {
      DispenserFishingBobberEntity.PositionType positionType = DispenserFishingBobberEntity.PositionType.INVALID;

      for(int i = -1; i <= 2; ++i) {
         DispenserFishingBobberEntity.PositionType positionType2 = this.getPositionType(pos.add(-2, i, -2), pos.add(2, i, 2));
         switch(positionType2) {
         case INVALID:
            return false;
         case ABOVE_WATER:
            if (positionType == DispenserFishingBobberEntity.PositionType.INVALID) {
               return false;
            }
            break;
         case INSIDE_WATER:
            if (positionType == DispenserFishingBobberEntity.PositionType.ABOVE_WATER) {
               return false;
            }
         }

         positionType = positionType2;
      }

      return true;
   }

   private DispenserFishingBobberEntity.PositionType getPositionType(BlockPos start, BlockPos end) {
      return (DispenserFishingBobberEntity.PositionType)BlockPos.stream(start, end).map(this::getPositionType).reduce((positionType, positionType2) -> {
         return positionType == positionType2 ? positionType : DispenserFishingBobberEntity.PositionType.INVALID;
      }).orElse(DispenserFishingBobberEntity.PositionType.INVALID);
   }

   private DispenserFishingBobberEntity.PositionType getPositionType(BlockPos pos) {
      BlockState blockState = this.world.getBlockState(pos);
      if (!blockState.isAir() && !blockState.isOf(Blocks.LILY_PAD)) {
         FluidState fluidState = blockState.getFluidState();
         return fluidState.isIn(FluidTags.WATER) && fluidState.isStill() && blockState.getCollisionShape(this.world, pos).isEmpty() ? DispenserFishingBobberEntity.PositionType.INSIDE_WATER : DispenserFishingBobberEntity.PositionType.INVALID;
      } else {
         return DispenserFishingBobberEntity.PositionType.ABOVE_WATER;
      }
   }

   public boolean isInOpenWater() {
      return this.inOpenWater;
   }

   public int use(ItemStack usedItem) {
      //PlayerEntity playerEntity = this.getPlayerOwner();
      if (!this.world.isClient) {
         int i = 0;
         if (this.hookedEntity != null) {
            this.pullHookedEntity(this.hookedEntity);
            //Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, usedItem, this, Collections.emptyList());
            this.world.sendEntityStatus(this, (byte)31);
            i = this.hookedEntity instanceof ItemEntity ? 3 : 5;
         } else if (this.hookCountdown > 0) {
            LootContext.Builder builder = (new LootContext.Builder((ServerWorld)this.world)).parameter(LootContextParameters.ORIGIN, this.getPos()).parameter(LootContextParameters.TOOL, usedItem).parameter(LootContextParameters.THIS_ENTITY, this).random(this.random).luck((float)this.luckOfTheSeaLevel);
            LootTable lootTable = this.world.getServer().getLootManager().getTable(LootTables.FISHING_GAMEPLAY);
            List<ItemStack> list = lootTable.generateLoot(builder.build(LootContextTypes.FISHING));
            //Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, usedItem, this, list);
            Iterator<ItemStack> var7 = list.iterator();

            while(var7.hasNext()) {
               ItemStack itemStack = (ItemStack)var7.next();
               ItemEntity itemEntity = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), itemStack);
               double d = ownerPosition.getX() - this.getX();
               double e = ownerPosition.getY() - this.getY();
               double f = ownerPosition.getZ() - this.getZ();
               itemEntity.setVelocity(d * 0.1D, e * 0.1D + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08D, f * 0.1D);
               this.world.spawnEntity(itemEntity);
               //world.spawnEntity(new ExperienceOrbEntity(world, ownerPosition.getX(), ownerPosition.getY() + 0.5D, ownerPosition.getZ() + 0.5D, this.random.nextInt(6) + 1));
            }

            i = 1;
         }

         if (this.onGround) {
            i = 2;
         }

         this.discard();
         return i;
      } else {
         return 0;
      }
   }

   public void handleStatus(byte status) {
      if (status == 31 && this.world.isClient && this.hookedEntity instanceof PlayerEntity && ((PlayerEntity)this.hookedEntity).isMainPlayer()) {
         this.pullHookedEntity(this.hookedEntity);
      }

      super.handleStatus(status);
   }

   protected void pullHookedEntity(Entity entity) {
      Vec3d vec3d = (new Vec3d(ownerPosition.getX() - this.getX(), ownerPosition.getY() - this.getY(), ownerPosition.getZ() - this.getZ())).multiply(0.1D);
      entity.setVelocity(entity.getVelocity().add(vec3d));
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.NONE;
   }

   public void remove(Entity.RemovalReason reason) {
      if(dispOwner != null) {
         dispOwner.SetBobber(null);
      }
      super.remove(reason);
   }

   public void onRemoved() {
      dispOwner.SetBobber(null);
   }

   public void setOwner(@Nullable Entity entity) {
      pSetOwner(entity);
      this.setPlayerFishHook(this);
   }

   public void setOwnerPosition(Vec3d pos) {
      ownerPosition = pos;
   }

   public Vec3d getOwnerPosition() {
      return ownerPosition;
   }

   private void setPlayerFishHook(@Nullable DispenserFishingBobberEntity fishingBobber) {
      PlayerEntity playerEntity = this.getPlayerOwner();
      if (playerEntity != null) {
         //playerEntity.fishHook = fishingBobber;
      }

   }

   @Nullable
   public PlayerEntity getPlayerOwner() {
      Entity entity = this.getOwner();
      return entity instanceof PlayerEntity ? (PlayerEntity)entity : null;
   }

   @Nullable
   public Entity getHookedEntity() {
      return this.hookedEntity;
   }

   public boolean canUsePortals() {
      return false;
   }

   public Packet<?> createSpawnPacket() {
      //Entity entity = this.getOwner();
      return EntitySpawnPacket.create(this, RenewAutoClientInitialize.PacketID);
      //return new EntitySpawnS2CPacket(this, this.getId());
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      pOnSpawnPacket(packet);
      //if (this.getPlayerOwner() == null) {
      //   int i = packet.getEntityData();
      //   LOGGER.error((String)"Failed to recreate fishing hook on client. {} (id: {}) is not a valid owner.", (Object)this.world.getEntityById(i), (Object)i);
      //   this.kill();
      //}

   }

   //Projectile entity
   public void pTick() {
      if (!this.shot) {
         this.emitGameEvent(GameEvent.PROJECTILE_SHOOT, null, this.getBlockPos());
         this.shot = true;
      }

      if (!this.leftOwner) {
         this.leftOwner = this.shouldLeaveOwner();
      }

      super.tick();
   }

   public void pSetOwner(@Nullable Entity entity) {
      if (entity != null) {
         this.ownerUuid = entity.getUuid();
         this.owner = entity;
      }

   }

   @Nullable
   public Entity getOwner() {
      if (this.owner != null && !this.owner.isRemoved()) {
         return this.owner;
      } else if (this.ownerUuid != null && this.world instanceof ServerWorld) {
         this.owner = ((ServerWorld)this.world).getEntity(this.ownerUuid);
         return this.owner;
      } else {
         return null;
      }
   }

   /**
    * {@return the cause entity of any effect applied by this projectile} If this
    * projectile has an owner, the effect is attributed to the owner; otherwise, it
    * is attributed to this projectile itself.
    */
   public Entity getEffectCause() {
      return (Entity)MoreObjects.firstNonNull(this.getOwner(), this);
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      if (this.ownerUuid != null) {
         nbt.putUuid("Owner", this.ownerUuid);
      }

      if (this.leftOwner) {
         nbt.putBoolean("LeftOwner", true);
      }

      nbt.putBoolean("HasBeenShot", this.shot);
   }

   protected boolean isOwner(Entity entity) {
      return entity.getUuid().equals(this.ownerUuid);
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      if (nbt.containsUuid("Owner")) {
         this.ownerUuid = nbt.getUuid("Owner");
      }

      this.leftOwner = nbt.getBoolean("LeftOwner");
      this.shot = nbt.getBoolean("HasBeenShot");
   }

   private boolean shouldLeaveOwner() {
      Entity entity = this.getOwner();
      if (entity != null) {
         Iterator<Entity> var2 = this.world.getOtherEntities(this, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0D), (entityx) -> {
            return !entityx.isSpectator() && entityx.collides();
         }).iterator();

         while(var2.hasNext()) {
            Entity entity2 = (Entity)var2.next();
            if (entity2.getRootVehicle() == entity.getRootVehicle()) {
               return false;
            }
         }
      }

      return true;
   }

   public void setVelocity(double x, double y, double z, float speed, float divergence) {
      Vec3d vec3d = (new Vec3d(x, y, z)).normalize().add(this.random.nextGaussian() * 0.007499999832361937D * (double)divergence, this.random.nextGaussian() * 0.007499999832361937D * (double)divergence, this.random.nextGaussian() * 0.007499999832361937D * (double)divergence).multiply((double)speed);
      this.setVelocity(vec3d);
      double d = vec3d.horizontalLength();
      this.setYaw((float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D));
      this.setPitch((float)(MathHelper.atan2(vec3d.y, d) * 57.2957763671875D));
      this.prevYaw = this.getYaw();
      this.prevPitch = this.getPitch();
   }

   public void setProperties(Entity user, float pitch, float yaw, float roll, float modifierZ, float modifierXYZ) {
      float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
      float g = -MathHelper.sin((pitch + roll) * 0.017453292F);
      float h = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
      this.setVelocity((double)f, (double)g, (double)h, modifierZ, modifierXYZ);
      Vec3d vec3d = user.getVelocity();
      this.setVelocity(this.getVelocity().add(vec3d.x, user.isOnGround() ? 0.0D : vec3d.y, vec3d.z));
   }

   protected void onCollision(HitResult hitResult) {
      HitResult.Type type = hitResult.getType();
      if (type == HitResult.Type.ENTITY) {
         this.onEntityHit((EntityHitResult)hitResult);
      } else if (type == HitResult.Type.BLOCK) {
         this.onBlockHit((BlockHitResult)hitResult);
      }

      if (type != HitResult.Type.MISS) {
         this.emitGameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
      }

   }

   public void setVelocityClient(double x, double y, double z) {
      this.setVelocity(x, y, z);
      if (this.prevPitch == 0.0F && this.prevYaw == 0.0F) {
         double d = Math.sqrt(x * x + z * z);
         this.setPitch((float)(MathHelper.atan2(y, d) * 57.2957763671875D));
         this.setYaw((float)(MathHelper.atan2(x, z) * 57.2957763671875D));
         this.prevPitch = this.getPitch();
         this.prevYaw = this.getYaw();
         this.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
      }

   }

   protected boolean pCanHit(Entity entity) {
      if (!entity.isSpectator() && entity.isAlive() && entity.collides()) {
         Entity entity2 = this.getOwner();
         return entity2 == null || this.leftOwner || !entity2.isConnectedThroughVehicle(entity);
      } else {
         return false;
      }
   }

   protected void updateRotation() {
      Vec3d vec3d = this.getVelocity();
      double d = vec3d.horizontalLength();
      this.setPitch(updateRotation(this.prevPitch, (float)(MathHelper.atan2(vec3d.y, d) * 57.2957763671875D)));
      this.setYaw(updateRotation(this.prevYaw, (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D)));
   }

   protected static float updateRotation(float prevRot, float newRot) {
      while(newRot - prevRot < -180.0F) {
         prevRot -= 360.0F;
      }

      while(newRot - prevRot >= 180.0F) {
         prevRot += 360.0F;
      }

      return MathHelper.lerp(0.2F, prevRot, newRot);
   }

   public void pOnSpawnPacket(EntitySpawnS2CPacket packet) {
      super.onSpawnPacket(packet);
      Entity entity = this.world.getEntityById(packet.getEntityData());
      if (entity != null) {
         this.setOwner(entity);
      }

   }

   public boolean canModifyAt(World world, BlockPos pos) {
      Entity entity = this.getOwner();
      if (entity instanceof PlayerEntity) {
         return entity.canModifyAt(world, pos);
      } else {
         return entity == null || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
      }
   }

   static {
      HOOK_ENTITY_ID = DataTracker.registerData(DispenserFishingBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CAUGHT_FISH = DataTracker.registerData(DispenserFishingBobberEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   static enum State {
      FLYING,
      HOOKED_IN_ENTITY,
      BOBBING;
   }

   private static enum PositionType {
      ABOVE_WATER,
      INSIDE_WATER,
      INVALID;
   }
}
