package net.fabricmc.renew_auto;

import net.minecraft.util.Identifier;
import net.minecraft.entity.Entity;
import net.minecraft.util.registry.Registry;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class RenewAutoClientInitialize implements ClientModInitializer {
    public static final Identifier PacketID = new Identifier("renew_auto", "spawn_packet"); //Rename me please

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(RenewAutoInitialize.FISHING_BOBBER_ENTITY, (context) -> { return new DispenserFishingBobberEntityRenderer(context); });
		receiveEntityPacket();
    }

    public void receiveEntityPacket() {
		ClientPlayNetworking.registerGlobalReceiver(PacketID, (client, handler, byteBuf, responseSender) -> {
			if (client.world != null) {
				EntitySpawnS2CPacket spawnPacket = new EntitySpawnS2CPacket(byteBuf);
				Entity e = spawnPacket.getEntityTypeId().create(client.world);
				if (e == null)
					throw new IllegalStateException("Failed to create instance of entity \"" + Registry.ENTITY_TYPE.getId(spawnPacket.getEntityTypeId()) + "\"!");
				e.onSpawnPacket(spawnPacket);
				DispenserFishingBobberEntity bobber = (DispenserFishingBobberEntity)e;
				bobber.setOwnerPosition(EntitySpawnPacket.PacketBufUtil.readVec3d(byteBuf));
				client.world.addEntity(spawnPacket.getId(), e);
			}
		});
	}
}
