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

package net.fabricmc.fabric.api.object.builder.v1.block;

import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.fabricmc.fabric.mixin.object.builder.AbstractBlockAccessor;
import net.fabricmc.fabric.mixin.object.builder.AbstractBlockSettingsAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Fabric's version of Block.Settings. Adds additional methods and hooks
 * not found in the original class.
 *
 * <p>Make note that this behaves slightly different from the
 * vanilla counterpart, copying some settings that vanilla does not.
 *
 * <p>To use it, simply replace Block.Settings.of() with
 * FabricBlockSettings.of().
 */
public class FabricBlockSettings extends BlockBehaviour.Properties {
	protected FabricBlockSettings() {
		super();
	}

	protected FabricBlockSettings(BlockBehaviour.Properties settings) {
		this();
		// Mostly Copied from vanilla's copy method
		// Note: If new methods are added to Block settings, an accessor must be added here
		AbstractBlockSettingsAccessor thisAccessor = (AbstractBlockSettingsAccessor) this;
		AbstractBlockSettingsAccessor otherAccessor = (AbstractBlockSettingsAccessor) settings;

		// Copied in vanilla: sorted by vanilla copy order
		this.destroyTime(otherAccessor.getDestroyTime());
		this.explosionResistance(otherAccessor.getExplosionResistance());
		this.collidable(otherAccessor.getHasCollision());
		thisAccessor.setIsRandomlyTicking(otherAccessor.getIsRandomlyTicking());
		this.lightLevel(otherAccessor.getLightEmission());
		thisAccessor.setMapColor(otherAccessor.getMapColor());
		this.sound(otherAccessor.getSoundType());
		this.friction(otherAccessor.getFriction());
		this.speedFactor(otherAccessor.getSpeedFactor());
		thisAccessor.setDynamicShape(otherAccessor.getDynamicShape());
		thisAccessor.setCanOcclude(otherAccessor.getCanOcclude());
		thisAccessor.setIsAir(otherAccessor.getIsAir());
		thisAccessor.setRequiresCorrectToolForDrops(otherAccessor.isRequiresCorrectToolForDrops());
		thisAccessor.setOffsetFunction(otherAccessor.getOffsetFunction());
		thisAccessor.setSpawnParticlesOnBreak(otherAccessor.getSpawnParticlesOnBreak());
		thisAccessor.setRequiredFeatures(otherAccessor.getRequiredFeatures());

		// Not copied in vanilla: field definition order
		this.jumpFactor(otherAccessor.getJumpFactor());
		this.drops(otherAccessor.getDrops());
		this.isValidSpawn(otherAccessor.getIsValidSpawn());
		this.isRedstoneConductor(otherAccessor.getIsRedstoneConductor());
		this.isSuffocating(otherAccessor.getIsSuffocating());
		this.isViewBlocking(otherAccessor.getIsViewBlocking());
		this.hasPostProcess(otherAccessor.getHasPostProcess());
		this.emissiveRendering(otherAccessor.getEmissiveRendering());
	}

	public static FabricBlockSettings create() {
		return new FabricBlockSettings();
	}

	/**
	 * @deprecated Use {@link FabricBlockSettings#create()} ()} instead.
	 */
	@Deprecated
	public static FabricBlockSettings of() {
		return create();
	}

	public static FabricBlockSettings copyOf(BlockBehaviour block) {
		return new FabricBlockSettings(((AbstractBlockAccessor) block).getProperties());
	}

	public static FabricBlockSettings copyOf(BlockBehaviour.Properties settings) {
		return new FabricBlockSettings(settings);
	}

	@Override
	public FabricBlockSettings noCollission() {
		super.noCollission();
		return this;
	}

	@Override
	public FabricBlockSettings noOcclusion() {
		super.noOcclusion();
		return this;
	}

	@Override
	public FabricBlockSettings friction(float value) {
		super.friction(value);
		return this;
	}

	@Override
	public FabricBlockSettings speedFactor(float velocityMultiplier) {
		super.speedFactor(velocityMultiplier);
		return this;
	}

	@Override
	public FabricBlockSettings jumpFactor(float jumpVelocityMultiplier) {
		super.jumpFactor(jumpVelocityMultiplier);
		return this;
	}

	@Override
	public FabricBlockSettings sound(SoundType group) {
		super.sound(group);
		return this;
	}

	///**
	// * @deprecated Please use {@link FabricBlockSettings#lightLevel(ToIntFunction)}.
	// */
	//@Deprecated
	//public FabricBlockSettings lightLevel(ToIntFunction<BlockState> levelFunction) {
	//	return this.lightLevel(levelFunction);
	//}

	@Override
	public FabricBlockSettings lightLevel(ToIntFunction<BlockState> luminanceFunction) {
		super.lightLevel(luminanceFunction);
		return this;
	}

	@Override
	public FabricBlockSettings strength(float hardness, float resistance) {
		super.strength(hardness, resistance);
		return this;
	}

	@Override
	public FabricBlockSettings instabreak() {
		super.instabreak();
		return this;
	}

