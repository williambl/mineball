package com.williambl.mineball;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents;

public class MineballModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		EntityRendererRegistry.register(MineballMod.MINEBALL, context -> new ThrownItemRenderer<>(context, 1.5f, false));

		ClientWorldTickEvents.START.register(SuperStrikeClient.INSTANCE);
		ClientWorldTickEvents.START.register(KickingClient.INSTANCE);
	}
}
