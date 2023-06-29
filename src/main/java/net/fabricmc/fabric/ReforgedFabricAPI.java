package net.fabricmc.fabric;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl;
import net.fabricmc.fabric.impl.event.lifecycle.LifecycleEventsImpl;
import net.fabricmc.fabric.impl.lookup.ApiLookupImpl;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

import static net.fabricmc.fabric.ReforgedFabricAPIConstants.MOD_ID;

@Mod(MOD_ID)
@Mod.EventBusSubscriber
public class ReforgedFabricAPI {
    public ReforgedFabricAPI() {
        ApiLookupImpl.onInitialize();
        LifecycleEventsImpl.onInitialize();
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    public void registerCommands(RegisterCommandsEvent event) {
        CommandRegistrationCallback.EVENT.invoker().register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }
}
