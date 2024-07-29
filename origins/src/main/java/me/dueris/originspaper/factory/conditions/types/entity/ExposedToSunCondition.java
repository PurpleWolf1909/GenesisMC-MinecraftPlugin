package me.dueris.originspaper.factory.conditions.types.entity;

import io.github.dueris.calio.parser.InstanceDefiner;
import io.github.dueris.calio.parser.reader.DeserializedFactoryJson;
import me.dueris.originspaper.OriginsPaper;
import me.dueris.originspaper.factory.conditions.ConditionFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ExposedToSunCondition {

	public static boolean condition(DeserializedFactoryJson data, @NotNull Entity entity) {

		Level world = entity.level();
		if (!world.isDay() || entity.getBukkitEntity().isInRain()) {
			return false;
		}

		BlockPos blockPos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
		float brightness = world.getLightLevelDependentMagicValue(blockPos);

		return brightness > 0.5
			&& world.canSeeSky(blockPos);

	}

	public static @NotNull ConditionFactory<Entity> getFactory() {
		return new ConditionFactory<>(
			OriginsPaper.apoliIdentifier("exposed_to_sun"),
			InstanceDefiner.instanceDefiner(),
			ExposedToSunCondition::condition
		);
	}
}
