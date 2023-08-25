package me.dueris.genesismc.factory.powers.OriginsMod.actions;

import me.dueris.genesismc.CooldownStuff;
import me.dueris.genesismc.GenesisMC;
import me.dueris.genesismc.entity.OriginPlayer;
import me.dueris.genesismc.factory.conditions.ConditionExecutor;
import me.dueris.genesismc.factory.powers.CraftPower;
import me.dueris.genesismc.utils.OriginContainer;
import me.dueris.genesismc.utils.PowerContainer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

import static me.dueris.genesismc.KeybindHandler.isKeyBeingPressed;

public class SelfActionOnHit extends CraftPower implements Listener {
    @Override
    public void run() {

    }

    @EventHandler
    public void s(EntityDamageByEntityEvent e){
        Entity actor = e.getEntity();
        Entity target = e.getDamager();

        if (!(target instanceof Player player)) return;
        if (!getPowerArray().contains(target)) return;

        for (OriginContainer origin : OriginPlayer.getOrigin(player).values()) {
            ConditionExecutor executor = new ConditionExecutor();
            if(CooldownStuff.isPlayerInCooldown((Player) target, "key.attack")) return;
            if(executor.check("condition", "conditions", (Player) target, origin, getPowerFile(), null, target)){
                if(!getPowerArray().contains(target)) return;
                setActive(origin.getPowerFileFromType(getPowerFile()).getTag(), true);
                    ActionTypes.EntityActionType(target, origin.getPowerFileFromType(getPowerFile()).getEntityAction());
                    if(origin.getPowerFileFromType(getPowerFile()).get("cooldown", "1") != null){
                        CooldownStuff.addCooldown((Player) target, origin.getPowerFileFromType(getPowerFile()).getTag(),Integer.parseInt(origin.getPowerFileFromType(getPowerFile()).get("cooldown", "1").toString()), "key.attack");
                    }
            }else{
                if(!getPowerArray().contains(target)) return;
                setActive(origin.getPowerFileFromType(getPowerFile()).getTag(), false);
            }
        }
    }

    @Override
    public String getPowerFile() {
        return "origins:self_action_on_hit";
    }

    @Override
    public ArrayList<Player> getPowerArray() {
        return self_action_on_hit;
    }

    @Override
    public void setActive(String tag, Boolean bool) {
        if(powers_active.containsKey(tag)){
            powers_active.replace(tag, bool);
        }else{
            powers_active.put(tag, bool);
        }
    }
}