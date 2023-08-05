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

package net.fabricmc.fabric.mixin.registry.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;

// Adds namespaces to tag directories for registries added by mods.
@Mixin(TagManager.class)
abstract class TagManagerLoaderMixin {
    @Inject(method = "getTagDir", at = @At("HEAD"), cancellable = true)
    private static void onGetPath(ResourceKey<? extends Registry<?>> registry, CallbackInfoReturnable<String> info) {
        // TODO: Expand this change to static registries in the future.
        if (!DynamicRegistriesImpl.DYNAMIC_REGISTRY_KEYS.contains(registry)) {
            return;
        }

        ResourceLocation id = registry.location();

        // Vanilla doesn't mark namespaces in the directories of tags at all,
        // so we prepend the directories with the namespace if it's a modded registry id.
        if (!id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            info.setReturnValue("tags/" + id.getNamespace() + "/" + id.getPath());
        }
    }
}
