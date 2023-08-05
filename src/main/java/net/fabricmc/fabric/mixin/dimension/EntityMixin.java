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

package net.fabricmc.fabric.mixin.dimension;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.impl.dimension.Teleportable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;

/**
 * This mixin implements {@link Entity#findDimensionEntryPoint(ServerLevel)} for modded dimensions, as Vanilla will
 * not return a teleport target for anything but Vanilla dimensions and prevents changing teleport target in
 * {@link ServerPlayer#findDimensionEntryPoint(ServerLevel)} when teleporting to END using api.
 * This also prevents several End dimension-specific code when teleporting using api.
 */
@Mixin(value = {ServerPlayer.class, Entity.class})
public class EntityMixin implements Teleportable {
    @Unique
    @Nullable
    protected PortalInfo customTeleportTarget;

    @Override
    public void fabric_setCustomTeleportTarget(PortalInfo teleportTarget) {
        this.customTeleportTarget = teleportTarget;
    }

    @Inject(method = "findDimensionEntryPoint", at = @At("HEAD"), cancellable = true, allow = 1)
    public void getTeleportTarget(ServerLevel destination, CallbackInfoReturnable<PortalInfo> cir) {
        // Check if a destination has been set for the entity currently being teleported
        PortalInfo customTarget = this.customTeleportTarget;

        if (customTarget != null) {
            cir.setReturnValue(customTarget);
        }
    }

    /**
     * This stops the following behaviors, in 1 mixin.
     * - ServerWorld#createEndSpawnPlatform in Entity
     * - End-to-overworld spawning behavior in ServerPlayerEntity
     * - ServerPlayerEntity#createEndSpawnPlatform in ServerPlayerEntity
     */
    @Redirect(method = "changeDimension", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"))
    private ResourceKey<Level> stopEndSpecificBehavior() {
        if (this.customTeleportTarget != null) return null;
        return Level.END;
    }
}
