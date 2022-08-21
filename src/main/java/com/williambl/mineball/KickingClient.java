package com.williambl.mineball;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.function.UnaryOperator;

@Environment(EnvType.CLIENT)
public class KickingClient implements ClientWorldTickEvents.Start {
	public static final KickingClient INSTANCE = new KickingClient();
	public static final KeyMapping KICKING_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.mineball.kick", GLFW.GLFW_KEY_M, "key.categories.mineball"));
	private static boolean wasKeyDown = false;

	@Override
	public void startWorldTick(Minecraft client, ClientLevel level) {
		if (KICKING_KEY.isDown()) {
			wasKeyDown = true;
		} else {
			if (wasKeyDown) {
				var forwards = client.player.getForward();
				ClientPlayNetworking.send(Kicking.PACKET_ID, Kicking.createPacket(forwards.yRot(
					rotationFromKeys(client)
				)));
			}
		}
	}

	private static float rotationFromKeys(Minecraft client) {
		int horizontal = getHorizontalDirection(client);
		int vertical = getVerticalDirection(client);

		if (horizontal == -1) {
			return (float) switch (vertical) {
				case -1 -> 1.25 * Math.PI;
				case 1  -> 1.75 * Math.PI;
				default -> 1.50 * Math.PI;
			};
		} else if (horizontal == 1) {
			return (float) switch (vertical) {
				case -1 -> 0.75 * Math.PI;
				case 1  -> 0.25 * Math.PI;
				default -> 0.50 * Math.PI;
			};
		} else {
			return vertical == -1 ? (float) Math.PI : 0;
		}
	}

	private static int getHorizontalDirection(Minecraft client) {
		int dir = 0;

		if (client.options.keyLeft.isDown()) {
			dir--;
		}
		if (client.options.keyRight.isDown()) {
			dir++;
		}

		return dir;
	}

	private static int getVerticalDirection(Minecraft client) {
		int dir = 0;

		if (client.options.keyUp.isDown()) {
			dir--;
		}
		if (client.options.keyDown.isDown()) {
			dir++;
		}

		return dir;
	}
}
