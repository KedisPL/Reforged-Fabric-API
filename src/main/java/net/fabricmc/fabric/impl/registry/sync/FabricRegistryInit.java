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

package net.fabricmc.fabric.impl.registry.sync;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;

public class FabricRegistryInit implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				RegistrySyncManager.sendPacket(server, handler.player));

		// Synced in PlaySoundS2CPacket.
		RegistryAttributeHolder.get(BuiltInRegistries.SOUND_EVENT)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced with RegistryTagContainer from RegistryTagManager.
		RegistryAttributeHolder.get(BuiltInRegistries.FLUID)
				.addAttribute(RegistryAttribute.SYNCED);

		// StatusEffectInstance serialises with raw id.
		RegistryAttributeHolder.get(BuiltInRegistries.MOB_EFFECT)
				.addAttribute(RegistryAttribute.SYNCED)
				.addAttribute(RegistryAttribute.PERSISTED);

		// Synced in ChunkDeltaUpdateS2CPacket among other places, a pallet is used when saving.
		RegistryAttributeHolder.get(BuiltInRegistries.BLOCK)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced in EnchantmentScreenHandler
		RegistryAttributeHolder.get(BuiltInRegistries.ENCHANTMENT)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced in EntitySpawnS2CPacket and RegistryTagManager
		RegistryAttributeHolder.get(BuiltInRegistries.ENTITY_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced in RegistryTagManager
		RegistryAttributeHolder.get(BuiltInRegistries.ITEM)
				.addAttribute(RegistryAttribute.SYNCED);

		// Saved and synced using string ID.
		RegistryAttributeHolder.get(BuiltInRegistries.POTION);

		// Doesnt seem to be accessed apart from registering?
		RegistryAttributeHolder.get(BuiltInRegistries.CARVER);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.FEATURE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.FOLIAGE_PLACER_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.TRUNK_PLACER_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.TREE_DECORATOR_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.FEATURE_SIZE_TYPE);

		// Synced in ParticleS2CPacket
		RegistryAttributeHolder.get(BuiltInRegistries.PARTICLE_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.BIOME_SOURCE);

		// Synced. Vanilla uses raw ids in BlockEntityUpdateS2CPacket, and mods use the Vanilla syncing since 1.18
		RegistryAttributeHolder.get(BuiltInRegistries.BLOCK_ENTITY_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced in PaintingSpawnS2CPacket
		RegistryAttributeHolder.get(BuiltInRegistries.PAINTING_VARIANT)
				.addAttribute(RegistryAttribute.SYNCED);

		// Doesnt seem to be synced or saved, STAT_TYPE seems to handle the syncing.
		RegistryAttributeHolder.get(BuiltInRegistries.CUSTOM_STAT);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.CHUNK_STATUS);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_PIECE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.RULE_TEST);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.POS_RULE_TEST);

		RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_PROCESSOR);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_POOL_ELEMENT);

		// Uses a data tracker (and thus, raw IDs) to sync cat entities to the client
		RegistryAttributeHolder.get(BuiltInRegistries.CAT_VARIANT)
				.addAttribute(RegistryAttribute.SYNCED);

		// Uses a data tracker (and thus, raw IDs) to sync frog entities to the client
		RegistryAttributeHolder.get(BuiltInRegistries.FROG_VARIANT)
				.addAttribute(RegistryAttribute.SYNCED);

		// Uses a data tracker (and thus, raw IDs) to sync painting entities to the client
		RegistryAttributeHolder.get(BuiltInRegistries.PAINTING_VARIANT)
				.addAttribute(RegistryAttribute.SYNCED);

		//  Uses the raw ID when syncing the command tree to the client
		RegistryAttributeHolder.get(BuiltInRegistries.COMMAND_ARGUMENT_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced in OpenScreenS2CPacket
		RegistryAttributeHolder.get(BuiltInRegistries.MENU)
				.addAttribute(RegistryAttribute.SYNCED);

		// Does not seem to be serialised, only queried by id. Not synced
		RegistryAttributeHolder.get(BuiltInRegistries.RECIPE_TYPE);

		// Synced by id
		RegistryAttributeHolder.get(BuiltInRegistries.RECIPE_SERIALIZER);

		// Synced and saved by id
		RegistryAttributeHolder.get(BuiltInRegistries.ATTRIBUTE);

		// Synced in StatisticsS2CPacket
		RegistryAttributeHolder.get(BuiltInRegistries.STAT_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced by rawID in TrackedDataHandlerRegistry.VILLAGER_DATA
		RegistryAttributeHolder.get(BuiltInRegistries.VILLAGER_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced by rawID in TrackedDataHandlerRegistry.VILLAGER_DATA
		RegistryAttributeHolder.get(BuiltInRegistries.VILLAGER_PROFESSION)
				.addAttribute(RegistryAttribute.SYNCED);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.POINT_OF_INTEREST_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.MEMORY_MODULE_TYPE);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.SENSOR_TYPE);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.SCHEDULE);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.ACTIVITY);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.LOOT_FUNCTION_TYPE);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.LOOT_CONDITION_TYPE);

		// Synced in TagManager::toPacket/fromPacket -> TagGroup::serialize/deserialize
		RegistryAttributeHolder.get(BuiltInRegistries.GAME_EVENT)
				.addAttribute(RegistryAttribute.SYNCED);
	}
}
