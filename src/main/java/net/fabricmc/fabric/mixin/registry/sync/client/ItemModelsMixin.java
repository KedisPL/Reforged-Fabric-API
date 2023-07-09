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

package net.fabricmc.fabric.mixin.registry.sync.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.fabric.impl.registry.sync.trackers.Int2ObjectMapTracker;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

@Mixin(ItemModelShaper.class)
public class ItemModelsMixin {
	@Final
	@Shadow
	public Int2ObjectMap<ModelResourceLocation> shapes;
	@Final
	@Shadow
	private Int2ObjectMap<BakedModel> shapesCache;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(ModelManager bakedModelManager, CallbackInfo info) {
		Int2ObjectMapTracker.register(BuiltInRegistries.ITEM, "ItemModels.modelIds", shapes);
		Int2ObjectMapTracker.register(BuiltInRegistries.ITEM, "ItemModels.models", shapesCache);
	}
}
