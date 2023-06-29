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

package net.fabricmc.fabric.mixin.client.rendering.fluid;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRendererHookContainer;

@Mixin(LiquidBlockRenderer.class)
public class FluidRendererMixin {
	@Final
	@Shadow
	private TextureAtlasSprite[] lavaIcons;
	@Final
	@Shadow
	private TextureAtlasSprite[] waterIcons;
	@Shadow
	private TextureAtlasSprite waterOverlay;

	private final ThreadLocal<FluidRendererHookContainer> fabric_renderHandler = ThreadLocal.withInitial(FluidRendererHookContainer::new);
	private final ThreadLocal<Boolean> fabric_customRendering = ThreadLocal.withInitial(() -> false);
	private final ThreadLocal<Block> fabric_neighborBlock = new ThreadLocal<>();

	@Inject(at = @At("RETURN"), method = "setupSprites")
	public void onResourceReloadReturn(CallbackInfo info) {
		LiquidBlockRenderer self = (LiquidBlockRenderer) (Object) this;
		((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).onFluidRendererReload(self, waterIcons, lavaIcons, waterOverlay);
	}

	@Inject(at = @At("HEAD"), method = "tesselate", cancellable = true)
	public void tesselate(BlockAndTintGetter view, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
		if (!fabric_customRendering.get()) {
			// Prevent recursively looking up custom fluid renderers when default behavior is being invoked
			try {
				fabric_customRendering.set(true);
				//tessellateViaHandler(view, pos, vertexConsumer, blockState, fluidState, info);
			} finally {
				fabric_customRendering.set(false);
			}
		}

		if (info.isCancelled()) {
			return;
		}

		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		ctr.getSprites(view, pos, fluidState);
	}

	@Unique
	private void tessellateViaHandler(BlockAndTintGetter view, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		FluidRenderHandler handler = ((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).getOverride(fluidState.getType());

		ctr.view = view;
		ctr.pos = pos;
		ctr.blockState = blockState;
		ctr.fluidState = fluidState;
		ctr.handler = handler;

		if (handler != null) {
			handler.renderFluid(pos, view, vertexConsumer, blockState, fluidState);
			info.cancel();
		}
	}

	@Inject(at = @At("RETURN"), method = "tesselate")
	public void tesselateReturn(BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
		fabric_renderHandler.get().clear();
	}

	@ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;isNeighborSameFluid(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/material/FluidState;)Z"), method = "tesselate", ordinal = 0)
	public boolean modLavaCheck(boolean chk) {
		// First boolean local is set by vanilla according to 'matches lava'
		// but uses the negation consistent with 'matches water'
		// for determining if special water sprite should be used behind glass.

		// Has other uses but those are overridden by this mixin and have
		// already happened by the time this hook is called

		// If this fluid has an overlay texture, set this boolean too false
		final FluidRendererHookContainer ctr = fabric_renderHandler.get();
		return !ctr.hasOverlay;
	}

	@ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;isNeighborSameFluid(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/material/FluidState;)Z"), method = "tesselate", ordinal = 0)
	public TextureAtlasSprite[] modSpriteArray(TextureAtlasSprite[] chk) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		return ctr.handler != null ? ctr.sprites : chk;
	}

	// Redirect redirects all 'waterOverlaySprite' gets in 'render' to this method, this is correct
	@Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;waterOverlay:Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"), method = "tesselate")
	public TextureAtlasSprite modWaterOverlaySprite(LiquidBlockRenderer self) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		return ctr.handler != null && ctr.hasOverlay ? ctr.overlay : waterOverlay;
	}

	@ModifyVariable(at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 0, shift = At.Shift.BEFORE), method = "tesselate", ordinal = 0)
	public int modTintColor(int chk) {
		FluidRendererHookContainer ctr = fabric_renderHandler.get();
		return ctr.handler != null ? ctr.handler.getFluidColor(ctr.view, ctr.pos, ctr.fluidState) : chk;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"), method = "tesselate")
	public Block getOverlayBlock(BlockState state) {
		Block block = state.getBlock();
		fabric_neighborBlock.set(block);

		// An if-statement follows, we don't want this anymore and 'null' makes
		// its condition always false (due to instanceof)
		return null;
	}

	@ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", shift = At.Shift.BY, by = 2), method = "tesselate", ordinal = 0)
	public TextureAtlasSprite modSideSpriteForOverlay(TextureAtlasSprite chk) {
		Block block = fabric_neighborBlock.get();

		if (FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(block)) {
			FluidRendererHookContainer ctr = fabric_renderHandler.get();
			return ctr.handler != null && ctr.hasOverlay ? ctr.overlay : waterOverlay;
		}

		return chk;
	}
}
