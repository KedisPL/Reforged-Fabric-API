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

import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
abstract class PlayerEntityMixin {
	@Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
	private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> info) {
		Player.BedSleepingProblem failureReason = EntitySleepEvents.ALLOW_SLEEPING.invoker().allowSleep((Player) (Object) this, pos);

		if (failureReason != null) {
			info.setReturnValue(Either.left(failureReason));
		}
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
	private boolean redirectDaySleepCheck(Level world) {
		boolean day = world.isDay();

		if (((LivingEntity) (Object) this).getSleepingPos().isPresent()) {
			BlockPos pos = ((LivingEntity) (Object) this).getSleepingPos().get();
			InteractionResult result = EntitySleepEvents.ALLOW_SLEEP_TIME.invoker().allowSleepTime((Player) (Object) this, pos, !day);

			if (result != InteractionResult.PASS) {
				return !result.consumesAction(); // true from the event = night-like conditions, so we have to invert
			}
		}

		return day;
	}

	@Inject(method = "isSleepingLongEnough", at = @At("RETURN"), cancellable = true)
	private void onIsSleepingLongEnough(CallbackInfoReturnable<Boolean> info) {
		if (info.getReturnValueZ()) {
			info.setReturnValue(EntitySleepEvents.ALLOW_RESETTING_TIME.invoker().allowResettingTime((Player) (Object) this));
		}
	}
}
