package me.dueris.genesismc.core.factory.powers.attributes;

import me.dueris.genesismc.core.entity.OriginPlayer;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

import static me.dueris.genesismc.core.factory.powers.Powers.*;

public class AttributeHandler extends BukkitRunnable {
    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (natural_armor.contains(p)) {
                p.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(8);
            }
            if (nine_lives.contains(p)) {
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(18);
            }
            if(attribute.contains(p)){
                Map<String, BinaryOperator<Integer>> operationMap = new HashMap<>();

                operationMap.put("addition", Integer::sum);
                operationMap.put("subtraction", (a, b) -> a - b);
                operationMap.put("multiplication", (a, b) -> a * b);
                operationMap.put("division", (a, b) -> a / b);

                System.out.println(OriginPlayer.getOrigin(p).getPowerFileFromType("origins:attribute").getModifier());

                Attribute attribute_modifier = Attribute.valueOf(OriginPlayer.getOrigin(p).getPowerFileFromType("origins:attribute").getValue("attribute"));
                Integer value = Integer.valueOf(OriginPlayer.getOrigin(p).getPowerFileFromType("origins:attribute").getValue("value"));
                String operation = OriginPlayer.getOrigin(p).getPowerFileFromType("origins:attribute").getValue("operation");
                int base_value = (int) p.getAttribute(Attribute.valueOf(attribute_modifier.toString())).getBaseValue();

                BinaryOperator mathOperator = operationMap.get(operation);
                if(mathOperator != null) {
                    int result = (int) mathOperator.apply(base_value, value);
                    p.getAttribute(Attribute.valueOf(attribute_modifier.toString())).setBaseValue(result);
                }else{
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Unable to parse origins:attribute, unable to get result");
                }

            }
        }
    }
}