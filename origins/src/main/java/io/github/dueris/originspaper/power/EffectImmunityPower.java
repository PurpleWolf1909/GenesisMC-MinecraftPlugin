package io.github.dueris.originspaper.power;

import io.github.dueris.calio.SerializableDataTypes;
import io.github.dueris.calio.parser.SerializableData;
import io.github.dueris.originspaper.OriginsPaper;
import io.github.dueris.originspaper.condition.ConditionFactory;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EffectImmunityPower extends PowerType {
	private final boolean inverted;
	private final ArrayList<Holder<MobEffect>> effects;

	public EffectImmunityPower(@NotNull ResourceLocation key, @NotNull ResourceLocation type, Component name, Component description, boolean hidden, ConditionFactory<Entity> condition, int loadingPriority,
							   Holder<MobEffect> mobEffect, List<Holder<MobEffect>> mobEffects, boolean inverted) {
		super(key, type, name, description, hidden, condition, loadingPriority);
		this.inverted = inverted;
		this.effects = new ArrayList<>();
		if (mobEffect != null) {
			effects.add(mobEffect);
		}

		if (mobEffects != null && !mobEffects.isEmpty()) {
			effects.addAll(mobEffects);
		}
	}

	public static SerializableData buildFactory() {
		return PowerType.buildFactory().typedRegistry(OriginsPaper.apoliIdentifier("effect_immunity"))
			.add("effect", SerializableDataTypes.STATUS_EFFECT_ENTRY, null)
			.add("effects", SerializableDataTypes.list(SerializableDataTypes.STATUS_EFFECT_ENTRY), null)
			.add("inverted", SerializableDataTypes.BOOLEAN, false);
	}

	public boolean doesApply(MobEffectInstance instance) {
		return doesApply(instance.getEffect());
	}

	public boolean doesApply(Holder<MobEffect> effect) {
		return inverted ^ effects.contains(effect);
	}

	public ArrayList<Holder<MobEffect>> getEffects() {
		return effects;
	}

	public boolean isInverted() {
		return inverted;
	}
}