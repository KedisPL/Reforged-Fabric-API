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

package net.fabricmc.fabric.impl.tag.convention;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagRegistration<T> {
    public static final TagRegistration<Item> ITEM_TAG_REGISTRATION = new TagRegistration<>(Registries.ITEM);
    public static final TagRegistration<Block> BLOCK_TAG_REGISTRATION = new TagRegistration<>(Registries.BLOCK);
    public static final TagRegistration<Biome> BIOME_TAG_REGISTRATION = new TagRegistration<>(Registries.BIOME);
    public static final TagRegistration<Fluid> FLUID_TAG_REGISTRATION = new TagRegistration<>(Registries.FLUID);
    public static final TagRegistration<EntityType<?>> ENTITY_TYPE_TAG_REGISTRATION = new TagRegistration<>(Registries.ENTITY_TYPE);
    public static final TagRegistration<Enchantment> ENCHANTMENT_TAG_REGISTRATION = new TagRegistration<>(Registries.ENCHANTMENT);
    private final ResourceKey<Registry<T>> registryKey;

    private TagRegistration(ResourceKey<Registry<T>> registry) {
        registryKey = registry;
    }

    public TagKey<T> registerFabric(String tagId) {
        return TagKey.create(registryKey, new ResourceLocation("fabric", tagId));
    }

    public TagKey<T> registerCommon(String tagId) {
        return TagKey.create(registryKey, new ResourceLocation("c", tagId));
    }
}
