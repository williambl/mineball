package com.williambl.mineball;

import net.minecraft.resources.ResourceLocation;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mineball implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Mineball");

	public static final String MODID = "mineball";

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}

	@Override
	public void onInitialize(ModContainer mod) {
	}
}
