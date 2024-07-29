package me.dueris.originspaper.factory.conditions.types.entity;

import io.github.dueris.calio.SerializableDataTypes;
import io.github.dueris.calio.parser.InstanceDefiner;
import me.dueris.originspaper.OriginsPaper;
import me.dueris.originspaper.factory.conditions.ConditionFactory;
import me.dueris.originspaper.util.Util;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class SubmergedInCondition {

	public static @NotNull ConditionFactory<Entity> getFactory() {
		return new ConditionFactory<>(
			OriginsPaper.apoliIdentifier("submerged_in"),
			InstanceDefiner.instanceDefiner()
				.add("fluid", SerializableDataTypes.FLUID_TAG),
			(data, entity) -> {
				return Util.apoli$isSubmergedInLoosely(entity, data.get("fluid"));
			}
		);
	}
}
