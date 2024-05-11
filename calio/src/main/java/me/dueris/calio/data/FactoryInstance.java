package me.dueris.calio.data;

import me.dueris.calio.data.factory.FactoryBuilder;
import me.dueris.calio.registry.Registrable;
import me.dueris.calio.registry.Registrar;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.util.List;

public interface FactoryInstance extends Registrable {

	/**
	 * Returns the current allowed instances for the Registerable
	 */
	List<FactoryDataDefiner> getValidObjectFactory();

	/**
	 * Creates an instance of the FactoryProvider class using the provided raw file, registry, and namespaced tag associated with the instance being created.
	 *
	 * @param obj           the FactoryProvider object to create an instance of
	 * @param rawFile       the raw file to use for creating the instance
	 * @param registry      the registry to use for creating the instance
	 * @param namespacedTag the namespaced tag to use for creating the instance
	 */
	void createInstance(FactoryBuilder obj, File rawFile, Registrar<? extends Registrable> registry, NamespacedKey namespacedTag);
}