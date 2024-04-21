package me.dueris.genesismc.factory.powers.apoli;

import it.unimi.dsi.fastutil.Pair;
import me.dueris.calio.builder.inst.factory.FactoryJsonObject;
import me.dueris.genesismc.GenesisMC;
import me.dueris.genesismc.event.OriginChangeEvent;
import me.dueris.genesismc.factory.CraftApoli;
import me.dueris.genesismc.factory.conditions.ConditionExecutor;
import me.dueris.genesismc.factory.powers.CraftPower;
import me.dueris.genesismc.registry.registries.Layer;
import me.dueris.genesismc.registry.registries.Power;
import me.dueris.genesismc.util.Utils;
import me.dueris.genesismc.util.entity.OriginPlayerAccessor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

import static me.dueris.genesismc.util.CooldownUtils.createCooldownBar;
import static me.dueris.genesismc.util.CooldownUtils.getBarColor;

public class Resource extends CraftPower implements Listener {
    public static HashMap<Player, HashMap<String, Pair<BossBar, Double>>> registeredBars = new HashMap();

    public static double countNumbersBetween(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("Start integer should be less than or equal to end integer.");
        }

        int count = 0;

        for (int i = start + 1; i < end; i++) {
            count++;
        }

        return count + 1;
    }

    public static Pair<BossBar, Double> getResource(Entity entity, String tag) {
        if (registeredBars.containsKey(entity) && registeredBars.get(entity).containsKey(tag)) {
            return registeredBars.get(entity).get(tag);
        }
        return null;
    }

    @EventHandler
    public void start(PlayerJoinEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                execute(e.getPlayer());
            }
        }.runTaskLater(GenesisMC.getPlugin(), 5);
    }

    @EventHandler
    public void start(OriginChangeEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                execute(e.getPlayer());
            }
        }.runTaskLater(GenesisMC.getPlugin(), 5);
    }

    private void execute(Player p) {
        for (Layer layer : CraftApoli.getLayersFromRegistry()) {
            for (Power power : OriginPlayerAccessor.getMultiPowerFileFromType(p, getType(), layer)) {
                final String tag = power.getTag();
                FactoryJsonObject hudRender = power.getJsonObject("hud_render");
                BossBar bar = createCooldownBar(p, getBarColor(hudRender), BarStyle.SEGMENTED_6, Utils.getNameOrTag(power).first());
                Pair<BossBar, Double> pair = new Pair<BossBar, Double>() {
                    @Override
                    public BossBar left() {
                        return bar;
                    }

                    @Override
                    public Double right() {
                        return countNumbersBetween(power.getNumberOrDefault("start_value", power.getNumber("min").getInt()).getInt(), power.getNumber("max").getInt());
                    }
                };
                // Override to use start_value or default min
                if (power.isPresent("start_value")) {
                    int startValue = power.getNumber("start_value").getInt();
                    if (startValue == 0) bar.setProgress(0);
                    else bar.setProgress((double) 1 / power.getNumber("start_value").getInt());
                } else {
                    int min = power.getNumber("min").getInt();
                    if (min > 1) {
                        min = 1 / min;
                    } else if (min < 0) {
                        throw new IllegalArgumentException("Minimum value cannot be a negative number!");
                    }
                    bar.setProgress(min);
                }
                HashMap<String, Pair<BossBar, Double>> map = new HashMap<>();
                map.put(tag, pair);
                registeredBars.put(p, map);
                if (power.getJsonObject("hud_render") != null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            FactoryJsonObject hud_render = power.getJsonObject("hud_render");
                            final boolean[] canRender = {hud_render.getBooleanOrDefault("should_render", true)};
                            if (hud_render.isPresent("condition")) {
                                canRender[0] = ConditionExecutor.testEntity(power.getJsonObject("condition"), (CraftEntity) p);
                            }

                            bar.setVisible(canRender[0]);
                        }
                    }.runTaskTimer(GenesisMC.getPlugin(), 0, 1);
                }
                bar.addPlayer(p);
            }
        }
    }

    @Override
    public String getType() {
        return "apoli:resource";
    }

    @Override
    public ArrayList<Player> getPlayersWithPower() {
        return resource;
    }

}
