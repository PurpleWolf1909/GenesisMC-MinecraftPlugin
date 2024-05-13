package me.dueris.genesismc.factory.powers.apoli;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import me.dueris.calio.data.factory.FactoryJsonObject;
import me.dueris.genesismc.factory.conditions.ConditionExecutor;
import me.dueris.genesismc.factory.powers.CraftPower;
import me.dueris.genesismc.registry.registries.Power;
import me.dueris.genesismc.util.entity.OriginPlayerAccessor;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;

public class RestrictArmor extends CraftPower implements Listener {

	@EventHandler
	public void tick(PlayerArmorChangeEvent e) {
		Player p = e.getPlayer();
		if (getPlayersWithPower().contains(p)) {
			for (Power power : OriginPlayerAccessor.getPowers(p, getType())) {
				if (power == null) continue;
				if (ConditionExecutor.testEntity(power.getJsonObject("condition"), (CraftEntity) p)) {
					runPower(p, power);
				}
			}
		}
	}


	@Override
	public void run(Player p, Power power) {
		long interval = power.getNumberOrDefault("interval", 1L).getLong();
		if (interval == 0) interval = 1L;
		if (Bukkit.getServer().getCurrentTick() % interval == 0) {
			if (ConditionExecutor.testEntity(power.getJsonObject("condition"), (CraftEntity) p)) {
				runPower(p, power);
			} else {
				setActive(p, power.getTag(), false);
			}
		}
	}

	public void runPower(Player p, Power power) {
		setActive(p, power.getTag(), true);
		boolean passFeet = false;
		boolean passLegs = false;
		boolean passChest = false;
		boolean passHead = false;
		FactoryJsonObject headObj = power.getJsonObject("head");
		FactoryJsonObject chestObj = power.getJsonObject("chest");
		FactoryJsonObject legsObj = power.getJsonObject("legs");
		FactoryJsonObject feetObj = power.getJsonObject("feet");

		if (!headObj.isEmpty())
			passHead = ConditionExecutor.testItem(headObj, p.getInventory().getItem(EquipmentSlot.HEAD));
		if (!chestObj.isEmpty())
			passChest = ConditionExecutor.testItem(chestObj, p.getInventory().getItem(EquipmentSlot.CHEST));
		if (!legsObj.isEmpty())
			passLegs = ConditionExecutor.testItem(legsObj, p.getInventory().getItem(EquipmentSlot.LEGS));
		if (!feetObj.isEmpty())
			passFeet = ConditionExecutor.testItem(feetObj, p.getInventory().getItem(EquipmentSlot.FEET));

		if (passFeet)
			OriginPlayerAccessor.moveEquipmentInventory(p, EquipmentSlot.FEET);
		if (passChest)
			OriginPlayerAccessor.moveEquipmentInventory(p, EquipmentSlot.CHEST);
		if (passHead)
			OriginPlayerAccessor.moveEquipmentInventory(p, EquipmentSlot.HEAD);
		if (passLegs)
			OriginPlayerAccessor.moveEquipmentInventory(p, EquipmentSlot.LEGS);
	}

	@Override
	public String getType() {
		return "apoli:restrict_armor";
	}

	@Override
	public ArrayList<Player> getPlayersWithPower() {
		return restrict_armor;
	}
}