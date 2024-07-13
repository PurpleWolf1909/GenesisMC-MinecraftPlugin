package me.dueris.originspaper.screen;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import me.dueris.originspaper.OriginsPaper;
import me.dueris.originspaper.factory.powers.apoli.ModifyPlayerSpawnPower;
import me.dueris.originspaper.factory.powers.holder.PowerType;
import me.dueris.originspaper.registry.registries.Layer;
import me.dueris.originspaper.registry.registries.Origin;
import me.dueris.originspaper.util.ComponentUtil;
import me.dueris.originspaper.util.entity.PlayerManager;
import me.dueris.originspaper.util.entity.PowerHolderComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record OriginPage(Origin origin) implements ChoosingPage {

	public static void setAttributesToDefault(org.bukkit.entity.Player player) {
		setAttributesToDefault(((CraftPlayer) player).getHandle());
	}

	public static void setAttributesToDefault(Player p) {
		p.getBukkitEntity().getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(0);
		p.getBukkitEntity().getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(0);
		p.getBukkitEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1);
		p.getBukkitEntity().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);
		p.getBukkitEntity().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0);
		p.getBukkitEntity().getAttribute(Attribute.GENERIC_LUCK).setBaseValue(0);
		p.getBukkitEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
		p.getBukkitEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
		p.getBukkitEntity().getAttribute(Attribute.GENERIC_GRAVITY).setBaseValue(0.08);
	}

	public static ItemStack itemProperties(ItemStack item, Component displayName, ItemFlag[] itemFlag, Enchantment enchantment, String lore) {
		ItemMeta itemMeta = item.getItemMeta();
		if (displayName != null)
			itemMeta.displayName(displayName.decorate(TextDecoration.ITALIC.withState(false).decoration()));
		if (itemFlag != null) itemMeta.addItemFlags(itemFlag);
		if (enchantment != null) itemMeta.addEnchant(enchantment, 1, true);
		if (lore != null) itemMeta.lore(cutStringIntoLines(lore).stream().map(OriginPage::noItalic).toList());
		item.setItemMeta(itemMeta);
		return item;
	}

	private static Component noItalic(String string) {
		return Component.text(string).decorate(TextDecoration.ITALIC.withState(false).decoration());
	}

	public static List<String> cutStringIntoLines(String string) {
		ArrayList<String> strings = new ArrayList<>();
		int startStringLength = string.length();
		while (string.length() > 40) {
			for (int i = 40; i > 1; i--) {
				if (String.valueOf(string.charAt(i)).matches("[\\s\\n]") || String.valueOf(string.charAt(i)).equals(" ")) {
					strings.add(string.substring(0, i));
					string = string.substring(i + 1);
					break;
				}
			}
			if (startStringLength == string.length()) return List.of(string);
		}
		if (strings.isEmpty()) return List.of(string);
		strings.add(string);
		return strings.stream().toList();
	}


	public int getOrder() {
		return this.origin.getOrder();
	}

	@Override
	public NamespacedKey key() {
		return NamespacedKey.fromString(this.origin.key().asString() + "_page");
	}

	@Override
	public ItemStack[] createDisplay(Player player, Layer layer) {
		List<ItemStack> stacks = new ArrayList<>();
		List<PowerType> powerContainers = new ArrayList<>(origin.getPowerContainers().stream().filter(Objects::nonNull).filter(p -> !p.isHidden()).toList());

		for (int i = 0; i < 54; i++) {
			if (i <= 2 || (i >= 6 && i <= 8)) {
				Material impactMaterial = this.origin.getImpact() == 1 ? Material.GREEN_STAINED_GLASS_PANE :
					this.origin.getImpact() == 2 ? Material.YELLOW_STAINED_GLASS_PANE :
						this.origin.getImpact() == 3 ? Material.RED_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
				Component impactComponent = this.origin.getImpact() == 1 ? Component.text("Low").color(TextColor.color(0x54FC54)) :
					this.origin.getImpact() == 2 ? Component.text("Medium").color(TextColor.color(0xDDCD33)) :
						this.origin.getImpact() == 3 ? Component.text("High").color(TextColor.color(0xFC5454)) : Component.text("None").color(TextColor.color(0xA8A8A8));
				Component fullImpactComponent = Component.textOfChildren(Component.text("Impact: "), impactComponent).decorate(TextDecoration.ITALIC.as(false).decoration());
				ItemStack impact = itemProperties(new ItemStack(impactMaterial), fullImpactComponent, ItemFlag.values(), null, null);

				if ((this.origin.getImpact() == 1 && (i == 0 || i == 8)) ||
					(this.origin.getImpact() == 2 && (i == 0 || i == 8 || i == 1 || i == 7)) ||
					(this.origin.getImpact() == 3 || this.origin.getImpact() == 0)) {
					stacks.add(impact);
				} else {
					stacks.add(new ItemStack(Material.AIR));
				}

			} else if (i == 13) {
				stacks.add(getChoosingStack(player));
			} else if ((i >= 20 && i <= 24) || (i >= 29 && i <= 33) || (i == 39 || i == 40 || i == 41)) {
				while (!powerContainers.isEmpty() && (powerContainers.get(0).isHidden()
					|| (powerContainers.get(0).getName().equalsIgnoreCase("No Name")
					&& powerContainers.get(0).getDescription().equalsIgnoreCase("No Description")))) {
					powerContainers.remove(0);
				}
				if (!powerContainers.isEmpty()) {
					ItemStack originPower = new ItemStack(Material.FILLED_MAP);

					ItemMeta meta = originPower.getItemMeta();
					meta.displayName(ComponentUtil.apply(powerContainers.get(0).getName()));
					Arrays.stream(ItemFlag.values()).toList().forEach(originPower::addItemFlags);
					meta.lore(ComponentUtil.apply(cutStringIntoLines(powerContainers.get(0).getDescription())));
					originPower.setItemMeta(meta);
					Arrays.stream(ItemFlag.values()).toList().forEach(originPower::addItemFlags);

					stacks.add(originPower);

					powerContainers.remove(0);
				} else {
					ItemStack blank = new ItemStack(Material.MAP);
					Arrays.stream(ItemFlag.values()).toList().forEach(blank::addItemFlags);
					stacks.add(blank);
				}
			} else if (i == 45) {
				stacks.add(ScreenNavigator.BACK_ITEMSTACK);
			} else if (i == 53) {
				stacks.add(ScreenNavigator.NEXT_ITEMSTACK);
			} else {
				stacks.add(new ItemStack(Material.AIR));
			}
		}
		return stacks.toArray(new ItemStack[0]);
	}

	@Override
	public ItemStack getChoosingStack(Player player) {
		ItemStack originIcon = new ItemStack(this.origin.getMaterialIcon());
		org.bukkit.entity.Player bukkit = (org.bukkit.entity.Player) player.getBukkitEntity();
		if (originIcon.getType().equals(Material.PLAYER_HEAD)) {
			SkullMeta skull_p = (SkullMeta) originIcon.getItemMeta();
			skull_p.setOwningPlayer(bukkit);
			skull_p.setOwner(bukkit.getName());
			skull_p.setPlayerProfile(bukkit.getPlayerProfile());
			skull_p.setOwnerProfile(bukkit.getPlayerProfile());
			originIcon.setItemMeta(skull_p);
		}
		return itemProperties(originIcon, Component.text(this.origin.getName()), ItemFlag.values(), null, this.origin.getDescription());
	}

	@Override
	public void onChoose(Player player, Layer layer) {
		PowerHolderComponent.setOrigin((org.bukkit.entity.Player) player.getBukkitEntity(), layer, this.origin);
		player.getBukkitEntity().getOpenInventory().close();
		org.bukkit.entity.Player bukkitEntity = (org.bukkit.entity.Player) player.getBukkitEntity();

		if (PlayerManager.firstJoin.contains(bukkitEntity)) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (PowerHolderComponent.hasPowerType(bukkitEntity, ModifyPlayerSpawnPower.class)) {
						ModifyPlayerSpawnPower.suspendPlayer(bukkitEntity);
					}
					for (ModifyPlayerSpawnPower power : PowerHolderComponent.getPowers(bukkitEntity, ModifyPlayerSpawnPower.class)) {
						power.runD(new PlayerPostRespawnEvent(bukkitEntity, bukkitEntity.getLocation(), false));
					}
					PlayerManager.firstJoin.remove(bukkitEntity);
				}
			}.runTaskLater(OriginsPaper.getPlugin(), 4);
		}
	}
}