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
import org.bukkit.event.entity.EntityExhaustionEvent;

import java.util.ArrayList;
import java.util.function.BinaryOperator;

public class ModifyExhaustionPower extends CraftPower implements Listener {

	@EventHandler
	public void run(EntityExhaustionEvent e) {
		Player p = (Player) e.getEntity();
		if (modify_exhaustion.contains(p)) {
			for (Power power : OriginPlayerAccessor.getPowers(p, getType())) {
				if (ConditionExecutor.testEntity(power.getJsonObject("condition"), (CraftEntity) p)) {
					for (Modifier modifier : power.getModifiers()) {
						Float value = modifier.value();
						String operation = modifier.operation();
						BinaryOperator mathOperator = Utils.getOperationMappingsFloat().get(operation);
						if (mathOperator != null) {
							float result = (float) mathOperator.apply(e.getExhaustion(), value);
							e.setExhaustion(result);

							setActive(p, power.getTag(), true);
						}
					}
				} else {
					setActive(p, power.getTag(), false);
				}
			}
		}
	}

	@Override
	public String getType() {
		return "apoli:modify_exhaustion";
	}

	@Override
	public ArrayList<Player> getPlayersWithPower() {
		return modify_exhaustion;
	}
}