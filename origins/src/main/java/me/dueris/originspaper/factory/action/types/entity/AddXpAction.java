package me.dueris.originspaper.factory.action.types.entity;

import io.github.dueris.calio.SerializableDataTypes;
import io.github.dueris.calio.parser.InstanceDefiner;
import me.dueris.originspaper.OriginsPaper;
import me.dueris.originspaper.factory.action.ActionFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class AddXpAction {

	public static @NotNull ActionFactory<Entity> getFactory() {
		return new ActionFactory<>(OriginsPaper.apoliIdentifier("add_xp"),
			InstanceDefiner.instanceDefiner()
				.add("points", SerializableDataTypes.INT, 0)
				.add("levels", SerializableDataTypes.INT, 0),
			(data, entity) -> {
				if (entity instanceof Player) {
					int points = data.getInt("points");
					int levels = data.getInt("levels");
					if (points > 0) {
						((Player) entity).giveExperiencePoints(points);
					}
					((Player) entity).giveExperienceLevels(levels);
				}
			}
		);
	}
}
