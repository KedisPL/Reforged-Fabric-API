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

package net.fabricmc.fabric.mixin.client.model.loading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Tuple;
import net.minecraft.util.profiling.ProfilerFiller;

@Mixin(ModelManager.class)
public class BakedModelManagerMixin implements FabricBakedModelManager {
    @Shadow
    private Map<ResourceLocation, BakedModel> bakedRegistry;

    @Override
    public BakedModel getModel(ResourceLocation id) {
        return bakedRegistry.get(id);
    }

    @Redirect(method = "reload", at = @At(value = "INVOKE", target = "java/util/concurrent/CompletableFuture.thenCombineAsync(Ljava/util/concurrent/CompletionStage;Ljava/util/function/BiFunction;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", remap = false), allow = 1)
    private CompletableFuture<ModelBakery> loadModelPluginData(
            CompletableFuture<Map<ResourceLocation, BlockModel>> self,
            CompletionStage<Map<ResourceLocation, List<ModelBakery.LoadedJson>>> otherFuture,
            BiFunction<Map<ResourceLocation, BlockModel>, Map<ResourceLocation, List<ModelBakery.LoadedJson>>, ModelBakery> modelLoaderConstructor,
            Executor executor,
            // reload args
            PreparableReloadListener.PreparationBarrier synchronizer,
            ResourceManager manager,
            ProfilerFiller prepareProfiler,
            ProfilerFiller applyProfiler,
            Executor prepareExecutor,
            Executor applyExecutor) {
        CompletableFuture<List<ModelLoadingPlugin>> pluginsFuture = ModelLoadingPluginManager.preparePlugins(manager, prepareExecutor);
        CompletableFuture<Tuple<Map<ResourceLocation, BlockModel>, Map<ResourceLocation, List<ModelBakery.LoadedJson>>>> pairFuture = self.thenCombine(otherFuture, Tuple::new);
        return pairFuture.thenCombineAsync(pluginsFuture, (pair, plugins) -> {
            ModelLoadingPluginManager.CURRENT_PLUGINS.set(plugins);
            return modelLoaderConstructor.apply(pair.getA(), pair.getB());
        }, executor);
    }
}
