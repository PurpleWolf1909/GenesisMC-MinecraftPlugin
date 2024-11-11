package io.github.dueris.originspaper.condition.type.item;

import io.github.dueris.originspaper.condition.ConditionConfiguration;
import io.github.dueris.originspaper.condition.type.ItemConditionType;
import io.github.dueris.originspaper.condition.type.ItemConditionTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EnchantableItemConditionType extends ItemConditionType {

	@Override
	public boolean test(Level world, ItemStack stack) {
		return stack.isEnchantable();
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return ItemConditionTypes.ENCHANTABLE;
	}

}
