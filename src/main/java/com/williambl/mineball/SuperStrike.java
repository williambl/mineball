package com.williambl.mineball;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.Comparator;

import static com.williambl.mineball.MineballMod.id;

public class SuperStrike {
	public static final ResourceLocation PACKET_ID = id("super_strike");

	public static FriendlyByteBuf createPacket(int distance) {
		return PacketByteBufs.create().writeVarInt(distance);
	}

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(PACKET_ID, (server, player, handler, buf, responseSender) -> {
			var distance = buf.readVarInt();
			server.execute(() -> player.level.getEntities(EntityTypeTest.forClass(Mineball.class), player.getBoundingBox().inflate(0.5), EntitySelector.pushableBy(player)).stream()
					.min(Comparator.comparing(player::distanceToSqr))
					.ifPresent(ball -> {
						ball.kick(player, 0.4, factorFromDistance(distance));
						ball.setSuperStriking();
						player.getLevel().sendParticles(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), 10, player.getBbWidth(), player.getBbHeight(), player.getBbWidth(), 0.1);
					}));
		});
	}

	public static double factorFromDistance(double distance) {
		return (Math.min(40 - Math.abs(distance), 40) / 40.0) * 3.0;
	}
}
