package io.github.dueris.originspaper.registry.nms;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dueris.originspaper.OriginsPaper;
import io.github.dueris.originspaper.origin.Origin;
import io.github.dueris.originspaper.registry.Registries;
import io.github.dueris.originspaper.storage.OriginComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OriginLootCondition implements LootItemCondition {
	public static final Codec<OriginLootCondition> CODEC = RecordCodecBuilder.create((instance) -> instance.group(ResourceLocation.CODEC.fieldOf("origin").forGetter(OriginLootCondition::getOriginId), ResourceLocation.CODEC.optionalFieldOf("source").forGetter(OriginLootCondition::getOriginSourceId)).apply(instance, OriginLootCondition::new));
	public static final LootItemConditionType TYPE;

	static {
		TYPE = new LootItemConditionType(MapCodec.assumeMapUnsafe(CODEC));
	}

	private final ResourceLocation originId;
	private final ResourceLocation originSourceId;

	private OriginLootCondition(ResourceLocation originId, Optional<ResourceLocation> originSourceId) {
		this.originId = originId;
		this.originSourceId = originSourceId.orElseGet(null);
	}

	public boolean test(LootContext context) {
		Entity entity = context.getParam(LootContextParams.THIS_ENTITY);
		CraftEntity var4 = entity.getBukkitEntity();
		if (var4 instanceof Player player) {
			Origin origin = OriginsPaper.getRegistry().retrieve(Registries.ORIGIN).get(this.originId);
			return OriginComponent.hasOrigin(player, origin.getTag());
		} else {
			return false;
		}
	}

	public @NotNull LootItemConditionType getType() {
		return OriginLootCondition.TYPE;
	}

	public ResourceLocation getOriginId() {
		return this.originId;
	}

	public Optional<ResourceLocation> getOriginSourceId() {
		return Optional.of(this.originSourceId);
	}
}