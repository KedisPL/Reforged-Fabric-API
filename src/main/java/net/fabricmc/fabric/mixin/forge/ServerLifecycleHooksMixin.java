package net.fabricmc.fabric.mixin.forge;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLifecycleHooks.class)
public class ServerLifecycleHooksMixin {
    @Inject(method = "handleServerStarting", at = @At(value = "HEAD", target = "Lnet/minecraftforge/server/ServerLifecycleHooks;handleServerStarting(Lnet/minecraft/server/MinecraftServer;)Z"), cancellable = true, remap = false)
    private static void serverStarting(final MinecraftServer minecraftServer, CallbackInfoReturnable<Boolean> callbackInfo) {
        ServerLifecycleEvents.SERVER_STARTING.invoker().onServerStarting(minecraftServer);
    }

    @Inject(method = "handleServerStarted", at = @At(value = "HEAD", target = "Lnet/minecraftforge/server/ServerLifecycleHooks;handleServerStarted(Lnet/minecraft/server/MinecraftServer;)Z"), cancellable = true, remap = false)
    private static void serverStarted(final MinecraftServer minecraftServer, CallbackInfo callbackInfo) {
        ServerLifecycleEvents.SERVER_STARTED.invoker().onServerStarted(minecraftServer);
    }

    @Inject(method = "handleServerStopping", at = @At(value = "HEAD", target = "Lnet/minecraftforge/server/ServerLifecycleHooks;handleServerStopping(Lnet/minecraft/server/MinecraftServer;)Z"), cancellable = true, remap = false)
    private static void serverStopping(final MinecraftServer minecraftServer, CallbackInfo callbackInfo) {
        ServerLifecycleEvents.SERVER_STOPPING.invoker().onServerStopping(minecraftServer);
    }

    @Inject(method = "handleServerStopper", at = @At(value = "HEAD", target = "Lnet/minecraftforge/server/ServerLifecycleHooks;handleServerStopped(Lnet/minecraft/server/MinecraftServer;)Z"), cancellable = true, remap = false)
    private static void serverStopped(final MinecraftServer minecraftServer, CallbackInfo callbackInfo) {
        ServerLifecycleEvents.SERVER_STOPPED.invoker().onServerStopped(minecraftServer);
    }
}
