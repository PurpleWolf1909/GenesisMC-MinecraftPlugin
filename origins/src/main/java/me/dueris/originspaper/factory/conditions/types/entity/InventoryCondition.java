package me.dueris.originspaper.factory.conditions.types.entity;

import io.github.dueris.calio.SerializableDataTypes;
import io.github.dueris.calio.parser.InstanceDefiner;
import io.github.dueris.calio.parser.reader.DeserializedFactoryJson;
import me.dueris.originspaper.OriginsPaper;
import me.dueris.originspaper.factory.conditions.ConditionFactory;
import me.dueris.originspaper.factory.data.ApoliDataTypes;
import me.dueris.originspaper.factory.data.types.Comparison;
import me.dueris.originspaper.factory.data.types.InventoryType;
import me.dueris.originspaper.util.Util;
import net.minecraft.world.entity.Entity;

import java.util.EnumSet;
import java.util.Set;

public class InventoryCondition {

	public static boolean condition(DeserializedFactoryJson data, Entity entity) {

		Set<InventoryType> inventoryTypes = data.get("inventory_types");
		Util.ProcessMode processMode = data.get("process_mode");
		Comparison comparison = data.get("comparison");

		int compareTo = data.get("compare_to");
		int matches = 0;

		if (inventoryTypes.contains(InventoryType.INVENTORY)) {
			matches += Util.checkInventory(data, entity, processMode.getProcessor());
		}

		return comparison.compare(matches, compareTo);

	}

	public static ConditionFactory<Entity> getFactory() {
		return new ConditionFactory<>(
			OriginsPaper.apoliIdentifier("inventory"),
			InstanceDefiner.instanceDefiner()
				.add("inventory_types", SerializableDataTypes.enumSet(InventoryType.class, ApoliDataTypes.INVENTORY_TYPE), EnumSet.of(InventoryType.INVENTORY))
				.add("process_mode", ApoliDataTypes.PROCESS_MODE, Util.ProcessMode.ITEMS)
				.add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
				.add("slots", SerializableDataTypes.list(ApoliDataTypes.ITEM_SLOT), null)
				.add("slot", ApoliDataTypes.ITEM_SLOT, null)
				.add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
				.add("compare_to", SerializableDataTypes.INT, 0),
			InventoryCondition::condition
		);
	}
}
