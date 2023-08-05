//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.fabricmc.fabric.impl.client.model.loading;

import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
record BlockStateResolverHolder(BlockStateResolver resolver, Block block, ResourceLocation blockId) {
    BlockStateResolverHolder(BlockStateResolver resolver, Block block, ResourceLocation blockId) {
        this.resolver = resolver;
        this.block = block;
        this.blockId = blockId;
    }

    public BlockStateResolver resolver() {
        return this.resolver;
    }

    public Block block() {
        return this.block;
    }

    public ResourceLocation blockId() {
        return this.blockId;
    }
}
