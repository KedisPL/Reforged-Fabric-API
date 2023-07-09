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

package net.fabricmc.fabric.mixin.entity.event.elytra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
@Mixin(Player.class)
abstract class PlayerEntityMixin extends LivingEntity {
	PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
		throw new AssertionError();
	}

	@Shadow
	public abstract void startFallFlying();

	/**
	 * Allow the server-side and client-side elytra checks to fail when {@link EntityElytraEvents#ALLOW} blocks flight,
	 * and otherwise to succeed for elytra flight through {@link EntityElytraEvents#CUSTOM}.
	 */
	@SuppressWarnings("ConstantConditions")
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/EquipmentSlot;CHEST:Lnet/minecraft/world/entity/EquipmentSlot;"), method = "tryToStartFallFlying()Z", allow = 1, cancellable = true)
	void injectElytraCheck(CallbackInfoReturnable<Boolean> cir) {
		Player self = (Player) (Object) this;

		if (!EntityElytraEvents.ALLOW.invoker().allowElytraFlight(self)) {
			cir.setReturnValue(false);
			return; // Return to prevent the rest of this injector from running.
		}

		if (EntityElytraEvents.CUSTOM.invoker().useCustomElytra(self, false)) {
			startFallFlying();
			cir.setReturnValue(true);
		}
	}
}
