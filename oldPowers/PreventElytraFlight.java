package me.dueris.genesismc.factory.powers.apoli;

import me.dueris.genesismc.factory.conditions.ConditionExecutor;
import me.dueris.genesismc.factory.powers.CraftPower;
import me.dueris.genesismc.registry.registries.Power;
import me.dueris.genesismc.util.entity.OriginPlayerAccessor;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

import java.util.ArrayList;

import static me.dueris.genesismc.factory.powers.apoli.superclass.PreventSuperClass.prevent_elytra_flight;

public class PreventElytraFlight extends CraftPower implements Listener {

	@EventHandler
	public void run(EntityToggleGlideEvent e) {
		if (e.getEntity() instanceof Player p) {
			if (prevent_elytra_flight.contains(p)) {
				for (Power power : OriginPlayerAccessor.getPowers(p, getType())) {
					if (ConditionExecutor.testEntity(power.getJsonObject("condition"), (CraftEntity) p)) {
						e.setCancelled(true);
						setActive(p, power.getTag(), true);
					} else {
						setActive(p, power.getTag(), false);
					}
				}
			}
		}
	}

	@Override
	public String getType() {
		return "apoli:prevent_elytra_flight";
	}

	@Override
	public ArrayList<Player> getPlayersWithPower() {
		return prevent_elytra_flight;
	}
}