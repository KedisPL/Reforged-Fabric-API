/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.entity.event;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@Shadow
	public abstract boolean isDeadOrDying();

	@Shadow
	public abstract Optional<BlockPos> getSleepingPos();

	@Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;killedEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void onEntityKilledOther(DamageSource source, CallbackInfo ci, Entity attacker) {
		// FIXME: Cannot use shadowed fields from supermixins - needs a fix so people can use fabric api in a dev environment even though this is fine in this repo and prod.
		//  A temporary fix is to just cast the mixin to LivingEntity and access the world field with a few ugly casts.
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.invoker().afterKilledOtherEntity((ServerLevel) ((LivingEntity) (Object) this).level(), attacker, (LivingEntity) (Object) this);
	}

	@Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
	private void notifyDeath(DamageSource source, CallbackInfo ci) {
		ServerLivingEntityEvents.AFTER_DEATH.invoker().afterDeath((LivingEntity) (Object) this, source);
	}

	@Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z", ordinal = 1))
	boolean beforeEntityKilled(LivingEntity livingEntity, DamageSource source, float amount) {
		return isDeadOrDying() && ServerLivingEntityEvents.ALLOW_DEATH.invoker().allowDeath(livingEntity, source, amount);
	}

	@Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), cancellable = true)
	private void beforeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage((LivingEntity) (Object) this, source, amount)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "startSleeping", at = @At("RETURN"))
	private void onSleep(BlockPos pos, CallbackInfo info) {
		EntitySleepEvents.START_SLEEPING.invoker().onStartSleeping((LivingEntity) (Object) this, pos);
	}

	@Inject(method = "stopSleeping", at = @At("HEAD"))
	private void onWakeUp(CallbackInfo info) {
		BlockPos sleepingPos = getSleepingPos().orElse(null);

		// If actually asleep - this method is often called with data loading, syncing etc. "just to be sure"
		if (sleepingPos != null) {
			EntitySleepEvents.STOP_SLEEPING.invoker().onStopSleeping((LivingEntity) (Object) this, sleepingPos);
		}
	}

	@Dynamic("m_289088_: Synthetic lambda body for Optional.map in checkBedExists")
	@Inject(method = "m_289088_", at = @At("RETURN"), cancellable = true)
	private void onIsSleepingInBed(BlockPos sleepingPos, CallbackInfoReturnable<Boolean> info) {
		BlockState bedState = ((LivingEntity) (Object) this).level().getBlockState(sleepingPos);
		InteractionResult result = EntitySleepEvents.ALLOW_BED.invoker().allowBed((LivingEntity) (Object) this, sleepingPos, bedState, info.getReturnValueZ());

		if (result != InteractionResult.PASS) {
			info.setReturnValue(result.consumesAction());
		}
	}

	@Inject(method = "getBedOrientation", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void onGetSleepingDirection(CallbackInfoReturnable<Direction> info, @Nullable BlockPos sleepingPos) {
		if (sleepingPos != null) {
			info.setReturnValue(EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.invoker().modifySleepDirection((LivingEntity) (Object) this, sleepingPos, info.getReturnValue()));
		}
	}

	// This is needed 1) so that the vanilla logic in wakeUp runs for modded beds and 2) for the injector below.
	// The injector is shared because method_18404 and sleep share much of the structure here.
	@Dynamic("method_18404: Synthetic lambda body for Optional.ifPresent in wakeUp")
	@ModifyVariable(method = {"method_18404", "sleep"}, at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
	private BlockState modifyBedForOccupiedState(BlockState state, BlockPos sleepingPos) {
		InteractionResult result = EntitySleepEvents.ALLOW_BED.invoker().allowBed((LivingEntity) (Object) this, sleepingPos, state, state.getBlock() instanceof BedBlock);

		// If a valid bed, replace with vanilla red bed so that the vanilla instanceof check succeeds.
		return result.consumesAction() ? Blocks.RED_BED.defaultBlockState() : state;
	}

	// The injector is shared because method_18404 and sleep share much of the structure here.
	@Dynamic("method_18404: Synthetic lambda body for Optional.ifPresent in wakeUp")
	@Redirect(method = {"method_18404", "sleep"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean setOccupiedState(Level world, BlockPos pos, BlockState state, int flags) {
		// This might have been replaced by a red bed above, so we get it again.
		// Note that we *need* to replace it so the state.with(OCCUPIED, ...) call doesn't crash
		// when the bed doesn't have the property.
		BlockState originalState = world.getBlockState(pos);
		boolean occupied = state.getValue(BedBlock.OCCUPIED);

		if (EntitySleepEvents.SET_BED_OCCUPATION_STATE.invoker().setBedOccupationState((LivingEntity) (Object) this, pos, originalState, occupied)) {
			return true;
		} else if (originalState.hasProperty(BedBlock.OCCUPIED)) {
			// This check is widened from (instanceof BedBlock) to a property check to allow modded blocks
			// that don't use the event.
			return world.setBlock(pos, originalState.setValue(BedBlock.OCCUPIED, occupied), flags);
		} else {
			return false;
		}
	}

	@Dynamic("method_18404: Synthetic lambda body for Optional.ifPresent in wakeUp")
	@Redirect(method = "method_18404", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BedBlock;findWakeUpPosition(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/CollisionView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;F)Ljava/util/Optional;"))
	private Optional<Vec3> modifyWakeUpPosition(EntityType<?> type, CollisionGetter world, BlockPos pos, Direction direction, float yaw) {
		Optional<Vec3> original = Optional.empty();
		BlockState bedState = world.getBlockState(pos);

		if (bedState.getBlock() instanceof BedBlock) {
			original = BedBlock.findStandUpPosition(type, world, pos, direction, yaw);
		}

		Vec3 newPos = EntitySleepEvents.MODIFY_WAKE_UP_POSITION.invoker().modifyWakeUpPosition((LivingEntity) (Object) this, pos, bedState, original.orElse(null));
		return Optional.ofNullable(newPos);
	}
}
