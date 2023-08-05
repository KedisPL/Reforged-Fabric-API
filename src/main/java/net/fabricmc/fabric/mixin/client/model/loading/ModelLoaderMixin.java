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
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoaderHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

@Mixin(ModelBakery.class)
public abstract class ModelLoaderMixin implements ModelLoaderHooks {
    // The missing model is always loaded and added first.
    @Final
    @Shadow
    public static ModelResourceLocation MISSING_MODEL_LOCATION;
    @Final
    @Shadow
    private Set<ResourceLocation> loadingStack;
    @Final
    @Shadow
    private Map<ResourceLocation, UnbakedModel> unbakedCache;
    @Shadow
    @Final
    private Map<ResourceLocation, UnbakedModel> topLevelModels;

    @Unique
    private ModelLoadingEventDispatcher fabric_eventDispatcher;
    // Explicitly not @Unique to allow mods that heavily rework model loading to reimplement the guard.
    // Note that this is an implementation detail; it can change at any time.
    private int fabric_guardGetOrLoadModel = 0;
    private boolean fabric_enableGetOrLoadModelGuard = true;

    @Shadow
    private void loadTopLevel(ModelResourceLocation id) {
    }

    @Shadow
    public abstract UnbakedModel getModel(ResourceLocation id);

    @Shadow
    private void loadModel(ResourceLocation id) {
    }

    @Shadow
    private void cacheAndQueueDependencies(ResourceLocation id, UnbakedModel unbakedModel) {
    }

    @Shadow
    public abstract BlockModel loadBlockModel(ResourceLocation id);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 0))
    private void afterMissingModelInit(BlockColors blockColors, ProfilerFiller profiler, Map<ResourceLocation, BlockModel> jsonUnbakedModels, Map<ResourceLocation, List<ModelBakery.LoadedJson>> blockStates, CallbackInfo info) {
        // Sanity check
        if (!unbakedCache.containsKey(MISSING_MODEL_LOCATION)) {
            throw new AssertionError("Missing model not initialized. This is likely a Fabric API porting bug.");
        }

        profiler.popPush("fabric_plugins_init");

        fabric_eventDispatcher = new ModelLoadingEventDispatcher((ModelBakery) (Object) this, ModelLoadingPluginManager.CURRENT_PLUGINS.get());
        ModelLoadingPluginManager.CURRENT_PLUGINS.remove();
        fabric_eventDispatcher.addExtraModels(this::addModel);
    }

    @Unique
    private void addModel(ResourceLocation id) {
        if (id instanceof ModelResourceLocation) {
            loadTopLevel((ModelResourceLocation) id);
        } else {
            // The vanilla addModel method is arbitrarily limited to ModelIdentifiers,
            // but it's useful to tell the game to just load and bake a direct model path as well.
            // Replicate the vanilla logic of addModel here.
            UnbakedModel unbakedModel = getModel(id);
            this.unbakedCache.put(id, unbakedModel);
            this.topLevelModels.put(id, unbakedModel);
        }
    }

    @Inject(method = "getModel", at = @At("HEAD"))
    private void fabric_preventNestedGetOrLoadModel(ResourceLocation id, CallbackInfoReturnable<UnbakedModel> cir) {
        if (fabric_enableGetOrLoadModelGuard && fabric_guardGetOrLoadModel > 0) {
            throw new IllegalStateException("ModelLoader#getOrLoadModel called from a ModelResolver or ModelModifier.OnBake instance. This is not allowed to prevent errors during model loading. Use getOrLoadModel from the context instead.");
        }
    }

    @Inject(method = "loadModel", at = @At("HEAD"), cancellable = true)
    private void onLoadModel(ResourceLocation id, CallbackInfo ci) {
        // Prevent calls to getOrLoadModel from loadModel as it will cause problems.
        // Mods should call getOrLoadModel on the ModelResolver.Context instead.
        fabric_guardGetOrLoadModel++;

        try {
            if (fabric_eventDispatcher.loadModel(id)) {
                ci.cancel();
            }
        } finally {
            fabric_guardGetOrLoadModel--;
        }
    }

    @ModifyVariable(method = "cacheAndQueueDependencies", at = @At("HEAD"), argsOnly = true)
    private UnbakedModel onPutModel(UnbakedModel model, ResourceLocation id) {
        fabric_guardGetOrLoadModel++;

        try {
            return fabric_eventDispatcher.modifyModelOnLoad(id, model);
        } finally {
            fabric_guardGetOrLoadModel--;
        }
    }

    @Override
    public ModelLoadingEventDispatcher fabric_getDispatcher() {
        return fabric_eventDispatcher;
    }

    @Override
    public UnbakedModel fabric_getMissingModel() {
        return unbakedCache.get(MISSING_MODEL_LOCATION);
    }

    /**
     * Unlike getOrLoadModel, this method supports nested model loading.
     *
     * <p>Vanilla does not due to the iteration over modelsToLoad which causes models to be resolved multiple times,
     * possibly leading to crashes.
     */
    @Override
    public UnbakedModel fabric_getOrLoadModel(ResourceLocation id) {
        if (this.unbakedCache.containsKey(id)) {
            return this.unbakedCache.get(id);
        }

        if (!loadingStack.add(id)) {
            throw new IllegalStateException("Circular reference while loading " + id);
        }

        try {
            loadModel(id);
        } finally {
            loadingStack.remove(id);
        }

        return unbakedCache.get(id);
    }

    @Override
    public void fabric_putModel(ResourceLocation id, UnbakedModel model) {
        cacheAndQueueDependencies(id, model);
    }

    @Override
    public void fabric_putModelDirectly(ResourceLocation id, UnbakedModel model) {
        unbakedCache.put(id, model);
    }

    @Override
    public void fabric_queueModelDependencies(UnbakedModel model) {
        loadingStack.addAll(model.getDependencies());
    }

    @Override
    public BlockModel fabric_loadModelFromJson(ResourceLocation id) {
        return loadBlockModel(id);
    }
}
