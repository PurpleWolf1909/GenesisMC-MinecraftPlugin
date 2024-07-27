package me.dueris.originspaper.factory.conditions.meta;

import io.github.dueris.calio.SerializableDataTypes;
import io.github.dueris.calio.parser.InstanceDefiner;
import me.dueris.originspaper.OriginsPaper;
import me.dueris.originspaper.factory.conditions.ConditionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ChanceCondition {
	private static final Random random = new Random();

	public static <T> @NotNull ConditionFactory<T> getFactory() {
		return new ConditionFactory<>(
			OriginsPaper.apoliIdentifier("chance"),
			InstanceDefiner.instanceDefiner()
				.required("chance", SerializableDataTypes.FLOAT),
			(data, t) -> {
				return random.nextFloat() < data.getFloat("chance");
			}
		);
	}
}