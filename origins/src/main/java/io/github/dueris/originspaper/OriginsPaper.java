package io.github.dueris.originspaper;

import com.dragoncommissions.mixbukkit.MixBukkit;
import io.github.dueris.calio.parser.CalioParser;
import io.github.dueris.calio.parser.JsonObjectRemapper;
import io.github.dueris.calio.registry.IRegistry;
import io.github.dueris.calio.registry.impl.CalioRegistry;
import io.github.dueris.originspaper.action.factory.BiEntityActions;
import io.github.dueris.originspaper.action.factory.BlockActions;
import io.github.dueris.originspaper.action.factory.EntityActions;
import io.github.dueris.originspaper.action.factory.ItemActions;
import io.github.dueris.originspaper.command.OriginCommand;
import io.github.dueris.originspaper.command.PowerCommand;
import io.github.dueris.originspaper.command.ResourceCommand;
import io.github.dueris.originspaper.condition.factory.*;
import io.github.dueris.originspaper.data.ApoliDataTypes;
import io.github.dueris.originspaper.data.OriginsDataTypes;
import io.github.dueris.originspaper.data.types.modifier.ModifierOperations;
import io.github.dueris.originspaper.mixin.OriginsMixins;
import io.github.dueris.originspaper.origin.Origin;
import io.github.dueris.originspaper.origin.OriginLayer;
import io.github.dueris.originspaper.plugin.OriginsPlugin;
import io.github.dueris.originspaper.power.type.FireProjectilePower;
import io.github.dueris.originspaper.power.factory.PowerType;
import io.github.dueris.originspaper.registry.Registries;
import io.github.dueris.originspaper.storage.OriginConfiguration;
import io.github.dueris.originspaper.util.LangFile;
import io.github.dueris.originspaper.util.Renderer;
import io.github.dueris.originspaper.util.Util;
import io.github.dueris.originspaper.util.WrappedBootstrapContext;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.entrypoint.classloader.PaperPluginClassLoader;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class OriginsPaper {
	public static final Logger LOGGER = LogManager.getLogger("OriginsPaper");
	public static String LANGUAGE = "en_us";
	public static boolean showCommandOutput = false;
	public static MinecraftServer server;
	public static PluginData pluginData;
	public static Path jarFile;
	private static WrappedBootstrapContext context;

	public static @NotNull ResourceLocation identifier(String path) {
		return ResourceLocation.fromNamespaceAndPath("originspaper", path);
	}

	public static @NotNull ResourceLocation originIdentifier(String path) {
		return ResourceLocation.fromNamespaceAndPath("origins", path);
	}

	public static @NotNull ResourceLocation apoliIdentifier(String path) {
		return ResourceLocation.fromNamespaceAndPath("apoli", path);
	}

	public static OriginLayer getLayer(ResourceLocation location) {
		return getRegistry().retrieve(Registries.LAYER).get(location);
	}

	public static PowerType getPower(ResourceLocation location) {
		return getRegistry().retrieve(Registries.POWER).get(location);
	}

	public static Origin getOrigin(ResourceLocation location) {
		return getRegistry().retrieve(Registries.ORIGIN).get(location);
	}

	public static IRegistry getRegistry() {
		return CalioRegistry.INSTANCE;
	}

	public static OriginsPlugin getPlugin() {
		return OriginsPlugin.plugin;
	}

	public static void init(@NotNull WrappedBootstrapContext context) throws Throwable {
		pluginData = new PluginData(YamlConfiguration.loadConfiguration(new StringReader(Util.readResource("/paper-plugin.yml"))));
		jarFile = context.context().getPluginSource();
		OriginsPaper.context = context;

		String runningVersion = ServerBuildInfo.buildInfo().minecraftVersionId();
		if (!pluginData.getSupportedVersions().contains(runningVersion)) {
			throw new IllegalStateException("This version of OriginsPaper does not support this version! Please use {}".replace("{}", pluginData.getSupportedVersions().toString()));
		}
		MixBukkit bukkit = new MixBukkit((PaperPluginClassLoader) OriginsPaper.class.getClassLoader());
		bukkit.onEnable(context.LOGGER, context.context().getPluginSource().toFile());

		OriginsMixins.init(bukkit);

		ApiCall.call(ApiCall.INIT, context);

		LifecycleEventManager<BootstrapContext> lifecycleManager = context.context().getLifecycleManager();
		lifecycleManager.registerEventHandler((LifecycleEvents.COMMANDS.newHandler(event -> {
			PowerCommand.register(event.registrar());
			OriginCommand.register(event.registrar());
			ResourceCommand.register(event.registrar());
		})).priority(4));

		io.github.dueris.calio.parser.JsonObjectRemapper remapper = new io.github.dueris.calio.parser.JsonObjectRemapper(
			List.of(new Tuple<>("origins", "apoli")),
			List.of(
				new Tuple<>("apoli:restrict_armor", "apoli:conditioned_restrict_armor"),
				new Tuple<>("apoli:has_tag", "apoli:has_command_tag"),
				new Tuple<>("apoli:custom_data", "apoli:nbt"),
				new Tuple<>("apoli:is_equippable", "apoli:equippable"),
				new Tuple<>("apoli:fireproof", "apoli:fire_resistant"),
				new Tuple<>("apoli:merge_nbt", "apoli:merge_custom_data"),
				new Tuple<>("apoli:revoke_power", "apoli:remove_power"),
				new Tuple<>("apoli:water_protection", "origins:water_protection"),
				new Tuple<>("apoli:enderian_pearl", "minecraft:ender_pearl")
			),
			List.of("power_type", "type", "entity_type")
		);
		JsonObjectRemapper.PRE_REMAP_HOOK.add(new Tuple<>(
			"apoli:enderian_pearl",
			(tuple) -> FireProjectilePower.IS_ENDERIAN_PEARL.add(tuple.getB())
		));
		CalioParser.REMAPPER.set(remapper);
		context.createRegistries(
			Registries.ORIGIN,
			Registries.LAYER,
			Registries.POWER,
			Registries.FLUID_CONDITION,
			Registries.ENTITY_CONDITION,
			Registries.BIOME_CONDITION,
			Registries.BIENTITY_CONDITION,
			Registries.BLOCK_CONDITION,
			Registries.ITEM_CONDITION,
			Registries.DAMAGE_CONDITION,
			Registries.ENTITY_ACTION,
			Registries.ITEM_ACTION,
			Registries.BLOCK_ACTION,
			Registries.BIENTITY_ACTION,
			Registries.LANG,
			Registries.CHOOSING_PAGE
		);

		OriginConfiguration.load();
		showCommandOutput = OriginConfiguration.getConfiguration().getBoolean("show-command-output", false);
		LANGUAGE = OriginConfiguration.getConfiguration().getString("language", LANGUAGE);
		LangFile.init();
		Renderer.init();

		OriginsDataTypes.init();
		ApoliDataTypes.init();

		ModifierOperations.registerAll();
		PowerType.registerAll();
		EntityConditions.register();
		BiEntityConditions.register();
		ItemConditions.register();
		BlockConditions.register();
		DamageConditions.register();
		FluidConditions.register();
		BiomeConditions.register();
		EntityActions.register();
		ItemActions.register();
		BlockActions.register();
		BiEntityActions.register();

		ApiCall.call(ApiCall.PRE_PARSE, context);
	}

	public enum ApiCall {
		INIT, PRE_PARSE;
		private static final List<Tuple<ApiCall, Consumer<WrappedBootstrapContext>>> REGISTERED = new CopyOnWriteArrayList<>();

		public static void registerCall(ApiCall call, Consumer<WrappedBootstrapContext> consumer) {
			REGISTERED.add(new Tuple<>(
				call, consumer
			));
		}

		private static void call(ApiCall call, WrappedBootstrapContext context) {
			for (Tuple<ApiCall, Consumer<WrappedBootstrapContext>> apiCall : REGISTERED) {
				if (apiCall.getA().equals(call)) {
					apiCall.getB().accept(context);
					REGISTERED.remove(apiCall);
				}
			}
		}
	}

	/**
	 * A parser for the plugin data found in `paper-plugin.yml`
	 * This is needed because that information is not directly stored
	 * in the plugin source, and is provided on build.
	 */
	public record PluginData(YamlConfiguration configuration) {
		public @NotNull List<String> getSupportedVersions() {
			return configuration.getStringList("supported");
		}

		public String getRecommendedVersion() {
			return configuration.getString("minecraft");
		}

		public String getPluginVersion() {
			return configuration.getString("plugin");
		}

		public String getFullVersion() {
			return configuration.getString("version");
		}

		public String getApoliVersion() {
			return configuration.getString("apoli");
		}
	}
}