	public FabricBlockSettings strength(float strength) {
		super.strength(strength);
		return this;
	}

	@Override
	public FabricBlockSettings randomTicks() {
		super.randomTicks();
		return this;
	}

	@Override
	public FabricBlockSettings dynamicShape() {
		super.dynamicShape();
		return this;
	}

	@Override
	public FabricBlockSettings noLootTable() {
		super.noLootTable();
		return this;
	}

	@Override
	public FabricBlockSettings dropsLike(Block block) {
		super.dropsLike(block);
		return this;
	}

	@Override
	public FabricBlockSettings air() {
		super.air();
		return this;
	}

	@Override
	public FabricBlockSettings isValidSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> predicate) {
		super.isValidSpawn(predicate);
		return this;
	}

	@Override
	public FabricBlockSettings isRedstoneConductor(BlockBehaviour.StatePredicate predicate) {
		super.isRedstoneConductor(predicate);
		return this;
	}

	@Override
	public FabricBlockSettings isSuffocating(BlockBehaviour.StatePredicate predicate) {
		super.isSuffocating(predicate);
		return this;
	}

	@Override
	public FabricBlockSettings isViewBlocking(BlockBehaviour.StatePredicate predicate) {
		super.isViewBlocking(predicate);
		return this;
	}

	@Override
	public FabricBlockSettings hasPostProcess(BlockBehaviour.StatePredicate predicate) {
		super.hasPostProcess(predicate);
		return this;
	}

	@Override
	public FabricBlockSettings emissiveRendering(BlockBehaviour.StatePredicate predicate) {
		super.emissiveRendering(predicate);
		return this;
	}

	/**
	 * Make the block require tool to drop and slows down mining speed if the incorrect tool is used.
	 */
	@Override
	public FabricBlockSettings requiresCorrectToolForDrops() {
		super.requiresCorrectToolForDrops();
		return this;
	}

	@Override
	public FabricBlockSettings mapColor(MapColor color) {
		super.mapColor(color);
		return this;
	}

	@Override
	public FabricBlockSettings destroyTime(float hardness) {
		super.destroyTime(hardness);
		return this;
	}

	@Override
	public FabricBlockSettings explosionResistance(float resistance) {
		super.explosionResistance(resistance);
		return this;
	}

	@Override
	public FabricBlockSettings offsetType(BlockBehaviour.OffsetType offsetType) {
		super.offsetType(offsetType);
		return this;
	}

	@Override
	public FabricBlockSettings noParticlesOnBreak() {
		super.noParticlesOnBreak();
		return this;
	}

	@Override
	public FabricBlockSettings requiredFeatures(FeatureFlag... features) {
		super.requiredFeatures(features);
		return this;
	}

	@Override
	public FabricBlockSettings mapColor(Function<BlockState, MapColor> mapColorProvider) {
		super.mapColor(mapColorProvider);
		return this;
	}

	@Override
	public FabricBlockSettings ignitedByLava() {
		super.ignitedByLava();
		return this;
	}

	@Override
	public FabricBlockSettings liquid() {
		super.liquid();
		return this;
	}

	@Override
	public FabricBlockSettings forceSolidOn() {
		super.forceSolidOn();
		return this;
	}

	@Override
	public FabricBlockSettings forceSolidOff() {
		super.forceSolidOff();
		return this;
	}

	@Override
	public FabricBlockSettings pushReaction(PushReaction pistonBehavior) {
		super.pushReaction(pistonBehavior);
		return this;
	}

	@Override
	public FabricBlockSettings instrument(NoteBlockInstrument instrument) {
		super.instrument(instrument);
		return this;
	}

	@Override
	public FabricBlockSettings replaceable() {
		super.replaceable();
		return this;
	}

	/* FABRIC ADDITIONS*/

	/**
	 * @deprecated Please use {@link FabricBlockSettings#luminance(int)}.
	 */
	@Deprecated
	public FabricBlockSettings lightLevele(int lightLevel) {
		this.luminance(lightLevel);
		return this;
	}

	public FabricBlockSettings luminance(int luminance) {
		this.lightLevel(ignored -> luminance);
		return this;
	}

	public FabricBlockSettings drops(ResourceLocation dropTableId) {
		((AbstractBlockSettingsAccessor) this).setDrops(dropTableId);
		return this;
	}

	/* FABRIC DELEGATE WRAPPERS */

	/**
	 * @deprecated Please migrate to {@link FabricBlockSettings#mapColor(MapColor)}
	 */
	@Deprecated
	public FabricBlockSettings materialColor(MapColor color) {
		return this.mapColor(color);
	}

	/**
	 * @deprecated Please migrate to {@link FabricBlockSettings#mapColor(DyeColor)}
	 */
	@Deprecated
	public FabricBlockSettings materialColor(DyeColor color) {
		return this.mapColor(color);
	}

	public FabricBlockSettings mapColor(DyeColor color) {
		return this.mapColor(color.getMapColor());
	}

	public FabricBlockSettings collidable(boolean collidable) {
		((AbstractBlockSettingsAccessor) this).setHasCollision(collidable);
		return this;
	}
}
