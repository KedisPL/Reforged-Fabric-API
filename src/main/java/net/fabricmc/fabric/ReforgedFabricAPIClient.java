package net.fabricmc.fabric;

import net.fabricmc.fabric.impl.client.registry.sync.FabricRegistryClientInit;
import net.fabricmc.fabric.impl.event.interaction.InteractionEventsRouterClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReforgedFabricAPIClient {
    @OnlyIn(Dist.CLIENT)
    public static void clientSetup(){
        FabricRegistryClientInit.onInitializeClient();
        InteractionEventsRouterClient.onInitializeClient();
    }
}
