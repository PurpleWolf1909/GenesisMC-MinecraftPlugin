package io.github.dueris.originspaper.condition.factory;

import io.github.dueris.calio.data.SerializableData;
import io.github.dueris.originspaper.OriginsPaper;
import io.github.dueris.originspaper.condition.type.fluid.FluidConditionType;
import io.github.dueris.originspaper.condition.type.fluid.InTagConditionType;
import io.github.dueris.originspaper.data.ApoliDataTypes;
import io.github.dueris.originspaper.registry.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FluidState;

import java.util.function.Predicate;

public class FluidConditions {

	public static void register() {
		MetaConditions.register(ApoliDataTypes.FLUID_CONDITION, FluidConditions::register);
		register(createSimpleFactory(OriginsPaper.apoliIdentifier("empty"), FluidState::isEmpty));
		register(createSimpleFactory(OriginsPaper.apoliIdentifier("still"), FluidState::isSource));
		register(InTagConditionType.getFactory());
		register(FluidConditionType.getFactory());
	}

	public static ConditionTypeFactory<FluidState> createSimpleFactory(ResourceLocation id, Predicate<FluidState> condition) {
		return new ConditionTypeFactory<>(id, new SerializableData(), (data, fluidState) -> {
			return condition.test(fluidState);
		});
	}

	public static ConditionTypeFactory<FluidState> register(ConditionTypeFactory<FluidState> conditionFactory) {
		return OriginsPaper.getRegistry().retrieve(Registries.FLUID_CONDITION).register(conditionFactory, conditionFactory.getSerializerId());
	}
}