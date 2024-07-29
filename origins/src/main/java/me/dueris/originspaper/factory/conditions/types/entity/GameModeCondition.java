package me.dueris.originspaper.factory.conditions.types.entity;

import io.github.dueris.calio.parser.InstanceDefiner;
import io.github.dueris.calio.parser.reader.DeserializedFactoryJson;
import me.dueris.originspaper.OriginsPaper;
import me.dueris.originspaper.factory.conditions.ConditionFactory;
import me.dueris.originspaper.factory.data.ApoliDataTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class GameModeCondition {

	public static boolean condition(DeserializedFactoryJson data, Entity entity) {

		if (!(entity instanceof Player playerEntity)) {
			return false;
		}

		GameType specifiedGameMode = data.get("gamemode");
		if (playerEntity instanceof ServerPlayer serverPlayerEntity) {

			ServerPlayerGameMode interactionManager = serverPlayerEntity.gameMode;
			return interactionManager.getGameModeForPlayer() == specifiedGameMode;

		}

		return false;

	}

	public static ConditionFactory<Entity> getFactory() {
		return new ConditionFactory<>(
			OriginsPaper.apoliIdentifier("gamemode"),
			InstanceDefiner.instanceDefiner()
				.add("gamemode", ApoliDataTypes.GAME_MODE),
			GameModeCondition::condition
		);
	}
}
