package me.dueris.genesismc.factory.powers.apoli;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import me.dueris.genesismc.factory.CraftApoli;
import me.dueris.genesismc.factory.conditions.ConditionExecutor;
import me.dueris.genesismc.factory.powers.CraftPower;
import me.dueris.genesismc.registry.LayerContainer;
import me.dueris.genesismc.registry.PowerContainer;
import me.dueris.genesismc.util.entity.OriginPlayerAccessor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;

import static me.dueris.genesismc.factory.powers.apoli.superclass.ValueModifyingSuperClass.modify_jump;

public class ModifyJumpPower extends CraftPower implements Listener {

    @Override
    public void setActive(Player p, String tag, Boolean bool) {
        if (powers_active.containsKey(p)) {
            if (powers_active.get(p).containsKey(tag)) {
                powers_active.get(p).replace(tag, bool);
            } else {
                powers_active.get(p).put(tag, bool);
            }
        } else {
            powers_active.put(p, new HashMap());
            setActive(p, tag, bool);
        }
    }

    @EventHandler
    public void ruDn(PlayerJumpEvent e) {
        Player p = e.getPlayer();
        if (modify_jump.contains(p)) {
            for (LayerContainer layer : CraftApoli.getLayers()) {
                ConditionExecutor conditionExecutor = me.dueris.genesismc.GenesisMC.getConditionExecutor();
                for (PowerContainer power : OriginPlayerAccessor.getMultiPowerFileFromType(p, getPowerFile(), layer)) {
                    if (conditionExecutor.check("condition", "conditions", p, power, "apoli:modify_jump", p, null, p.getLocation().getBlock(), null, p.getItemInHand(), null)) {
                        for (HashMap<String, Object> modifier : power.getPossibleModifiers("modifier", "modifiers")) {
                            if (modifier.get("value") instanceof Number) {
                                double modifierValue = ((Number) modifier.get("value")).doubleValue();
                                int jumpBoostLevel = (int) /*((modifierValue - 1.0) * 2.0)*/ Math.round(modifierValue * 4);

                                if (jumpBoostLevel >= 0) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20, jumpBoostLevel, false, false, false));
                                    setActive(p, power.getTag(), true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run(Player p) {

    }

    @Override
    public String getPowerFile() {
        return "apoli:modify_jump";
    }

    @Override
    public ArrayList<Player> getPowerArray() {
        return modify_jump;
    }
}