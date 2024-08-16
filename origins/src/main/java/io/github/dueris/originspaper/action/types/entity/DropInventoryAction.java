package io.github.dueris.originspaper.action.types.entity;

import io.github.dueris.calio.SerializableDataTypes;
import io.github.dueris.calio.parser.SerializableData;
import io.github.dueris.originspaper.OriginsPaper;
import io.github.dueris.originspaper.action.ActionFactory;
import io.github.dueris.originspaper.data.ApoliDataTypes;
import io.github.dueris.originspaper.data.types.InventoryType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import static io.github.dueris.originspaper.util.Util.dropInventory;

public class DropInventoryAction {

	public static void action(@NotNull SerializableData.Instance data, Entity entity) {

		InventoryType inventoryType = data.get("inventory_type");

		switch (inventoryType) {
			case INVENTORY -> dropInventory(data, entity);
		}

	}

	public static @NotNull ActionFactory<Entity> getFactory() {
		return new ActionFactory<>(OriginsPaper.apoliIdentifier("drop_inventory"),
				SerializableData.serializableData()
						.add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
						.add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
						.add("item_action", ApoliDataTypes.ITEM_ACTION, null)
						.add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
						.add("slots", SerializableDataTypes.list(ApoliDataTypes.ITEM_SLOT), null)
						.add("slot", ApoliDataTypes.ITEM_SLOT, null)
						.add("throw_randomly", SerializableDataTypes.BOOLEAN, false)
						.add("retain_ownership", SerializableDataTypes.BOOLEAN, true)
						.add("amount", SerializableDataTypes.INT, 0),
				DropInventoryAction::action
		);
	}
}
