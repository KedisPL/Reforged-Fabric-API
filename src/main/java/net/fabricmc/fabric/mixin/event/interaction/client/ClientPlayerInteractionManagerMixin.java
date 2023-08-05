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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private ClientPacketListener connection;
    @Shadow
    private GameType localPlayerMode;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameType;isCreative()Z", ordinal = 0), method = "startDestroyBlock", cancellable = true)
    public void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        fabric_fireAttackBlockCallback(pos, direction, info);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameType;isCreative()Z", ordinal = 0), method = "continueDestroyBlock", cancellable = true)
    public void method_2902(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        if (localPlayerMode.isCreative()) {
            fabric_fireAttackBlockCallback(pos, direction, info);
        }
    }

    @Unique
    private void fabric_fireAttackBlockCallback(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        InteractionResult result = AttackBlockCallback.EVENT.invoker().interact(minecraft.player, minecraft.level, InteractionHand.MAIN_HAND, pos, direction);

        if (result != InteractionResult.PASS) {
            // Returning true will spawn particles and trigger the animation of the hand -> only for SUCCESS.
            info.setReturnValue(result == InteractionResult.SUCCESS);

            // We also need to let the server process the action if it's accepted.
            if (result.consumesAction()) {
                startPrediction(minecraft.level, id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction, id));
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"), method = "useItemOn", cancellable = true)
    public void interactBlock(LocalPlayer player, InteractionHand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> info) {
        // hook interactBlock between the world border check and the actual block interaction to invoke the use block event first
        // this needs to be in interactBlock to avoid sending a packet in line with the event javadoc

        if (player.isSpectator()) return; // vanilla spectator check happens later, repeat it before the event to avoid false invocations

        InteractionResult result = UseBlockCallback.EVENT.invoker().interact(player, player.level(), hand, blockHitResult);

        if (result != InteractionResult.PASS) {
            if (result == InteractionResult.SUCCESS) {
                // send interaction packet to the server with a new sequentially assigned id
                startPrediction(player.clientLevel, id -> new ServerboundUseItemOnPacket(hand, blockHitResult, id));
            }

            info.setReturnValue(result);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0), method = "useItem", cancellable = true)
    public void interactItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
        // hook interactBlock between the spectator check and sending the first packet to invoke the use item event first
        // this needs to be in interactBlock to avoid sending a packet in line with the event javadoc
        InteractionResultHolder<ItemStack> result = UseItemCallback.EVENT.invoker().interact(player, player.level(), hand);

        if (result.getResult() != InteractionResult.PASS) {
            if (result.getResult() == InteractionResult.SUCCESS) {
                // send the move packet like vanilla to ensure the position+view vectors are accurate
                connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.onGround()));
                // send interaction packet to the server with a new sequentially assigned id
                startPrediction((ClientLevel) player.level(), id -> new ServerboundUseItemPacket(hand, id));
            }

            info.setReturnValue(result.getResult());
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0), method = "attack", cancellable = true)
    public void attackEntity(Player player, Entity entity, CallbackInfo info) {
        InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(player, player.getCommandSenderWorld(), InteractionHand.MAIN_HAND /* TODO */, entity, null);

        if (result != InteractionResult.PASS) {
            if (result == InteractionResult.SUCCESS) {
                this.connection.send(ServerboundInteractPacket.createAttackPacket(entity, player.isShiftKeyDown()));
            }

            info.cancel();
        }
    }

    @Shadow
    public abstract void startPrediction(ClientLevel clientWorld, PredictiveAction supplier);
}
