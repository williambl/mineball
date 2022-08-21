package com.williambl.mineball.mixin;

import com.williambl.mineball.Mineball;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "jumpFromGround", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void kickBallWithJump(CallbackInfo ci, double d) {
		if (!this.level.isClientSide()) {
			for (var ball : this.level.getEntities(EntityTypeTest.forClass(Mineball.class), this.getBoundingBox().inflate(0.1), EntitySelector.pushableBy(this))) {
				ball.setDeltaMovement(ball.getDeltaMovement().add(this.getForward().add(0.0, 0.9, 0.0).scale(d * 2.0)));
			}
		}
	}
}
