package net.fabricmc.renew_auto;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
//import net.minecraft.util.registry.Registry;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Identifier;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class EntitySpawnPacket {
    public static Packet<?> create(DispenserFishingBobberEntity e, Identifier packetID) {
		if (e.world.isClient)
			throw new IllegalStateException("SpawnPacketUtil.create called on the logical client!");
		PacketByteBuf byteBuf = new PacketByteBuf(Unpooled.buffer());
		EntitySpawnS2CPacket spawnPacket = new EntitySpawnS2CPacket(e.getId(), e.getUuid(), e.getX(), e.getY(), e.getZ(), e.getPitch(), e.getYaw(), e.getType(), e.getId(), e.getVelocity());
		spawnPacket.write(byteBuf);
		PacketBufUtil.writeVec3d(byteBuf, new Vec3d(e.getOwnerPosition().getX(), e.getOwnerPosition().getY(), e.getOwnerPosition().getZ()));
		return ServerPlayNetworking.createS2CPacket(packetID, byteBuf);
	}
	public static final class PacketBufUtil {
 
		/**
		 * Packs a floating-point angle into a {@code byte}.
		 *
		 * @param angle
		 *         angle
		 * @return packed angle
		 */
		public static byte packAngle(float angle) {
			return (byte) MathHelper.floor(angle * 256 / 360);
		}
 
		/**
		 * Unpacks a floating-point angle from a {@code byte}.
		 *
		 * @param angleByte
		 *         packed angle
		 * @return angle
		 */
		public static float unpackAngle(byte angleByte) {
			return (angleByte * 360) / 256f;
		}
 
		/**
		 * Writes an angle to a {@link PacketByteBuf}.
		 *
		 * @param byteBuf
		 *         destination buffer
		 * @param angle
		 *         angle
		 */
		public static void writeAngle(PacketByteBuf byteBuf, float angle) {
			byteBuf.writeByte(packAngle(angle));
		}
 
		/**
		 * Reads an angle from a {@link PacketByteBuf}.
		 *
		 * @param byteBuf
		 *         source buffer
		 * @return angle
		 */
		public static float readAngle(PacketByteBuf byteBuf) {
			return unpackAngle(byteBuf.readByte());
		}
 
		/**
		 * Writes a {@link Vec3d} to a {@link PacketByteBuf}.
		 *
		 * @param byteBuf
		 *         destination buffer
		 * @param vec3d
		 *         vector
		 */
		public static void writeVec3d(PacketByteBuf byteBuf, Vec3d vec3d) {
			byteBuf.writeDouble(vec3d.x);
			byteBuf.writeDouble(vec3d.y);
			byteBuf.writeDouble(vec3d.z);
		}
 
		/**
		 * Reads a {@link Vec3d} from a {@link PacketByteBuf}.
		 *
		 * @param byteBuf
		 *         source buffer
		 * @return vector
		 */
		public static Vec3d readVec3d(PacketByteBuf byteBuf) {
			double x = byteBuf.readDouble();
			double y = byteBuf.readDouble();
			double z = byteBuf.readDouble();
			return new Vec3d(x, y, z);
		}
	}
}
