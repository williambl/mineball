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
				wasKeyDown = false;
				ClientPlayNetworking.send(Kicking.PACKET_ID, Kicking.createPacket(client.player.getDeltaMovement().normalize()));
			}
		}
	}
}
