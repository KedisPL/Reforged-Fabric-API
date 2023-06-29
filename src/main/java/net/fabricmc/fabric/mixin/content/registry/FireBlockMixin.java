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

package net.fabricmc.fabric.mixin.content.registry;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.impl.content.registry.FireBlockHooks;
import net.fabricmc.fabric.impl.content.registry.FlammableBlockRegistryImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@Mixin(FireBlock.class)
public class FireBlockMixin implements FireBlockHooks {
	private FlammableBlockRegistryImpl fabric_registry;

	@Shadow
	private int getBurnOdds(BlockState block_1) {
		return 0;
	}

	@Shadow
	private int getIgniteOdds(BlockState block_1) {
		return 0;
	}

	@Inject(at = @At("RETURN"), method = "<init>")
	private void afterConstruct(BlockBehaviour.Properties settings, CallbackInfo info) {
		fabric_registry = FlammableBlockRegistryImpl.getInstance((Block) (Object) this);
	}

	@Inject(at = @At("HEAD"), method = "getIgniteOdds", cancellable = true)
	private void getFabricBurnChance(BlockState block, CallbackInfoReturnable info) {
		FlammableBlockRegistry.Entry entry = fabric_registry.getFabric(block.getBlock());

		if (entry != null) {
			// TODO: use a (BlockState -> int) with this as the default impl
			if (block.hasProperty(BlockStateProperties.WATERLOGGED) && block.getValue(BlockStateProperties.WATERLOGGED)) {
				info.setReturnValue(0);
			} else {
				info.setReturnValue(entry.getBurnChance());
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "getBurnOdds", cancellable = true)
	private void getFabricSpreadChance(BlockState block, CallbackInfoReturnable info) {
		FlammableBlockRegistry.Entry entry = fabric_registry.getFabric(block.getBlock());

		if (entry != null) {
			// TODO: use a (BlockState -> int) with this as the default impl
			if (block.hasProperty(BlockStateProperties.WATERLOGGED) && block.getValue(BlockStateProperties.WATERLOGGED)) {
				info.setReturnValue(0);
			} else {
				info.setReturnValue(entry.getSpreadChance());
			}
		}
	}

	@Override
	public FlammableBlockRegistry.Entry fabric_getVanillaEntry(BlockState block) {
		return new FlammableBlockRegistry.Entry(getIgniteOdds(block), getBurnOdds(block));
	}
}
