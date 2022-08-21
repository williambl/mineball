package com.williambl.mineball;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.function.UnaryOperator;

@Environment(EnvType.CLIENT)
public class SuperStrikeClient implements ClientWorldTickEvents.Start {
	public static final SuperStrikeClient INSTANCE = new SuperStrikeClient();
	public static final KeyMapping SUPER_STRIKE_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.mineball.super_strike", GLFW.GLFW_KEY_N, "key.categories.mineball"));
	private static int ticksHeldFor = 0;
	private static int target = 0;

	public static boolean isActive() {
		return ticksHeldFor > 0;
	}

	@Override
	public void startWorldTick(Minecraft client, ClientLevel level) {
		if (SUPER_STRIKE_KEY.isDown()) {
			if (ticksHeldFor == 0 || target == 0) {
				target = level.getRandom().nextIntBetweenInclusive(20, 40);
			}

			ticksHeldFor++;
			var factor = SuperStrike.factorFromDistance(ticksHeldFor - target);
			client.player.displayClientMessage(
					Component.translatable(
							"message.mineball.super_strike",
							factor < 1 ? "" : "|".repeat((int) factor)
					).withStyle(withFactorColouring(factor)),
					true
			);
		} else {
			if (ticksHeldFor > 0) {
				int distance = target - ticksHeldFor;
				ClientPlayNetworking.send(SuperStrike.PACKET_ID, SuperStrike.createPacket(distance));
				ticksHeldFor = 0;
				target = 0;

				var factor = SuperStrike.factorFromDistance(distance);
				client.player.displayClientMessage(
						Component.translatable(
								"message.mineball.super_strike",
								factor < 1 ? "" : "*".repeat((int) factor)
						).withStyle(withFactorColouring(factor)),
						true
				);
			}
		}
	}

	private static UnaryOperator<Style> withFactorColouring(double factor) {
		if (factor > 2.8) {
			return s -> s.withColor(ChatFormatting.GREEN);
		}
		if (factor > 2.0) {
			return s -> s.withColor(ChatFormatting.YELLOW);
		}
		if (factor > 1.0) {
			return s -> s.withColor(TextColor.fromRgb(0xf49d3a));
		}
		return s -> s.withColor(ChatFormatting.DARK_RED);
	}
}
