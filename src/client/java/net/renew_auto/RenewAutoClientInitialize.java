package net.renew_auto;

import net.minecraft.util.Identifier;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class RenewAutoClientInitialize implements ClientModInitializer {
	public static final Identifier DISPENSER_BOBBER_SPAWN_PACKET_ID = new Identifier("renew_auto", "dispenser_bobber_spawn_packet");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(RenewAutoInitialize.FISHING_BOBBER_ENTITY, (context) -> { return new DispenserFishingBobberEntityRenderer(context); });
		receiveEntityPacket();
    }

    public void receiveEntityPacket() {
		ClientPlayNetworking.registerGlobalReceiver(RenewAutoInitialize.DISPENSER_BOBBER_SPAWN_PACKET_ID, (client, handler, byteBuf, responseSender) -> {
			if (client.world != null) {
				EntitySpawnS2CPacket spawnPacket = new EntitySpawnS2CPacket(byteBuf);
				Entity entity = spawnPacket.getEntityType().create(client.world);
				if (entity == null)
					throw new IllegalStateException("Failed to create instance of entity");
				entity.onSpawnPacket(spawnPacket);
				DispenserFishingBobberEntity bobber = (DispenserFishingBobberEntity)entity;
				bobber.setOwnerPosition(EntitySpawnPacket.PacketBufUtil.readVec3d(byteBuf));
				client.world.addEntity(entity);
			}
		});
	}
}
