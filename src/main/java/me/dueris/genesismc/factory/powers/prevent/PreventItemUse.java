package me.dueris.genesismc.factory.powers.prevent;

import me.dueris.genesismc.entity.OriginPlayerUtils;
import me.dueris.genesismc.factory.conditions.ConditionExecutor;
import me.dueris.genesismc.factory.conditions.item.ItemCondition;
import me.dueris.genesismc.factory.powers.CraftPower;
import me.dueris.genesismc.utils.OriginContainer;
import me.dueris.genesismc.utils.PowerContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;

import static me.dueris.genesismc.factory.powers.prevent.PreventSuperClass.prevent_item_use;

public class PreventItemUse extends CraftPower implements Listener {

    @Override
    public void setActive(Player p, String tag, Boolean bool) {
        if(powers_active.containsKey(p)){
            if(powers_active.get(p).containsKey(tag)){
                powers_active.get(p).replace(tag, bool);
            }else{
                powers_active.get(p).put(tag, bool);
            }
        }else{
            powers_active.put(p, new HashMap());
            setActive(p, tag, bool);
        }
    }

    @Override
    public void run(Player p) {

    }

    @EventHandler
    public void runD(PlayerInteractEvent e) {
        if (prevent_item_use.contains(e.getPlayer())) {
                if (e.getItem() == null) return;

                for (me.dueris.genesismc.utils.LayerContainer layer : me.dueris.genesismc.factory.CraftApoli.getLayers()) {
                    for (PowerContainer power : OriginPlayerUtils.getMultiPowerFileFromType(e.getPlayer(), getPowerFile(), layer)) {
                        if (power == null) {
                            getPowerArray().remove(e.getPlayer());
                            return;
                        } else {
                            ConditionExecutor conditionExecutor = me.dueris.genesismc.GenesisMC.getConditionExecutor();
                            boolean shouldCancel = conditionExecutor.check("item_condition", "item_conditions", e.getPlayer(), power, "origins:prevent_item_use", e.getPlayer(), null, e.getPlayer().getLocation().getBlock(), null, e.getItem(), null);
                            for (HashMap<String, Object> condition : power.getConditionFromString("item_condition", "item_conditions")) {
                                boolean inverted = (boolean) condition.getOrDefault("inverted", false);
                                if (condition.get("type") != null) {
                                    if (condition.get("type").toString().equalsIgnoreCase("origins:meat")) {
                                        if (inverted) {
                                            if (ItemCondition.getNonMeatMaterials().contains(e.getItem().getType())) {
                                                e.setCancelled(true);
                                            }else{
                                                return;
                                            }
                                        } else {
                                            if (ItemCondition.getMeatMaterials().contains(e.getItem().getType())) {
                                                e.setCancelled(true);
                                            }else{
                                                return;
                                            }
                                        }
                                    } else {
                                        if (shouldCancel) {
                                            e.setCancelled(true);
                                            setActive(e.getPlayer(), power.getTag(), true);
                                        } else {
                                            e.setCancelled(false);
                                            setActive(e.getPlayer(), power.getTag(), false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    @Override
    public String getPowerFile() {
        return "origins:prevent_item_use";
    }

    @Override
    public ArrayList<Player> getPowerArray() {
        return prevent_item_use;
    }
}