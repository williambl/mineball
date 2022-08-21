package com.williambl.mineball;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class Mineball extends Entity implements ItemSupplier {

	private int fireTicks = 0;

	public final List<PossessionData> possessions = new ArrayList<>();

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
			List<Entity> collidingEntities = this.level.getEntities(this, this.getBoundingBox().inflate(0.001), EntitySelector.pushableBy(this));
			if (!collidingEntities.isEmpty()) {
				for (Entity entity : collidingEntities) {
					this.push(entity);
				}
			}

			{
				var iterator = this.possessions.iterator();
				int count = 0;
				while (iterator.hasNext()) {
					var possession = iterator.next();
					count++;
					if (collidingEntities.contains(possession.entity)) {
						possession.incrementTicksSoFar();
						possession.resetTimeout();
					} else {
						possession.decrementTicksBeforeTimeout();
						if (possession.ticksBeforeTimeout() <= 0 || count > 3) {
							iterator.remove();
						}
					}
				}
			}

			for (Entity entity : collidingEntities) {
				if (this.possessions.stream().noneMatch(p -> p.entity() == entity)) {
					this.possessions.add(new PossessionData(entity));
				}
			}
		}

		if (this.level.isClientSide()) {
			if (this.fireTicks > 0) {
				this.fireTicks--;
				for (int i = 0; i < 3; i++) {
					this.level.addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
				}
			}

			if (this.isInWater()) {
				for(int o = 0; o < 4; ++o) {
					this.level.addParticle(ParticleTypes.BUBBLE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
				}
			}
		}
	}

	private double getInertia() {
		return this.isOnGround() ? Math.min(this.getBlockStateOn().getBlock().getFriction() + 0.35, 1.0) : this.isInWater() || this.isInWater() ? 0.6 : 0.95;
	}

	private double getGravity() {
		return this.isInWaterOrBubble() ? 0.1 : -0.04;
	}

	private double getPossessionKickFactor(Entity entity) {
		for (var possession : this.possessions) {
			if (possession.entity() == entity) {
				return Math.max(Math.min(2.0 * (possession.ticksSoFar() / 20.0), 2.0), 1.0);
			}
		}

		return 1.0;
	}

	public void kick(Entity entity, Vec3 direction, double upwardsFactor, double factor) {
		this.setDeltaMovement(this.getDeltaMovement().add(direction.add(0.0, upwardsFactor, 0.0).scale(factor * this.getPossessionKickFactor(entity))));
		this.playSound(MineballMod.KICK_SOUND);
	}


	public void kick(Entity entity, double upwardsFactor, double factor) {
		this.kick(entity, entity.getForward(), upwardsFactor, factor);
	}

	public void kick(Entity entity, double factor) {
		this.kick(entity, 0.9, factor);
	}

	public void setSuperStriking() {
		this.level.broadcastEntityEvent(this, (byte) 63);
		this.playSound(MineballMod.SUPER_STRIKE_SOUND);
	}

	@Override
	public boolean isOnFire() {
		return super.isOnFire() || (this.level.isClientSide() && this.fireTicks > 0);
	}

	@Override
	public void handleEntityEvent(byte b) {
		switch (b) {
			case 63 -> this.fireTicks = 20;
			default -> super.handleEntityEvent(b);
		}
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize() * 10;
		if (Double.isNaN(e)) {
			e = 1.0;
		}

		e *= 64.0 * getViewScale();
		return d < e * e;
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
		return MineballMod.MINEBALL_ITEM.getDefaultInstance();
	}

	static final class PossessionData {
		private final Entity entity;
		private int ticksSoFar;
		private int ticksBeforeTimeout;

		private static final int MAX_TIMEOUT = 60;

		PossessionData(Entity entity) {
			this.entity = entity;
			this.ticksSoFar = 0;
			this.ticksBeforeTimeout = MAX_TIMEOUT;
		}

		public Entity entity() {
			return this.entity;
		}

		public int ticksSoFar() {
			return this.ticksSoFar;
		}

		public int ticksBeforeTimeout() {
			return this.ticksBeforeTimeout;
		}

		public void incrementTicksSoFar() {
			this.ticksSoFar = this.ticksSoFar + 1;
		}

		public void decrementTicksBeforeTimeout() {
			this.ticksBeforeTimeout = this.ticksBeforeTimeout - 1;
		}

		public void resetTimeout() {
			this.ticksBeforeTimeout = MAX_TIMEOUT;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || obj.getClass() != this.getClass()) return false;
			var that = (PossessionData) obj;
			return Objects.equals(this.entity, that.entity) &&
					this.ticksSoFar == that.ticksSoFar &&
					this.ticksBeforeTimeout == that.ticksBeforeTimeout;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.entity, this.ticksSoFar, this.ticksBeforeTimeout);
		}

		@Override
		public String toString() {
			return "PossessionData[" +
					"entity=" + this.entity + ", " +
					"ticksSoFar=" + this.ticksSoFar + ", " +
					"ticksBeforeTimeout=" + this.ticksBeforeTimeout + ']';
		}
	}
}
