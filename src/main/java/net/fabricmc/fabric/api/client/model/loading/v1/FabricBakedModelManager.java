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

package net.fabricmc.fabric.api.client.model.loading.v1;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric-provided helper methods for {@link ModelManager}.
 *
 * <p>Note: This interface is automatically implemented on the {@link ModelManager} via Mixin and interface injection.
 */
public interface FabricBakedModelManager {
    /**
     * An alternative to {@link ModelManager#getModel(ModelResourceLocation)} that accepts an
     * {@link ResourceLocation} instead. Models loaded using {@link ModelLoadingPlugin.Context#addModels}
     * do not have a corresponding {@link ModelResourceLocation}, so the vanilla method cannot be used to
     * retrieve them. The {@link ResourceLocation} that was used to load them can be used in this method
     * to retrieve them.
     *
     * <p><b>This method, as well as its vanilla counterpart, should only be used after the
     * {@link ModelManager} has completed reloading.</b> Otherwise, the result will be
     * outdated or null.
     *
     * @param id the id of the model
     * @return the model
     */
    @Nullable
    default BakedModel getModel(ResourceLocation id) {
        throw new UnsupportedOperationException("Implemented via mixin.");
    }
}
