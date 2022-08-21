package com.williambl.mineball;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.Comparator;

import static com.williambl.mineball.MineballMod.id;

public class Kicking {
	public static final ResourceLocation PACKET_ID = id("kick");

	public static FriendlyByteBuf createPacket(Vec3 direction) {
		var buf = PacketByteBufs.create();
		buf.writeDouble(direction.x());
		buf.writeDouble(direction.y());
		buf.writeDouble(direction.z());
		return buf;
	}

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(PACKET_ID, (server, player, handler, buf, responseSender) -> {
			var direction = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			server.execute(() -> player.level.getEntities(EntityTypeTest.forClass(Mineball.class), player.getBoundingBox().inflate(0.5), EntitySelector.pushableBy(player)).stream()
					.min(Comparator.comparing(player::distanceToSqr))
					.ifPresent(ball -> {
						ball.kick(player, direction, 0.4, 0.5);
					}));
		});
	}
}
