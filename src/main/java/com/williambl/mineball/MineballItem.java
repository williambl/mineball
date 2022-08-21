package com.williambl.mineball;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class MineballItem extends Item {
	public MineballItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		if (!(level instanceof ServerLevel)) {
			return InteractionResult.SUCCESS;
		} else {
			ItemStack itemStack = useOnContext.getItemInHand();
			BlockPos blockPos = useOnContext.getClickedPos();
			Direction direction = useOnContext.getClickedFace();
			BlockState blockState = level.getBlockState(blockPos);
			BlockPos blockPos2;
			if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
				blockPos2 = blockPos;
			} else {
				blockPos2 = blockPos.relative(direction);
			}

			if (MineballMod.MINEBALL.spawn(
					(ServerLevel)level,
					itemStack,
					useOnContext.getPlayer(),
					blockPos2,
					MobSpawnType.SPAWN_EGG,
					true,
					!Objects.equals(blockPos, blockPos2) && direction == Direction.UP
			)
					!= null) {
				itemStack.shrink(1);
				level.gameEvent(useOnContext.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
			}

			return InteractionResult.CONSUME;
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		var hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		if (hitResult.getType() != HitResult.Type.BLOCK) {
			return InteractionResultHolder.pass(itemStack);
		} else if (!(level instanceof ServerLevel)) {
			return InteractionResultHolder.success(itemStack);
		} else {
			BlockPos blockPos = hitResult.getBlockPos();
			if (!(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
				return InteractionResultHolder.pass(itemStack);
			} else if (level.mayInteract(player, blockPos) && player.mayUseItemAt(blockPos, hitResult.getDirection(), itemStack)) {
				Entity entity = MineballMod.MINEBALL.spawn((ServerLevel)level, itemStack, player, blockPos, MobSpawnType.SPAWN_EGG, false, false);
				if (entity == null) {
					return InteractionResultHolder.pass(itemStack);
				} else {
					if (!player.getAbilities().instabuild) {
						itemStack.shrink(1);
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
					return InteractionResultHolder.consume(itemStack);
				}
			} else {
				return InteractionResultHolder.fail(itemStack);
			}
		}
	}
}
