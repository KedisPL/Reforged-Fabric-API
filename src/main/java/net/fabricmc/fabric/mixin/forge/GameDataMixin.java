package net.fabricmc.fabric.mixin.forge;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraftforge.registries.*;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

import static net.minecraftforge.registries.ForgeRegistry.REGISTRIES;

@Mixin(GameData.class)
public class GameDataMixin {
    @Shadow(remap = false) @Final
    private static final Logger LOGGER = LogManager.getLogger();
    @Overwrite(remap = false)
    public static void vanillaSnapshot()
    {
        unfreezeData();
        LOGGER.debug(REGISTRIES, "Creating vanilla freeze snapshot");
        LOGGER.debug(REGISTRIES, "Vanilla freeze snapshot created");
    }

    @Shadow(remap = false)
    public static void unfreezeData()
    {
        LOGGER.debug(REGISTRIES, "Unfreezing vanilla registries");
        BuiltInRegistries.REGISTRY.stream().filter(r -> r instanceof MappedRegistry).forEach(r -> ((MappedRegistry<?>)r).unfreeze());
    }
}
