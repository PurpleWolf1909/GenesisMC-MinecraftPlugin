package me.dueris.genesismc.factory.powers.apoli;

import me.dueris.genesismc.factory.conditions.ConditionExecutor;
import me.dueris.genesismc.factory.data.types.Modifier;
import me.dueris.genesismc.factory.powers.CraftPower;
import me.dueris.genesismc.registry.registries.Power;
import me.dueris.genesismc.util.Utils;
import me.dueris.genesismc.util.entity.OriginPlayerAccessor;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.ArrayList;
import java.util.function.BinaryOperator;

public class ModifyHealingPower extends CraftPower implements Listener {

	@EventHandler
	public void runD(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player p) {
			if (!modify_healing.contains(p)) return;
			for (Power power : OriginPlayerAccessor.getPowers(p, getType())) {
				for (Modifier modifier : power.getModifiers()) {
					Float value = modifier.value();
					String operation = modifier.operation();
					BinaryOperator mathOperator = Utils.getOperationMappingsFloat().get(operation);
					if (mathOperator != null) {
						float result = (float) mathOperator.apply(e.getAmount(), value);
						if (ConditionExecutor.testEntity(power.getJsonObject("condition"), (CraftEntity) p)) {
							setActive(p, power.getTag(), true);
							e.setAmount(result);
						} else {
							setActive(p, power.getTag(), false);
						}
					}
				}
			}
		}
	}

	@Override
	public String getType() {
		return "apoli:modify_healing";
	}

	@Override
	public ArrayList<Player> getPlayersWithPower() {
		return modify_healing;
	}
}