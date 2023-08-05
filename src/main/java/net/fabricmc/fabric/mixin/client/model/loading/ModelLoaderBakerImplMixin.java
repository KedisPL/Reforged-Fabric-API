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

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoaderHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

@Mixin(targets = "net/minecraft/client/resources/model/ModelBakery$ModelBakerImpl")
public class ModelLoaderBakerImplMixin {
    @Shadow
    @Final
    private ModelBakery this$0;
    @Shadow
    @Final
    private Function<Material, TextureAtlasSprite> modelTextureGetter;

    @ModifyVariable(method = "bake", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/resources/model/ModelBakery$ModelBakerImpl;getModel(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/UnbakedModel;"))
    private UnbakedModel invokeModifyBeforeBake(UnbakedModel model, ResourceLocation id, ModelState settings) {
        ModelLoadingEventDispatcher dispatcher = ((ModelLoaderHooks) (ModelLoaderHooks)this).fabric_getDispatcher();
        return dispatcher.modifyModelBeforeBake(model, id, modelTextureGetter, settings, (ModelBaker) this);
    }

    @Redirect(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/UnbakedModel;bake(Lnet/minecraft/client/resources/model/ModelBaker;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/BakedModel;"))
    private BakedModel invokeModifyAfterBake(UnbakedModel unbakedModel, ModelBaker baker, Function<Material, TextureAtlasSprite> textureGetter, ModelState settings, ResourceLocation id) {
        BakedModel model = unbakedModel.bake(baker, textureGetter, settings, id);
        ModelLoadingEventDispatcher dispatcher = ((ModelLoaderHooks) this.this$0).fabric_getDispatcher();
        return dispatcher.modifyModelAfterBake(model, id, unbakedModel, textureGetter, settings, baker);
    }

    @Redirect(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockModel;bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/resources/model/BakedModel;"))
    private BakedModel invokeModifyAfterBake(BlockModel unbakedModel, ModelBaker baker, BlockModel parent, Function<Material, TextureAtlasSprite> textureGetter, ModelState settings, ResourceLocation id, boolean hasDepth) {
        BakedModel model = unbakedModel.bake(baker, parent, textureGetter, settings, id, hasDepth);
        ModelLoadingEventDispatcher dispatcher = ((ModelLoaderHooks) this.this$0).fabric_getDispatcher();
        return dispatcher.modifyModelAfterBake(model, id, unbakedModel, textureGetter, settings, baker);
    }
}
