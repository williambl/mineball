package com.williambl.mineball;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.group.api.QuiltItemGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineballMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Mineball");

	public static final String MODID = "mineball";

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}


	public static CreativeModeTab TAB = QuiltItemGroup.createWithIcon(id("mineball"), MineballMod::getTabIcon);
	public static EntityType<Mineball> MINEBALL = Registry.register(Registry.ENTITY_TYPE, id("mineball"), FabricEntityTypeBuilder.create(MobCategory.AMBIENT, Mineball::new).dimensions(EntityDimensions.fixed(0.75f, 0.75f)).trackedUpdateRate(2).build());
	public static Item MINEBALL_ITEM = Registry.register(Registry.ITEM, id("mineball"), new MineballItem(new Item.Properties().stacksTo(16).tab(TAB)));

	public static ItemStack getTabIcon() {
		return MINEBALL_ITEM.getDefaultInstance();
	}

	@Override
	public void onInitialize(ModContainer mod) {
		SuperStrike.init();
		Kicking.init();
	}
}
