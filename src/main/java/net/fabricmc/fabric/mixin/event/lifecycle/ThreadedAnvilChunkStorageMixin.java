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

package net.fabricmc.fabric.mixin.event.lifecycle;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public abstract class ThreadedAnvilChunkStorageMixin {
	@Shadow
	@Final
	private ServerLevel level;

	// Chunk (Un)Load events, An explanation:
	// Must of this code is wrapped inside of futures and consumers, so it's generally a mess.

	/**
	 * Injection is inside of tryUnloadChunk.
	 * We inject just after "setLoadedToWorld" is made false, since here the WorldChunk is guaranteed to be unloaded.
	 */
	@Inject(method = "scheduleUnload", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setLoaded(Z)V", shift = At.Shift.AFTER))
	private void onChunkUnload(long pos, ChunkHolder chunkHolder, CompletableFuture<ChunkAccess> chunkFuture, ChunkAccess chunk, CallbackInfo ci) {
		ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.level, (LevelChunk) chunk);
	}

	/**
	 * Injection is inside of convertToFullChunk?
	 *
	 * <p>The following is expected contractually
	 *
	 * <ul><li>the chunk being loaded MUST be a WorldChunk.
	 * <li>everything within the chunk has been loaded into the world. Entities, BlockEntities, etc.</ul>
	 */
	@Inject(method = "protoChunkToFullChunk", at = @At("TAIL"))
	private void onChunkLoad(ChunkHolder chunkHolder, CallbackInfoReturnable<ChunkAccess> callbackInfoReturnable) {
		// We fire the event at TAIL since the chunk is guaranteed to be a WorldChunk then.
		ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(this.level, (LevelChunk) callbackInfoReturnable.getReturnValue());
	}
}
