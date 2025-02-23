package io.github.dueris.originspaper.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.*;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A Spigot util to easily make entities glow.
 * <p>
 * <b>1.17 -> 1.21</b>
 *
 * @author SkytAsul
 * @version 1.3.4
 */
public class GlowingEntitiesUtils implements Listener {

	protected final @NotNull Plugin plugin;
	boolean enabled = false;
	private Map<Player, PlayerData> glowing;
	private int uid;

	public GlowingEntitiesUtils(@NotNull Plugin plugin) {
		if (!Packets.enabled)
			throw new IllegalStateException(
				"The Glowing Entities API is disabled. An error has occured during initialization.");

		this.plugin = Objects.requireNonNull(plugin);

		enable();
	}

	public void enable() {
		if (enabled)
			throw new IllegalStateException("The Glowing Entities API has already been enabled.");

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		glowing = new HashMap<>();
		uid = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
		enabled = true;
	}

	public void disable() {
		if (!enabled)
			return;
		HandlerList.unregisterAll(this);
		glowing.values().forEach(playerData -> {
			try {
				Packets.removePacketsHandler(playerData);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		});
		glowing = null;
		uid = 0;
		enabled = false;
	}

	private void ensureEnabled() {
		if (!enabled)
			throw new IllegalStateException("The Glowing Entities API is not enabled.");
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		glowing.remove(event.getPlayer());
	}

	public void setGlowing(Entity entity, Player receiver) throws ReflectiveOperationException {
		setGlowing(entity, receiver, null);
	}

	public void setGlowing(Entity entity, Player receiver, ChatColor color) throws ReflectiveOperationException {
		String teamID = entity instanceof Player ? entity.getName() : entity.getUniqueId().toString();
		setGlowing(entity.getEntityId(), teamID, receiver, color, Packets.getEntityFlags(entity));
	}

	public void setGlowing(int entityID, String teamID, Player receiver) throws ReflectiveOperationException {
		setGlowing(entityID, teamID, receiver, null, (byte) 0);
	}

	public void setGlowing(int entityID, String teamID, Player receiver, ChatColor color)
		throws ReflectiveOperationException {
		setGlowing(entityID, teamID, receiver, color, (byte) 0);
	}

	public void setGlowing(int entityID, String teamID, Player receiver, ChatColor color, byte otherFlags)
		throws ReflectiveOperationException {
		ensureEnabled();
		if (color != null && !color.isColor())
			throw new IllegalArgumentException("ChatColor must be a color format");

		PlayerData playerData = glowing.get(receiver);
		if (playerData == null) {
			playerData = new PlayerData(this, receiver);
			Packets.addPacketsHandler(playerData);
			glowing.put(receiver, playerData);
		}

		GlowingData glowingData = playerData.glowingDatas.get(entityID);
		if (glowingData == null) {
			// the player did not have datas related to the entity: we must create the glowing status
			glowingData = new GlowingData(playerData, entityID, teamID, color, otherFlags);
			playerData.glowingDatas.put(entityID, glowingData);

			Packets.createGlowing(glowingData);
			if (color != null)
				Packets.setGlowingColor(glowingData);
		} else {
			// the player already had datas related to the entity: we must update the glowing status

			if (Objects.equals(glowingData.color, color))
				return; // nothing changed

			if (color == null) {
				Packets.removeGlowingColor(glowingData);
				glowingData.color = color; // we must set the color after in order to fetch the previous team
			} else {
				glowingData.color = color;
				Packets.setGlowingColor(glowingData);
			}
		}
	}

	public List<net.minecraft.world.entity.Entity> getGlowing(@NotNull ServerPlayer receiver) {
		if (this.glowing == null) return List.of();

		PlayerData playerData = glowing.get(receiver.getBukkitEntity());
		if (playerData == null)
			return new ArrayList<>();
		return playerData.glowingDatas.keySet().stream().map(receiver.level()::getEntity).toList();
	}

	public void unsetGlowing(@NotNull Entity entity, Player receiver) throws ReflectiveOperationException {
		unsetGlowing(entity.getEntityId(), receiver);
	}

	public void unsetGlowing(int entityID, Player receiver) throws ReflectiveOperationException {
		ensureEnabled();
		PlayerData playerData = glowing.get(receiver);
		if (playerData == null)
			return; // the player do not have any entity glowing

		GlowingData glowingData = playerData.glowingDatas.remove(entityID);
		if (glowingData == null)
			return; // the player did not have this entity glowing

		Packets.removeGlowing(glowingData);

		if (glowingData.color != null)
			Packets.removeGlowingColor(glowingData);

		/*
		 * if (playerData.glowingDatas.isEmpty()) { //NOSONAR // if the player do not have any other entity
		 * glowing, // we can safely remove all of its data to free some memory
		 * Packets.removePacketsHandler(playerData); glowing.remove(receiver); }
		 */
		// actually no, we should not remove the player datas
		// as it stores which teams did it receive.
		// if we do not save this information, team would be created
		// twice for the player, and BungeeCord does not like that
	}

	private static class PlayerData {

		final GlowingEntitiesUtils instance;
		final Player player;
		final Map<Integer, GlowingData> glowingDatas;
		ChannelHandler packetsHandler;
		EnumSet<ChatColor> sentColors;

		PlayerData(GlowingEntitiesUtils instance, Player player) {
			this.instance = instance;
			this.player = player;
			this.glowingDatas = new HashMap<>();
		}

	}

	private static class GlowingData {
		// unfortunately this cannot be a Java Record
		// as the "color" field is not final

		final PlayerData player;
		final int entityID;
		final String teamID;
		ChatColor color;
		byte otherFlags;
		boolean enabled;

		GlowingData(PlayerData player, int entityID, String teamID, ChatColor color, byte otherFlags) {
			this.player = player;
			this.entityID = entityID;
			this.teamID = teamID;
			this.color = color;
			this.otherFlags = otherFlags;
			this.enabled = true;
		}

	}

	protected static class Packets {

		private static final byte GLOWING_FLAG = 1 << 6;
		private static final Cache<Object, Object> packets =
			CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();
		private static final Object dummy = new Object();
		private static final String cpack = Bukkit.getServer().getClass().getPackage().getName() + ".";
		// Teams
		private static final EnumMap<ChatColor, TeamData> teams = new EnumMap<>(ChatColor.class);
		public static boolean enabled = false;
		// Entities
		static Object shulkerEntityType;
		private static Logger logger;
		private static int version;
		private static int versionMinor;
		private static Method getHandle;
		private static Method getDataWatcher;
		// Synched datas
		private static Object watcherObjectFlags;
		private static Object watcherDummy;
		private static Method watcherGet;
		private static Constructor<?> watcherItemConstructor;
		private static Method watcherItemObject;
		private static Method watcherItemDataGet;
		private static Method watcherBCreator;
		private static Method watcherBId;
		private static Method watcherBSerializer;
		private static Method watcherSerializerObject;
		// Networking
		private static Field playerConnection;
		private static Method sendPacket;
		private static Field networkManager;
		private static Field channelField;
		private static Class<?> packetBundle;
		private static Method packetBundlePackets;
		// Metadata
		private static Class<?> packetMetadata;
		private static Constructor<?> packetMetadataConstructor;
		private static Field packetMetadataEntity;
		private static Field packetMetadataItems;
		private static Constructor<?> createTeamPacket;
		private static Constructor<?> createTeamPacketData;
		private static Constructor<?> createTeam;
		private static Object scoreboardDummy;
		private static Object pushNever;
		private static Method setTeamPush;
		private static Method setTeamColor;
		private static Method getColorConstant;
		private static Constructor<?> packetAddEntity;
		private static Constructor<?> packetRemove;
		private static Object vec3dZero;

		static {
			try {
				logger = new Logger("GlowingEntities", null) {
					@Override
					public void log(LogRecord logRecord) {
						logRecord.setMessage("[GlowingEntities] " + logRecord.getMessage());
						super.log(logRecord);
					}
				};
				logger.setParent(Bukkit.getServer().getLogger());
				logger.setLevel(Level.ALL);

				// e.g. Bukkit.getBukkitVersion() -> 1.17.1-R0.1-SNAPSHOT
				String[] versions = Bukkit.getBukkitVersion().split("-R")[0].split("\\.");
				version = Integer.parseInt(versions[1]);
				versionMinor = versions.length <= 2 ? 0 : Integer.parseInt(versions[2]);

				boolean remapped = true;

				ProtocolMappings mappings = ProtocolMappings.getMappings(version, versionMinor, remapped);
				if (mappings == null) {
					mappings = ProtocolMappings.getLast(remapped);
					logger.warning("Loaded not matching version of the mappings for your server version");
				}

				/* Global variables */

				Class<?> entityClass = getNMSClass("world.entity", "Entity");
				Class<?> entityTypesClass = getNMSClass("world.entity", "EntityType");
				Object markerEntity = getNMSClass("world.entity", "Marker").getDeclaredConstructors()[0]
					.newInstance(getField(entityTypesClass, mappings.getMarkerTypeId(), null), null);

				getHandle = getCraftClass("entity", "CraftEntity").getDeclaredMethod("getHandle");
				getDataWatcher = entityClass.getDeclaredMethod(mappings.getWatcherAccessor());

				/* Synched datas */

				Class<?> dataWatcherClass = Class.forName("net.minecraft.network.syncher.SynchedEntityData");

				if (version > 20 || (version == 20 && versionMinor >= 5)) {
					var watcherBuilder = getNMSClass("network.syncher", "SynchedEntityData$Builder")
						.getDeclaredConstructor(getNMSClass("network.syncher", "SyncedDataHolder"))
						.newInstance(markerEntity);
					Field watcherBuilderItems = watcherBuilder.getClass().getDeclaredField("itemsById");
					watcherBuilderItems.setAccessible(true);
					watcherBuilderItems.set(watcherBuilder,
						Array.newInstance(watcherBuilderItems.getType().componentType(), 0));
					watcherDummy =
						watcherBuilder.getClass().getDeclaredMethod("build").invoke(watcherBuilder);
				}

				watcherObjectFlags = getField(entityClass, mappings.getWatcherFlags(), null);
				watcherGet = dataWatcherClass.getDeclaredMethod(mappings.getWatcherGet(), watcherObjectFlags.getClass());

				String subclass = version > 20 || (version == 20 && versionMinor >= 5) ? "DataValue" : "b";
				Class<?> watcherB = getNMSClass("network.syncher", "SynchedEntityData$" + subclass);
				watcherBCreator = watcherB.getDeclaredMethod("create", watcherObjectFlags.getClass(), Object.class);
				watcherBId = watcherB.getDeclaredMethod("id");
				watcherBSerializer = watcherB.getDeclaredMethod("serializer");
				watcherItemDataGet = watcherB.getDeclaredMethod("value");
				watcherSerializerObject =
					getNMSClass("network.syncher", "EntityDataSerializer").getDeclaredMethod("createAccessor", int.class);

				/* Networking */

				playerConnection = getField(getNMSClass("server.level", "ServerPlayer"), mappings.getPlayerConnection());
				sendPacket = getNMSClass("server.network", "ServerGamePacketListenerImpl").getMethod(mappings.getSendPacket(),
					getNMSClass("network.protocol", "Packet"));
				networkManager =
					getInheritedField(getNMSClass("server.network", "ServerGamePacketListenerImpl"), mappings.getNetworkManager());
				channelField = getField(getNMSClass("network", "Connection"), mappings.getChannel());

				if (version > 19 || (version == 19 && versionMinor >= 4)) {
					packetBundle = getNMSClass("network.protocol", "BundlePacket");
					packetBundlePackets =
						packetBundle.getMethod(version > 20 || (version == 20 && versionMinor >= 5) ? "subPackets" : "a");
				}

				/* Metadata */

				packetMetadata = getNMSClass("network.protocol.game", "ClientboundSetEntityDataPacket");
				packetMetadataEntity = getField(packetMetadata, mappings.getMetadataEntity());
				packetMetadataItems = getField(packetMetadata, mappings.getMetadataItems());
				packetMetadataConstructor = packetMetadata.getDeclaredConstructor(int.class, List.class);

				/* Teams */

				Class<?> scoreboardClass = getNMSClass("world.scores", "Scoreboard");
				Class<?> teamClass = getNMSClass("world.scores", "PlayerTeam");
				Class<?> pushClass = getNMSClass("world.scores", "Team$CollisionRule");
				Class<?> chatFormatClass = getNMSClass("ChatFormatting");

				createTeamPacket = getNMSClass("network.protocol.game", "ClientboundSetPlayerTeamPacket")
					.getDeclaredConstructor(String.class, int.class, Optional.class, Collection.class);
				createTeamPacket.setAccessible(true);
				createTeamPacketData = getNMSClass("network.protocol.game", "ClientboundSetPlayerTeamPacket$Parameters")
					.getDeclaredConstructor(teamClass);
				createTeam = teamClass.getDeclaredConstructor(scoreboardClass, String.class);
				scoreboardDummy = scoreboardClass.getDeclaredConstructor().newInstance();
				pushNever = pushClass.getDeclaredField("NEVER").get(null);
				setTeamPush = teamClass.getDeclaredMethod(mappings.getTeamSetCollision(), pushClass);
				setTeamColor = teamClass.getDeclaredMethod(mappings.getTeamSetColor(), chatFormatClass);
				getColorConstant = chatFormatClass.getDeclaredMethod("getByCode", char.class);

				/* Entities */

				Class<?> shulkerClass = getNMSClass("world.entity.monster", "Shulker");
				for (Field field : entityTypesClass.getDeclaredFields()) {
					if (field.getType() != entityTypesClass)
						continue;

					ParameterizedType fieldType = (ParameterizedType) field.getGenericType();
					if (fieldType.getActualTypeArguments()[0] == shulkerClass) {
						shulkerEntityType = field.get(null);
						break;
					}
				}
				if (shulkerEntityType == null)
					throw new IllegalStateException();

				Class<?> vec3dClass = getNMSClass("world.phys", "Vec3");
				vec3dZero = vec3dClass.getConstructor(double.class, double.class, double.class).newInstance(0d, 0d, 0d);


				// arg10 was added after version 1.18.2
				if (version >= 19) {
					packetAddEntity = getNMSClass("network.protocol.game", "ClientboundAddEntityPacket")
						.getDeclaredConstructor(int.class, UUID.class, double.class, double.class, double.class, float.class,
							float.class, entityTypesClass, int.class, vec3dClass, double.class);
				}

				packetRemove = getNMSClass("network.protocol.game", "ClientboundRemoveEntitiesPacket")
					.getDeclaredConstructor(version == 17 && versionMinor == 0 ? int.class : int[].class);

				enabled = true;
			} catch (Exception ex) {
				String errorMsg =
					"Glowing Entities reflection failed to initialize. The util is disabled. Please ensure your version ("
						+ Bukkit.getServer().getClass().getPackage().getName() + ") is supported.";
				if (logger == null) {
					ex.printStackTrace();
					System.err.println(errorMsg);
				} else {
					logger.log(Level.SEVERE, errorMsg, ex);
				}
			}
		}

		public static void sendPackets(Player p, Object... packets) throws ReflectiveOperationException {
			Object connection = playerConnection.get(getHandle.invoke(p));
			for (Object packet : packets) {
				if (packet == null)
					continue;
				sendPacket.invoke(connection, packet);
			}
		}

		public static byte getEntityFlags(Entity entity) throws ReflectiveOperationException {
			Object nmsEntity = getHandle.invoke(entity);
			Object dataWatcher = getDataWatcher.invoke(nmsEntity);
			return (byte) watcherGet.invoke(dataWatcher, watcherObjectFlags);
		}

		public static void createGlowing(GlowingData glowingData) throws ReflectiveOperationException {
			setMetadata(glowingData.player.player, glowingData.entityID, computeFlags(glowingData), true);
		}

		private static byte computeFlags(GlowingData glowingData) {
			byte newFlags = glowingData.otherFlags;
			if (glowingData.enabled) {
				newFlags |= GLOWING_FLAG;
			} else {
				newFlags &= ~GLOWING_FLAG;
			}
			return newFlags;
		}

		public static Object createFlagWatcherItem(byte newFlags) throws ReflectiveOperationException {
			return watcherItemConstructor != null ? watcherItemConstructor.newInstance(watcherObjectFlags, newFlags)
				: watcherBCreator.invoke(null, watcherObjectFlags, newFlags);
		}

		public static void removeGlowing(GlowingData glowingData) throws ReflectiveOperationException {
			setMetadata(glowingData.player.player, glowingData.entityID, glowingData.otherFlags, true);
		}

		public static void updateGlowingState(GlowingData glowingData) throws ReflectiveOperationException {
			if (glowingData.enabled)
				createGlowing(glowingData);
			else
				removeGlowing(glowingData);
		}

		@SuppressWarnings("squid:S3011")
		public static void setMetadata(Player player, int entityId, byte flags, boolean ignore)
			throws ReflectiveOperationException {
			List<Object> dataItems = new ArrayList<>(1);
			dataItems.add(watcherItemConstructor != null ? watcherItemConstructor.newInstance(watcherObjectFlags, flags)
				: watcherBCreator.invoke(null, watcherObjectFlags, flags));

			Object packetMetadata;
			if (version < 19 || (version == 19 && versionMinor < 3)) {
				packetMetadata = packetMetadataConstructor.newInstance(entityId, watcherDummy, false);
				packetMetadataItems.set(packetMetadata, dataItems);
			} else {
				packetMetadata = packetMetadataConstructor.newInstance(entityId, dataItems);
			}
			if (ignore)
				packets.put(packetMetadata, dummy);
			sendPackets(player, packetMetadata);
		}

		public static void setGlowingColor(GlowingData glowingData) throws ReflectiveOperationException {
			boolean sendCreation = false;
			if (glowingData.player.sentColors == null) {
				glowingData.player.sentColors = EnumSet.of(glowingData.color);
				sendCreation = true;
			} else if (glowingData.player.sentColors.add(glowingData.color)) {
				sendCreation = true;
			}

			TeamData teamData = teams.get(glowingData.color);
			if (teamData == null) {
				teamData = new TeamData(glowingData.player.instance.uid, glowingData.color);
				teams.put(glowingData.color, teamData);
			}

			Object entityAddPacket = teamData.getEntityAddPacket(glowingData.teamID);
			if (sendCreation) {
				sendPackets(glowingData.player.player, teamData.creationPacket, entityAddPacket);
			} else {
				sendPackets(glowingData.player.player, entityAddPacket);
			}
		}

		public static void removeGlowingColor(GlowingData glowingData) throws ReflectiveOperationException {
			TeamData teamData = teams.get(glowingData.color);
			if (teamData == null)
				return; // must not happen; this means the color has not been set previously

			sendPackets(glowingData.player.player, teamData.getEntityRemovePacket(glowingData.teamID));
		}

		public static void createEntity(Player player, int entityId, UUID entityUuid, Object entityType, Location location)
			throws IllegalArgumentException, ReflectiveOperationException {
			Object packet;
			if (version >= 19) {
				packet = packetAddEntity.newInstance(entityId, entityUuid, location.getX(), location.getY(),
					location.getZ(), location.getPitch(), location.getYaw(), entityType, 0, vec3dZero, 0d);
			} else {
				packet = packetAddEntity.newInstance(entityId, entityUuid, location.getX(), location.getY(),
					location.getZ(), location.getPitch(), location.getYaw(), entityType, 0, vec3dZero);
			}
			sendPackets(player, packet);
		}

		public static void removeEntities(Player player, int... entitiesId) throws ReflectiveOperationException {
			Object[] packets;
			if (version == 17 && versionMinor == 0) {
				packets = new Object[entitiesId.length];
				for (int i = 0; i < entitiesId.length; i++) {
					packets[i] = packetRemove.newInstance(entitiesId[i]);
				}
			} else {
				packets = new Object[]{packetRemove.newInstance(entitiesId)};
			}

			sendPackets(player, packets);
		}

		private static Channel getChannel(Player player) throws ReflectiveOperationException {
			return (Channel) channelField.get(networkManager.get(playerConnection.get(getHandle.invoke(player))));
		}

		public static void addPacketsHandler(PlayerData playerData) throws ReflectiveOperationException {
			playerData.packetsHandler = new ChannelDuplexHandler() {
				@Override
				public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
					if (msg.getClass().equals(packetMetadata) && packets.asMap().remove(msg) == null) {
						int entityID = packetMetadataEntity.getInt(msg);
						GlowingData glowingData = playerData.glowingDatas.get(entityID);
						if (glowingData != null) {

							List<Object> items = (List<Object>) packetMetadataItems.get(msg);
							if (items != null) {

								boolean containsFlags = false;
								boolean edited = false;
								for (int i = 0; i < items.size(); i++) {
									Object item = items.get(i);
									Object watcherObject;
									if (watcherItemObject != null) {
										watcherObject = watcherItemObject.invoke(item);
									} else {
										Object serializer = watcherBSerializer.invoke(item);
										watcherObject = watcherSerializerObject.invoke(serializer, watcherBId.invoke(item));
									}

									if (watcherObject.equals(watcherObjectFlags)) {
										containsFlags = true;
										byte flags = (byte) watcherItemDataGet.invoke(item);
										glowingData.otherFlags = flags;
										byte newFlags = computeFlags(glowingData);
										if (newFlags != flags) {
											edited = true;
											items = new LinkedList<>(items);
											// we cannot simply edit the item as it may be backed in the datawatcher, so we
											// make a copy of the list
											items.set(i, createFlagWatcherItem(newFlags));
											break;
											// we can break right now as the "flags" datawatcher object may not be present
											// twice in the same packet
										}
									}
								}

								if (!edited && !containsFlags) {
									// if the packet does not contain any flag information, we are unsure if it is a packet
									// simply containing informations about another object's data update OR if it is a packet
									// containing all non-default informations of the entity. Such as packet can be sent when
									// the player has got far away from the entity and come in sight distance again.
									// In this case, we must add manually the "flags" object, otherwise it would stay 0 and
									// the entity would not be glowing.
									// Ideally, we should listen for an "entity add" packet to be sure we are in the case
									// above, but honestly it's annoying because there are multiple types of "entity add"
									// packets, so we do like this instead. Less performant, but not by far.
									byte flags = computeFlags(glowingData);
									if (flags != 0) {
										edited = true;
										items = new LinkedList<>(items);
										items.add(createFlagWatcherItem(flags));
									}
								}

								if (edited) {
									// some of the metadata packets are broadcasted to all players near the target entity.
									// hence, if we directly edit the packet, some users that were not intended to see the
									// glowing color will be able to see it. We should send a new packet to the viewer only.

									Object newMsg;
									if (version < 19 || (version == 19 && versionMinor < 3)) {
										newMsg = packetMetadataConstructor.newInstance(entityID, watcherDummy, false);
										packetMetadataItems.set(newMsg, items);
									} else {
										newMsg = packetMetadataConstructor.newInstance(entityID, items);
									}
									packets.put(newMsg, dummy);
									sendPackets(playerData.player, newMsg);

									return; // we cancel the send of this packet
								}
							}
						}
					} else if (msg.getClass().equals(packetBundle)) {
						handlePacketBundle(msg);
					}
					super.write(ctx, msg, promise);
				}

				@SuppressWarnings("rawtypes")
				private void handlePacketBundle(Object bundle) throws ReflectiveOperationException {
					Iterable subPackets = (Iterable) packetBundlePackets.invoke(bundle);
					for (Iterator iterator = subPackets.iterator(); iterator.hasNext(); ) {
						Object packet = iterator.next();

						if (packet.getClass().equals(packetMetadata)) {
							int entityID = packetMetadataEntity.getInt(packet);
							GlowingData glowingData = playerData.glowingDatas.get(entityID);
							if (glowingData != null) {
								// means the bundle packet contains metadata about an entity that must be glowing.
								// editing a bundle packet is annoying, so we'll let it go to the player
								// and then send a metadata packet containing the correct glowing flag.

								Bukkit.getScheduler().runTaskLaterAsynchronously(playerData.instance.plugin, () -> {
									try {
										updateGlowingState(glowingData);
									} catch (ReflectiveOperationException e) {
										e.printStackTrace();
									}
								}, 1L);
								return;
							}
						}
					}
				}

			};

			getChannel(playerData.player).pipeline().addBefore("packet_handler", null, playerData.packetsHandler);
		}

		public static void removePacketsHandler(PlayerData playerData) throws ReflectiveOperationException {
			if (playerData.packetsHandler != null) {
				getChannel(playerData.player).pipeline().remove(playerData.packetsHandler);
			}
		}

		/* Reflection utils */
		@Deprecated
		private static Object getField(Class<?> clazz, String name, Object instance) throws ReflectiveOperationException {
			return getField(clazz, name).get(instance);
		}

		private static Field getField(Class<?> clazz, String name) throws ReflectiveOperationException {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		}

		private static Field getInheritedField(Class<?> clazz, String name) throws ReflectiveOperationException {
			Class<?> superclass = clazz;
			do {
				try {
					Field field = superclass.getDeclaredField(name);
					field.setAccessible(true);
					return field;
				} catch (NoSuchFieldException ex) {
				}
			} while ((superclass = clazz.getSuperclass()) != null);

			// if we are here this means the field is not in superclasses
			throw new NoSuchFieldException(name);
		}

		private static Class<?> getCraftClass(String craftPackage, String className) throws ClassNotFoundException {
			return Class.forName(cpack + craftPackage + "." + className);
		}

		private static Class<?> getNMSClass(String className) throws ClassNotFoundException {
			return Class.forName("net.minecraft." + className);
		}

		private static Class<?> getNMSClass(String nmPackage, String className) throws ClassNotFoundException {
			return Class.forName("net.minecraft." + nmPackage + "." + className);
		}

		private enum ProtocolMappings {
			V1_17(
				17,
				0,
				false,
				"Z",
				"Y",
				"getDataWatcher",
				"get",
				"b",
				"a",
				"sendPacket",
				"k",
				"setCollisionRule",
				"setColor",
				"a",
				"b"),
			V1_20_5_REMAPPED(
				21,
				1,
				true,
				"DATA_SHARED_FLAGS_ID",
				"MARKER",
				"getEntityData",
				"get",
				"connection",
				"connection",
				"send",
				"channel",
				"setCollisionRule",
				"setColor",
				"id",
				"packedItems"
			);

			static {
				try {
					fillAll();
				} catch (ReflectiveOperationException ex) {
					logger.severe("Failed to fill up all datas for mappings.");
					ex.printStackTrace();
				}
			}

			private final int major, minor;
			private final boolean remapped;
			private final String watcherFlags;
			private final String markerTypeId;
			private final String watcherAccessor;
			private final String watcherGet;
			private final String playerConnection;
			private final String networkManager;
			private final String sendPacket;
			private final String channel;
			private final String teamSetCollsion;
			private final String teamSetColor;
			private final String metadataEntity;
			private final String metadataItems;

			ProtocolMappings(int major, int minor, boolean remapped,
							 String watcherFlags, String markerTypeId, String watcherAccessor, String watcherGet,
							 String playerConnection, String networkManager, String sendPacket, String channel,
							 String teamSetCollsion, String teamSetColor, String metdatataEntity, String metadataItems) {
				this.major = major;
				this.minor = minor;
				this.remapped = remapped;
				this.watcherFlags = watcherFlags;
				this.markerTypeId = markerTypeId;
				this.watcherAccessor = watcherAccessor;
				this.watcherGet = watcherGet;
				this.playerConnection = playerConnection;
				this.networkManager = networkManager;
				this.sendPacket = sendPacket;
				this.channel = channel;
				this.teamSetCollsion = teamSetCollsion;
				this.teamSetColor = teamSetColor;
				this.metadataEntity = metdatataEntity;
				this.metadataItems = metadataItems;
			}

			private static void fillAll() throws ReflectiveOperationException {
				ProtocolMappings lastUnmapped = V1_17;
				ProtocolMappings lastRemapped = V1_20_5_REMAPPED;
				// /!\ we start at 1
				for (int i = 1; i < ProtocolMappings.values().length; i++) {
					ProtocolMappings map = ProtocolMappings.values()[i];
					for (Field field : ProtocolMappings.class.getDeclaredFields()) {
						if (field.getType() == String.class && field.get(map) == null) {
							field.set(map, field.get(map.isRemapped() ? lastRemapped : lastUnmapped));
						}
					}
					if (map.isRemapped())
						lastRemapped = map;
					else
						lastUnmapped = map;
				}
			}

			public static ProtocolMappings getMappings(int major, int minor, boolean remapped) {
				ProtocolMappings lastGood = null;
				for (ProtocolMappings map : values()) {
					if (map.isRemapped() != remapped)
						continue;

					// loop in ascending version order
					if (major == map.getMajor()) {
						if (minor == map.getMinor())
							return map; // perfect match

						if (minor > map.getMinor())
							lastGood = map; // looking for newer minor version

						if (minor < map.getMinor())
							return lastGood; // looking for older minor version: we get the last correct one
					}
				}
				// will return either null if no mappings matched the major => fallback to latest major with a
				// warning, either the last mappings with same major and smaller minor
				return lastGood;
			}

			public static ProtocolMappings getLast(boolean remapped) {
				return Arrays.stream(values()).filter(map -> map.isRemapped() == remapped).reduce((l, r) -> r).get();
			}

			public int getMajor() {
				return major;
			}

			public int getMinor() {
				return minor;
			}

			public boolean isRemapped() {
				return remapped;
			}

			public String getWatcherFlags() {
				return watcherFlags;
			}

			public String getMarkerTypeId() {
				return markerTypeId;
			}

			public String getWatcherAccessor() {
				return watcherAccessor;
			}

			public String getWatcherGet() {
				return watcherGet;
			}

			public String getPlayerConnection() {
				return playerConnection;
			}

			public String getNetworkManager() {
				return networkManager;
			}

			public String getSendPacket() {
				return sendPacket;
			}

			public String getChannel() {
				return channel;
			}

			public String getTeamSetCollision() {
				return teamSetCollsion;
			}

			public String getTeamSetColor() {
				return teamSetColor;
			}

			public String getMetadataEntity() {
				return metadataEntity;
			}

			public String getMetadataItems() {
				return metadataItems;
			}

		}

		private static class TeamData {

			private final String id;
			private final Object creationPacket;

			private final Cache<String, Object> addPackets =
				CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.MINUTES).build();
			private final Cache<String, Object> removePackets =
				CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.MINUTES).build();

			public TeamData(int uid, ChatColor color) throws ReflectiveOperationException {
				if (!color.isColor())
					throw new IllegalArgumentException();
				id = "glow-" + uid + color.getChar();
				Object team = createTeam.newInstance(scoreboardDummy, id);
				setTeamPush.invoke(team, pushNever);
				setTeamColor.invoke(team, getColorConstant.invoke(null, color.getChar()));
				Object packetData = createTeamPacketData.newInstance(team);
				creationPacket = createTeamPacket.newInstance(id, 0, Optional.of(packetData), Collections.EMPTY_LIST);
			}

			public Object getEntityAddPacket(String teamID) throws ReflectiveOperationException {
				Object packet = addPackets.getIfPresent(teamID);
				if (packet == null) {
					packet = createTeamPacket.newInstance(id, 3, Optional.empty(), List.of(teamID));
					addPackets.put(teamID, packet);
				}
				return packet;
			}

			public Object getEntityRemovePacket(String teamID) throws ReflectiveOperationException {
				Object packet = removePackets.getIfPresent(teamID);
				if (packet == null) {
					packet = createTeamPacket.newInstance(id, 4, Optional.empty(), List.of(teamID));
					removePackets.put(teamID, packet);
				}
				return packet;
			}

		}

	}

}