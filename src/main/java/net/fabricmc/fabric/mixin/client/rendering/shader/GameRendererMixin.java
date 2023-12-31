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

package net.fabricmc.fabric.mixin.client.rendering.shader;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

/**
 * Implements custom core shader registration (CoreShaderRegistrationCallback).
 */
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
	@Inject(
			method = "reloadShaders",
			at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false, shift = At.Shift.AFTER),
			slice = @Slice(from = @At(value = "NEW", target = "net/minecraft/client/renderer/ShaderInstance", ordinal = 0)),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void registerShaders(ResourceProvider factory, CallbackInfo info, List<?> shaderStages, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> programs) throws IOException {
		CoreShaderRegistrationCallback.RegistrationContext context = (id, vertexFormat, loadCallback) -> {
			ShaderInstance program = new FabricShaderProgram(factory, id, vertexFormat);
			programs.add(Pair.of(program, loadCallback));
		};
		CoreShaderRegistrationCallback.EVENT.invoker().registerShaders(context);
	}
}
