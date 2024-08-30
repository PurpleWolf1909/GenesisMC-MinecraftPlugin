package io.github.dueris.originspaper.registry;

import io.github.dueris.calio.registry.RegistryKey;
import io.github.dueris.calio.util.Util;
import io.github.dueris.originspaper.action.factory.ActionTypeFactory;
import io.github.dueris.originspaper.condition.factory.ConditionTypeFactory;
import io.github.dueris.originspaper.data.types.modifier.IModifierOperation;
import io.github.dueris.originspaper.origin.Origin;
import io.github.dueris.originspaper.origin.OriginLayer;
import io.github.dueris.originspaper.power.factory.PowerType;
import io.github.dueris.originspaper.screen.ChoosingPage;
import io.github.dueris.originspaper.util.LangFile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Registries {
	public static final RegistryKey<Origin> ORIGIN = new RegistryKey<>(Origin.class, apoliIdentifier("origin"));
	public static final RegistryKey<OriginLayer> LAYER = new RegistryKey<>(OriginLayer.class, apoliIdentifier("layer"));
	public static final RegistryKey<PowerType> POWER = new RegistryKey<>(PowerType.class, apoliIdentifier("craft_power"));

	public static final RegistryKey<ConditionTypeFactory<FluidState>> FLUID_CONDITION = new RegistryKey<>(Util.castClass(ConditionTypeFactory.class), apoliIdentifier("fluid_condition"));
	public static final RegistryKey<ConditionTypeFactory<Tuple<Level, ItemStack>>> ITEM_CONDITION = new RegistryKey<>(Util.castClass(ConditionTypeFactory.class), apoliIdentifier("item_condition"));
	public static final RegistryKey<ConditionTypeFactory<Entity>> ENTITY_CONDITION = new RegistryKey<>(Util.castClass(ConditionTypeFactory.class), apoliIdentifier("entity_condition"));
	public static final RegistryKey<ConditionTypeFactory<Tuple<DamageSource, Float>>> DAMAGE_CONDITION = new RegistryKey<>(Util.castClass(ConditionTypeFactory.class), apoliIdentifier("damage_condition"));
	public static final RegistryKey<ConditionTypeFactory<Tuple<Entity, Entity>>> BIENTITY_CONDITION = new RegistryKey<>(Util.castClass(ConditionTypeFactory.class), apoliIdentifier("bientity_condition"));
	public static final RegistryKey<ConditionTypeFactory<BlockInWorld>> BLOCK_CONDITION = new RegistryKey<>(Util.castClass(ConditionTypeFactory.class), apoliIdentifier("block_condition"));
	public static final RegistryKey<ConditionTypeFactory<Tuple<BlockPos, Holder<Biome>>>> BIOME_CONDITION = new RegistryKey<>(Util.castClass(ConditionTypeFactory.class), apoliIdentifier("biome_condition"));

	public static final RegistryKey<ActionTypeFactory<Tuple<Level, SlotAccess>>> ITEM_ACTION = new RegistryKey<>(Util.castClass(ActionTypeFactory.class), apoliIdentifier("item_action"));
	public static final RegistryKey<ActionTypeFactory<Entity>> ENTITY_ACTION = new RegistryKey<>(Util.castClass(ActionTypeFactory.class), apoliIdentifier("entity_action"));
	public static final RegistryKey<ActionTypeFactory<Tuple<Entity, Entity>>> BIENTITY_ACTION = new RegistryKey<>(Util.castClass(ActionTypeFactory.class), apoliIdentifier("bientity_action"));
	public static final RegistryKey<ActionTypeFactory<Triple<Level, BlockPos, Direction>>> BLOCK_ACTION = new RegistryKey<>(Util.castClass(ActionTypeFactory.class), apoliIdentifier("block_action"));

	public static final RegistryKey<LangFile> LANG = new RegistryKey<>(LangFile.class, identifier("lang_file"));
	public static final RegistryKey<ChoosingPage> CHOOSING_PAGE = new RegistryKey<>(ChoosingPage.class, originIdentifier("choosing_page"));
	public static final RegistryKey<IModifierOperation> MODIFIER_OPERATION = new RegistryKey<>(IModifierOperation.class, apoliIdentifier("modifier_operation"));

	@Contract("_ -> new")
	public static @NotNull ResourceLocation identifier(String path) {
		return ResourceLocation.fromNamespaceAndPath("originspaper", path);
	}

	@Contract("_ -> new")
	public static @NotNull ResourceLocation originIdentifier(String path) {
		return ResourceLocation.fromNamespaceAndPath("origins", path);
	}

	@Contract("_ -> new")
	public static @NotNull ResourceLocation apoliIdentifier(String path) {
		return ResourceLocation.fromNamespaceAndPath("apoli", path);
	}
}
