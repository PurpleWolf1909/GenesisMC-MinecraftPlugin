package io.github.dueris.calio;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import io.github.dueris.calio.data.exceptions.DataException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main class for the Calio Parser, used to start the parser with specified args
 *
 * @param threaded
 * @param threadCount
 */
public record CraftCalio(boolean threaded, int threadCount) {
	public static final Logger LOGGER = LogManager.getLogger("CraftCalio");
	@ApiStatus.Internal
	public static final Map<TagKey<?>, Collection<Holder<?>>> REGISTRY_TAGS = new ConcurrentHashMap<>();
	@ApiStatus.Internal
	public static final Map<Unit, RegistryAccess> DYNAMIC_REGISTRIES = new ConcurrentHashMap<>();
	@ApiStatus.Internal
	public static final Map<Unit, ReloadableServerResources> DATA_PACK_CONTENTS = new ConcurrentHashMap<>();
	private static RegistryAccess registryAccess;

	public static void setRegistryAccess(RegistryAccess access) {
		registryAccess = access;
	}

	public static RegistryAccess registryAccess() {
		return registryAccess;
	}

	public static @NotNull DataException createMissingRequiredFieldException(String name) {
		return new DataException(DataException.Phase.READING, name, "Field is required, but is missing!");
	}

	public static <R> @NotNull DataResult<R> createMissingRequiredFieldError(String name) {
		return DataResult.error(() -> "Required field \"" + name + "\" is missing!");
	}

	public static @NotNull ImmutableMap<TagKey<?>, Collection<Holder<?>>> getRegistryTags() {
		return ImmutableMap.copyOf(REGISTRY_TAGS);
	}

	public static Optional<RegistryAccess> getDynamicRegistries() {
		return Optional.ofNullable(DYNAMIC_REGISTRIES.get(Unit.INSTANCE));
	}

	public static Optional<ReloadableServerResources> getDataPackContents() {
		return Optional.ofNullable(DATA_PACK_CONTENTS.get(Unit.INSTANCE));
	}

	public static void initialize() {
		CriteriaTriggers.register(CodeTriggerCriterion.ID.toString(), CodeTriggerCriterion.INSTANCE);
	}

	@Deprecated(forRemoval = true)
	public static <T> boolean areTagsEqual(ResourceKey<? extends Registry<T>> registryKey, TagKey<T> tag1, TagKey<T> tag2) {
		return areTagsEqual(tag1, tag2);
	}

	/**
	 * 	<b>Use {@link TagKey#equals(Object)} (or {@link Objects#equals(Object, Object)} if nullability is of concern) instead.</b>
	 */
	@Deprecated(forRemoval = true)
	public static <T> boolean areTagsEqual(TagKey<T> tag1, TagKey<T> tag2) {
		return tag1 == tag2
			|| tag1 != null
			&& tag2 != null
			&& tag1.registry().equals(tag2.registry())
			&& tag1.location().equals(tag2.location());
	}

	@Override
	public @NotNull String toString() {
		return "CraftCalio[" +
			"async=" + threaded + ", " +
			"threadCount=" + threadCount + ']';
	}
}
