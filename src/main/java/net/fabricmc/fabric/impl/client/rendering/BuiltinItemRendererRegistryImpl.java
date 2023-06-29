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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public final class BuiltinItemRendererRegistryImpl implements BuiltinItemRendererRegistry {
	private static final Map<Item, DynamicItemRenderer> RENDERERS = new HashMap<>();

	public BuiltinItemRendererRegistryImpl() {
	}

	@Override
	public void register(Item item, BuiltinItemRenderer renderer) {
		Objects.requireNonNull(renderer, "renderer is null");
		this.register(item, (stack, mode, matrices, vertexConsumers, light, overlay) -> renderer.render(stack, matrices, vertexConsumers, light, overlay));
	}

	@Override
	public void register(ItemLike item, BuiltinItemRenderer renderer) {
		Objects.requireNonNull(item, "item is null");
		register(item.asItem(), renderer);
	}

	@Override
	public void register(ItemLike item, DynamicItemRenderer renderer) {
		Objects.requireNonNull(item, "item is null");
		Objects.requireNonNull(item.asItem(), "item is null");
		Objects.requireNonNull(renderer, "renderer is null");

		if (RENDERERS.putIfAbsent(item.asItem(), renderer) != null) {
			throw new IllegalArgumentException("Item " + BuiltInRegistries.ITEM.getKey(item.asItem()) + " already has a builtin renderer!");
		}
	}

	@Override
	@Nullable
	public DynamicItemRenderer get(ItemLike item) {
		Objects.requireNonNull(item.asItem(), "item is null");

		return RENDERERS.get(item.asItem());
	}
}
