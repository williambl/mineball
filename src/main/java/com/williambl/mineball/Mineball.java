package com.williambl.mineball;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class Mineball extends Entity implements ItemSupplier {
	public Mineball(EntityType<? extends Mineball> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();

		// movement + bounce
		var movementBefore = this.getDeltaMovement();
		this.move(MoverType.SELF, this.getDeltaMovement());

		var bounce = this.getDeltaMovement().subtract(movementBefore).scale(this.getInertia());
		var movementPlusBounce = this.getDeltaMovement().add(bounce);
		if (movementPlusBounce.lengthSqr() <= movementBefore.lengthSqr()) { // conservation of energy
			this.setDeltaMovement(movementPlusBounce);
		}

		// inertia + gravity
		this.setDeltaMovement(this.getDeltaMovement().scale(this.getInertia()));
		this.setDeltaMovement(this.getDeltaMovement().add(0.0, this.getGravity(), 0.0));

		// pushing around
		if (!this.level.isClientSide()) {
			List<Entity> list = this.level.getEntities(this, this.getBoundingBox().inflate(0.001), EntitySelector.pushableBy(this));
			if (!list.isEmpty()) {
				for (Entity entity : list) {
					this.push(entity);
				}
			}
		}
	}

	private double getInertia() {
		return this.horizontalCollision ? 0.65 : 0.95;
	}

	private double getGravity() {
		return -0.04;
	}

	@Override
	public void push(Entity entity) {
		super.push(entity);
		if (!this.isPassengerOfSameVehicle(entity)) {
		var otherMovement = entity.position().subtract(entity.xo, entity.yo, entity.zo);
			var movement = this.getDeltaMovement();
			double xVel = movement.x();
			double yVel = movement.y();
			double zVel = movement.z();
			if (Math.abs(movement.x()) < 0.01 || Math.signum(movement.x()) == Math.signum(otherMovement.x())) {
				xVel = Math.abs(otherMovement.x()) > Math.abs(movement.x()) ? otherMovement.x() : movement.x();
			}
			if (Math.abs(movement.y()) < 0.01 || Math.signum(movement.y()) == Math.signum(otherMovement.y())) {
				yVel = Math.abs(otherMovement.y()) > Math.abs(movement.y()) ? otherMovement.y() : movement.y();
			}
			if (Math.abs(movement.z()) < 0.01 || Math.signum(movement.z()) == Math.signum(otherMovement.z())) {
				zVel = Math.abs(otherMovement.z()) > Math.abs(movement.z()) ? otherMovement.z() : movement.z();
			}
			this.setDeltaMovement(new Vec3(xVel, yVel, zVel));
		}
	}

	@Override
	protected void defineSynchedData() {

	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {

	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {

	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}

	@Override
	public ItemStack getItem() {
		return Items.SNOWBALL.getDefaultInstance();
	}
}
