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

package net.fabricmc.fabric.impl.object.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public final class TradeOfferInternals {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-object-builder-api-v1");

	private TradeOfferInternals() {
	}

	// synchronized guards against concurrent modifications - Vanilla does not mutate the underlying arrays (as of 1.16),
	// so reads will be fine without locking.
	public static synchronized void registerVillagerOffers(VillagerProfession profession, int level, Consumer<List<VillagerTrades.ItemListing>> factory) {
		Objects.requireNonNull(profession, "VillagerProfession may not be null.");
		registerOffers(VillagerTrades.TRADES.computeIfAbsent(profession, key -> new Int2ObjectOpenHashMap<>()), level, factory);
	}

	public static synchronized void registerWanderingTraderOffers(int level, Consumer<List<VillagerTrades.ItemListing>> factory) {
		registerOffers(VillagerTrades.WANDERING_TRADER_TRADES, level, factory);
	}

	// Shared code to register offers for both villagers and wandering traders.
	private static void registerOffers(Int2ObjectMap<VillagerTrades.ItemListing[]> leveledTradeMap, int level, Consumer<List<VillagerTrades.ItemListing>> factory) {
		final List<VillagerTrades.ItemListing> list = new ArrayList<>();
		factory.accept(list);

		final VillagerTrades.ItemListing[] originalEntries = leveledTradeMap.computeIfAbsent(level, key -> new VillagerTrades.ItemListing[0]);
		final VillagerTrades.ItemListing[] addedEntries = list.toArray(new VillagerTrades.ItemListing[0]);

		final VillagerTrades.ItemListing[] allEntries = ArrayUtils.addAll(originalEntries, addedEntries);
		leveledTradeMap.put(level, allEntries);
	}

	public static void printRefreshOffersWarning() {
		Throwable loggingThrowable = new Throwable();
		LOGGER.warn("TradeOfferHelper#refreshOffers does not do anything, yet it was called! Stack trace:", loggingThrowable);
	}
}
