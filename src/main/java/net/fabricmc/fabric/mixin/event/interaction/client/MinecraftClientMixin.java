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

package net.fabricmc.fabric.mixin.event.interaction.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    private boolean fabric_itemPickCancelled;
    private boolean fabric_attackCancelled;

    @SuppressWarnings("deprecation")
    private ItemStack fabric_emulateOldPick() {
        Minecraft client = (Minecraft) (Object) this;
        ClientPickBlockCallback.Container ctr = new ClientPickBlockCallback.Container(ItemStack.EMPTY);
        ClientPickBlockCallback.EVENT.invoker().pick(client.player, client.hitResult, ctr);
        return ctr.getStack();
    }

    @Inject(at = @At("HEAD"), method = "pickBlock", cancellable = true)
    private void fabric_doItemPickWrapper(CallbackInfo info) {
        Minecraft client = (Minecraft) (Object) this;

        // Do a "best effort" emulation of the old events.
        ItemStack stack = ClientPickBlockGatherCallback.EVENT.invoker().pick(client.player, client.hitResult);

        // TODO: Remove in 0.3.0
        if (stack.isEmpty()) {
            stack = fabric_emulateOldPick();
        }

        if (stack.isEmpty()) {
            // fall through
        } else {
            info.cancel();

            // I don't like that we clone vanilla logic here, but it's our best bet for now.
            Inventory playerInventory = client.player.getInventory();

            if (client.player.getAbilities().instabuild && Screen.hasControlDown() && client.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockEntity be = client.level.getBlockEntity(((BlockHitResult) client.hitResult).getBlockPos());

                if (be != null) {
                    addCustomNbtData(stack, be);
                }
            }

            stack = ClientPickBlockApplyCallback.EVENT.invoker().pick(client.player, client.hitResult, stack);

            if (stack.isEmpty()) {
                return;
            }

            if (client.player.getAbilities().instabuild) {
                playerInventory.setPickedItem(stack);
                client.gameMode.handleCreativeModeItemAdd(client.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + playerInventory.selected);
            } else {
                int slot = playerInventory.findSlotMatchingItem(stack);

                if (slot >= 0) {
                    if (Inventory.isHotbarSlot(slot)) {
                        playerInventory.selected = slot;
                    } else {
                        client.gameMode.handlePickItem(slot);
                    }
                }
            }
        }
    }

    @Shadow
    protected abstract void addCustomNbtData(ItemStack itemStack_1, BlockEntity blockEntity_1);

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingItem(Lnet/minecraft/world/item/ItemStack;)I", shift = At.Shift.BEFORE), method = "pickBlock", ordinal = 0)
    public ItemStack modifyItemPick(ItemStack stack) {
        Minecraft client = (Minecraft) (Object) this;
        ItemStack result = ClientPickBlockApplyCallback.EVENT.invoker().pick(client.player, client.hitResult, stack);
        fabric_itemPickCancelled = result.isEmpty();
        return result;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingItem(Lnet/minecraft/world/item/ItemStack;)I"), method = "pickBlock", cancellable = true)
    public void cancelItemPick(CallbackInfo info) {
        if (fabric_itemPickCancelled) {
            info.cancel();
        }
    }

    @Shadow
    private LocalPlayer player;

    @Shadow
    public abstract ClientPacketListener getConnection();

    @Shadow
    @Final
    public Options options;

    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/multiplayer/PlayerController.interactAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/EntityRayTraceResult;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResultType;"
            ),
            method = "startUseItem",
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void injectUseEntityCallback(CallbackInfo ci, InteractionHand[] hands, int i1, int i2, InteractionHand hand, ItemStack stack, EntityHitResult hitResult, Entity entity) {
        InteractionResult result = UseEntityCallback.EVENT.invoker().interact(player, player.getCommandSenderWorld(), hand, entity, hitResult);

        if (result != InteractionResult.PASS) {
            if (result.consumesAction()) {
                Vec3 hitVec = hitResult.getLocation().subtract(entity.getX(), entity.getY(), entity.getZ());
                getConnection().send(ServerboundInteractPacket.createInteractionPacket(entity, player.isShiftKeyDown(), hand, hitVec));
            }

            if (result.shouldSwing()) {
                player.swing(hand);
            }

            ci.cancel();
        }
    }

    @Inject(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
                    ordinal = 0
            )
    )
    private void injectHandleInputEventsForPreAttackCallback(CallbackInfo ci) {
        int attackKeyPressCount = ((KeyBindingAccessor) options.keyAttack).fabric_getTimesPressed();

        if (options.keyAttack.isDown() || attackKeyPressCount != 0) {
            fabric_attackCancelled = ClientPreAttackCallback.EVENT.invoker().onClientPlayerPreAttack(
                    (Minecraft) (Object) this, player, attackKeyPressCount
            );
        } else {
            fabric_attackCancelled = false;
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void injectDoAttackForCancelling(CallbackInfoReturnable<Boolean> cir) {
        if (fabric_attackCancelled) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void injectHandleBlockBreakingForCancelling(boolean breaking, CallbackInfo ci) {
        if (fabric_attackCancelled) {
            if (gameMode != null) {
                gameMode.stopDestroyBlock();
            }

            ci.cancel();
        }
    }
}
