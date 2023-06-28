package me.dueris.genesismc.core.factory.conditions;

import me.dueris.genesismc.core.factory.conditions.damage.DamageCondition;
import me.dueris.genesismc.core.factory.conditions.entity.EntityCondition;
import me.dueris.genesismc.core.utils.OriginContainer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class Conditions {
    public static boolean check(Player p, OriginContainer origin, String powerfile, EntityDamageEvent dmgevent, Entity entity){
        if(origin.getPowerFileFromType(powerfile).getDamageCondition() != null && dmgevent != null){
            return DamageCondition.checkDamageCondition(p, origin, powerfile, dmgevent);
        }
        if(origin.getPowerFileFromType(powerfile).getEntityCondition() != null && entity != null){
            return EntityCondition.check(p, origin, powerfile, entity);
        }
        return true;
    }
}

