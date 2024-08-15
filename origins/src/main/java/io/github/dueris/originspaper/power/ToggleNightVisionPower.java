package io.github.dueris.originspaper.power;

import io.github.dueris.calio.SerializableDataTypes;
import io.github.dueris.calio.parser.SerializableData;
import io.github.dueris.originspaper.OriginsPaper;
import io.github.dueris.originspaper.condition.ConditionFactory;
import io.github.dueris.originspaper.data.ApoliDataTypes;
import io.github.dueris.originspaper.data.types.Keybind;
import io.github.dueris.originspaper.event.KeybindTriggerEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ToggleNightVisionPower extends NightVisionPower {
	private final Keybind keybind;

	private final List<Player> TICKED = new ArrayList<>();
	private boolean toggled;

	public ToggleNightVisionPower(@NotNull ResourceLocation key, @NotNull ResourceLocation type, Component name, Component description, boolean hidden, ConditionFactory<Entity> condition, int loadingPriority,
								  float strength, boolean activeByDefault, Keybind keybind) {
		super(key, type, name, description, hidden, condition, loadingPriority, strength);
		this.toggled = activeByDefault;
		this.keybind = keybind;
	}

	@EventHandler
	public void onKey(@NotNull KeybindTriggerEvent e) {
		Player player = ((CraftPlayer) e.getPlayer()).getHandle();
		if (e.getKey().equalsIgnoreCase(keybind.key()) && getPlayers().contains(player) && !TICKED.contains(player)) {
			this.toggled = !this.toggled;
			TICKED.add(player);
			new BukkitRunnable() {
				@Override
				public void run() {
					TICKED.remove(player);
				}
			}.runTaskLater(OriginsPaper.getPlugin(), 1);
		}
	}

	@Override
	public boolean isActive(@NotNull Entity player) {
		return this.toggled && super.isActive(player);
	}

	public static SerializableData buildFactory() {
		return NightVisionPower.buildFactory().typedRegistry(OriginsPaper.apoliIdentifier("toggle_night_vision"))
			.add("active_by_default", SerializableDataTypes.BOOLEAN, false)
			.add("key", ApoliDataTypes.KEYBIND, Keybind.DEFAULT_KEYBIND);
	}
}