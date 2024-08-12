package me.dueris.originspaper.data.types.modifier;

import io.github.dueris.calio.SerializableDataTypes;
import io.github.dueris.calio.parser.InstanceDefiner;
import io.github.dueris.calio.parser.reader.DeserializedFactoryJson;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.stream.Collectors;

public enum ModifierOperation implements IModifierOperation {

	ADD_BASE_EARLY(Phase.BASE, 0, (values, base, current) -> base + values.stream().reduce(0.0, Double::sum)),
	MULTIPLY_BASE_ADDITIVE(Phase.BASE, 100, (values, base, current) ->
		current + (base * values.stream().reduce(0.0, Double::sum))),
	MULTIPLY_BASE_MULTIPLICATIVE(Phase.BASE, 200, (values, base, current) -> {
		double value = current;
		for (double v : values) {
			value *= (1 + v);
		}
		return value;
	}),
	ADD_BASE_LATE(Phase.BASE, 300, (values, base, current) -> {
		double value = current;
		for (double v : values) {
			value += v;
		}
		return value;
	}),
	MIN_BASE(Phase.BASE, 400, (values, base, current) -> {
		double value = current;
		for (double v : values) {
			value = Math.max(v, value);
		}
		return value;
	}),
	MAX_BASE(Phase.BASE, 500, (values, base, current) -> {
		double value = current;
		for (double v : values) {
			value = Math.min(v, value);
		}
		return value;
	}),
	SET_BASE(Phase.BASE, 600, (values, base, current) -> {
		double value = current;
		for (double v : values) {
			value = v;
		}
		return value;
	}),
	MULTIPLY_TOTAL_ADDITIVE(Phase.TOTAL, 0, (values, base, current) ->
		current + (base * values.stream().reduce(0.0, Double::sum))),
	MULTIPLY_TOTAL_MULTIPLICATIVE(Phase.TOTAL, 100, (values, base, current) -> {
		double value = current;
		for (double v : values) {
			value *= (1 + v);
		}
		return value;
	}),
	ADD_TOTAL_LATE(Phase.TOTAL, 200, (values, base, current) -> {
		double value = current;
		for (double v : values) {
			value = v;
		}
		return value;
	}),
	MIN_TOTAL(Phase.TOTAL, 300, (values, base, current) -> {
		double value = current;
		for (double v : values) {
			value = Math.max(v, value);
		}
		return value;
	}),
	MAX_TOTAL(Phase.TOTAL, 400, (values, base, current) -> {
		double value = current;
		for (double v : values) {
			value = Math.min(v, value);
		}
		return value;
	}),
	SET_TOTAL(Phase.TOTAL, 500, (values, base, current) -> {
		double value = current;
		for (double v : values) {
			value = v;
		}
		return value;
	});

	public static final InstanceDefiner DATA = InstanceDefiner.instanceDefiner()
		.add("value", SerializableDataTypes.DOUBLE)
		.add("modifier", Modifier.LIST_TYPE, null);

	private final Phase phase;
	private final int order;
	private final PropertyDispatch.TriFunction<List<Double>, Double, Double, Double> function;

	ModifierOperation(Phase phase, int order, PropertyDispatch.TriFunction<List<Double>, Double, Double, Double> function) {
		this.phase = phase;
		this.order = order;
		this.function = function;
	}

	@Override
	public Phase getPhase() {
		return phase;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public InstanceDefiner getData() {
		return DATA;
	}

	@Override
	public double apply(Entity entity, List<DeserializedFactoryJson> instances, double base, double current) {
		return function.apply(
			instances.stream()
				.map(instance -> {
					double value = instance.get("value");
					if (instance.isPresent("modifier")) {
						List<Modifier> modifiers = instance.get("modifier");
						value = ModifierUtil.applyModifiers(entity, modifiers, value);
					}
					return value;
				})
				.collect(Collectors.toList()),
			base, current);
	}

}