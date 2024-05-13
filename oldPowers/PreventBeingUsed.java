package me.dueris.genesismc.factory.powers.apoli;

import me.dueris.genesismc.factory.conditions.ConditionExecutor;
import me.dueris.genesismc.factory.powers.CraftPower;
import me.dueris.genesismc.registry.registries.Power;
import me.dueris.genesismc.util.entity.OriginPlayerAccessor;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

import static me.dueris.genesismc.factory.powers.apoli.superclass.PreventSuperClass.prevent_being_used;

public class PreventBeingUsed extends CraftPower implements Listener {

	@EventHandler
	public void run(PlayerInteractEvent e) {
		if (prevent_being_used.contains(e.getPlayer())) {
			Player p = e.getPlayer();
			for (Power power : OriginPlayerAccessor.getPowers(p, getType())) {
				if (ConditionExecutor.testEntity(power.getJsonObject("condition"), (CraftEntity) p) && ConditionExecutor.testItem(power.getJsonObject("item_condition"), e.getItem())) {
					setActive(p, power.getTag(), true);
					e.setCancelled(true);
				} else {
					setActive(p, power.getTag(), false);
				}
			}
		}
	}

	@Override
	public String getType() {
		return "apoli:prevent_being_used";
	}

	@Override
	public ArrayList<Player> getPlayersWithPower() {
		return prevent_being_used;
	}
}