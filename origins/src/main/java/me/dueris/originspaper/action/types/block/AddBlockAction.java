package me.dueris.originspaper.action.types.block;

import io.github.dueris.calio.SerializableDataTypes;
import io.github.dueris.calio.parser.InstanceDefiner;
import me.dueris.originspaper.OriginsPaper;
import me.dueris.originspaper.action.ActionFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

public class AddBlockAction {

	public static @NotNull ActionFactory<Triple<Level, BlockPos, Direction>> getFactory() {
		return new ActionFactory<>(
			OriginsPaper.apoliIdentifier("add_block"),
			InstanceDefiner.instanceDefiner()
				.add("block", SerializableDataTypes.BLOCK_STATE),
			(data, block) -> {
				BlockState actualState = data.get("block");
				BlockPos pos = block.getMiddle().relative(block.getRight());
				block.getLeft().setBlockAndUpdate(pos, actualState);
			}
		);
	}
}