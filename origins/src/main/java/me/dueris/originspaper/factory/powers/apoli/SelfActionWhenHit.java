package me.dueris.originspaper.factory.powers.apoli;

import com.google.gson.JsonObject;
import me.dueris.calio.data.FactoryData;
import me.dueris.calio.data.factory.FactoryJsonObject;
import me.dueris.originspaper.OriginsPaper;
import me.dueris.originspaper.factory.actions.Actions;
import me.dueris.originspaper.factory.conditions.ConditionExecutor;
import me.dueris.originspaper.factory.data.types.HudRender;
import me.dueris.originspaper.factory.powers.holder.PowerType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class SelfActionWhenHit extends PowerType implements Listener, CooldownPower {
	private final FactoryJsonObject entityAction;
	private final FactoryJsonObject damageCondition;
	private final HudRender hudRender;
	private final int cooldown;

	public SelfActionWhenHit(String name, String description, boolean hidden, FactoryJsonObject condition, int loading_priority, FactoryJsonObject entityAction, FactoryJsonObject damageCondition, FactoryJsonObject hudRender, int cooldown) {
		super(name, description, hidden, condition, loading_priority);
		this.entityAction = entityAction;
		this.damageCondition = damageCondition;
		this.hudRender = HudRender.createHudRender(hudRender);
		this.cooldown = cooldown;
	}

	public static FactoryData registerComponents(FactoryData data) {
		return PowerType.registerComponents(data).ofNamespace(OriginsPaper.apoliIdentifier("self_action_when_hit"))
			.add("entity_action", FactoryJsonObject.class, new FactoryJsonObject(new JsonObject()))
			.add("damage_condition", FactoryJsonObject.class, new FactoryJsonObject(new JsonObject()))
			.add("hud_render", FactoryJsonObject.class, new FactoryJsonObject(new JsonObject()))
			.add("cooldown", int.class, 1);
	}

	@EventHandler
	public void s(EntityDamageEvent e) {
		Entity actor = e.getEntity();

		if (!(actor instanceof Player player)) return;
		if (!getPlayers().contains(player)) return;

		if (Cooldown.isInCooldown(player, this)) return;
		if (isActive(player) && ConditionExecutor.testDamage(damageCondition, e)) {
			Actions.executeEntity(player, entityAction);
			Cooldown.addCooldown(player, cooldown, this);
		}
	}

	@Override
	public HudRender getHudRender() {
		return hudRender;
	}

	@Override
	public int getCooldown() {
		return cooldown;
	}
}
