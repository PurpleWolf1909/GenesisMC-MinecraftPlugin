package me.dueris.originspaper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import it.unimi.dsi.fastutil.Pair;
import me.dueris.calio.data.JsonObjectRemapper;
import me.dueris.originspaper.content.NMSBootstrap;
import me.dueris.originspaper.registry.Registries;
import me.dueris.originspaper.util.Util;
import me.dueris.originspaper.util.WrappedBootstrapContext;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// TODO: MachineMaker PluginDatapacks
public class Bootstrap implements PluginBootstrap {
	public static ArrayList<String> oldDV = new ArrayList<>();
	public static ArrayList<Consumer<WrappedBootstrapContext>> apiCalls = new ArrayList<>();
	public static AtomicBoolean BOOTSTRAPPED = new AtomicBoolean(false);

	//TODO: Unsure of what this does so i'm leaving it as it
	static {
		oldDV.add("OriginsGenesis");
		oldDV.add("Origins-Genesis");
		oldDV.add("Origins-GenesisMC");
		oldDV.add("Origins-GenesisMC[0_2_2]");
		oldDV.add("Origins-GenesisMC[0_2_4]");
		oldDV.add("Origins-GenesisMC[0_2_6]");
	}

	public static void deleteDirectory(Path directory, boolean ignoreErrors) throws IOException {
		if (Files.exists(directory)) {
			Files.walk(directory)
				.sorted(Comparator.reverseOrder()) // Sort in reverse order for correct deletion
				.forEach(path -> {
					try {
						Files.deleteIfExists(path);
						Files.delete(path);
					} catch (IOException e) {
						if (!ignoreErrors) {
							System.err.println("Error deleting: " + path + e);
						}
					}
				});
		}
	}

	public static void copyOriginDatapack(Path datapackPath, WrappedBootstrapContext context) {
		for (String string : oldDV) {
			if (Files.exists(datapackPath)) {
				String path = Path.of(datapackPath + File.separator + string).toAbsolutePath().toString();
				try {
					deleteDirectory(Path.of(path), true);
				} catch (IOException e) {
					// Something happened when deleting, ignore.
				}
			} else {
				File file = new File(datapackPath.toAbsolutePath().toString());
				file.mkdirs();
				copyOriginDatapack(datapackPath, context);
			}
		}
		try {
			CodeSource src = Util.class.getProtectionDomain().getCodeSource();
			URL jar = src.getLocation();
			ZipInputStream zip = new ZipInputStream(jar.openStream());
			while (true) {
				ZipEntry entry = zip.getNextEntry();
				if (entry == null)
					break;
				String name = entry.getName();

				if (!name.startsWith("minecraft/")) continue;
				if (FilenameUtils.getExtension(name).equals("zip")) continue;
				if (name.equals("minecraft/")) continue;

				name = name.substring(9);
				File file = new File(datapackPath.toAbsolutePath().toString().replace(".\\", "") + File.separator + name);
				if (!file.getName().contains(".")) {
					Files.createDirectory(Path.of(file.getAbsolutePath()));
					continue;
				}

				// Ensure parent directory exists
				File parentDir = file.getParentFile();
				if (!parentDir.exists()) {
					parentDir.mkdirs();
				}

				// Copy PNG files
				if (FilenameUtils.getExtension(name).equalsIgnoreCase("png")) {
					try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = zip.read(buffer)) > 0) {
							bos.write(buffer, 0, len);
						}
					}
				} else { // Copy non-PNG files as text
					Files.writeString(Path.of(file.getAbsolutePath()), new String(zip.readAllBytes()));
				}
			}
			zip.close();
		} catch (Exception e) {
			// e.printStackTrace(); // Print stack trace for debugging // I changed my mind
		}

	}

	public static String levelNameProp() {
		Path propPath = Paths.get("server.properties");
		if (propPath.toFile().exists()) {
			Properties properties = new Properties();
			try (FileInputStream input = new FileInputStream(propPath.toFile())) {
				properties.load(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(properties.keySet());
			return properties.getProperty("level-name", "world");
		} else {
			return "world";
		}
	}

	@Override
	public void bootstrap(@Nullable BootstrapContext bootContext) {
		WrappedBootstrapContext context = new WrappedBootstrapContext(bootContext);
		if (bootContext != null) {
			NMSBootstrap.bootstrap(context);
			for (Consumer<WrappedBootstrapContext> apiCall : apiCalls) {
				apiCall.accept(context);
			}
			File packDir = new File(this.parseDatapackPath());
			try {
				copyOriginDatapack(packDir.toPath(), context);
			} catch (Exception e) {
				// ignore
			} finally {
				context.initRegistries(packDir.toPath());
			}
		}

		JsonObjectRemapper.typeMappings.add(new Pair<String, String>() {
			@Override
			public String left() {
				return "origins";
			}

			@Override
			public String right() {
				return "apoli";
			}
		});
		// Our version of restricted_armor allows handling of both.
		JsonObjectRemapper.typeAlias.put("apoli:conditioned_restrict_armor", "apoli:restrict_armor");
		JsonObjectRemapper.typeAlias.put("apugli:edible_item", "apoli:edible_item");
		JsonObjectRemapper.typeAlias.put("apoli:modify_attribute", "apoli:conditioned_attribute");
		JsonObjectRemapper.typeAlias.put("apoli:add_to_set", "apoli:add_to_entity_set");
		JsonObjectRemapper.typeAlias.put("apoli:remove_from_set", "apoli:remove_from_entity_set");
		JsonObjectRemapper.typeAlias.put("apoli:action_on_set", "apoli:action_on_entity_set");
		JsonObjectRemapper.typeAlias.put("apoli:in_set", "apoli:in_entity_set");
		JsonObjectRemapper.typeAlias.put("apoli:set_size", "apoli:entity_set_size");

		// Create new registry instances
		context.createRegistries(
			Registries.ORIGIN,
			Registries.LAYER,
			Registries.CRAFT_POWER,
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
			Registries.TEXTURE_LOCATION,
			Registries.LANG,
			Registries.PACK_SOURCE,
			Registries.CHOOSING_PAGE
		);
		BOOTSTRAPPED.set(true);
	}

	public String parseDatapackPath() {
		try {
			org.bukkit.configuration.file.YamlConfiguration bukkitConfiguration = YamlConfiguration.loadConfiguration(Paths.get("bukkit.yml").toFile());
			File container;
			container = new File(bukkitConfiguration.getString("settings.world-container", "."));
			String s = Optional.ofNullable(
				levelNameProp()
			).orElse("world");
			Path datapackFolder = Paths.get(container.getAbsolutePath() + File.separator + s + File.separator + "datapacks");
			return datapackFolder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}