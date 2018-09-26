package com.rojel.wesv;

import java.util.EnumMap;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import fr.mrmicky.fastparticle.ParticleType;

/**
 * YAML plugin configuration retrieval and manipulation class.
 *
 * @author Martin Ambrus
 * @since 1.0a
 */
@SuppressWarnings("deprecation")
public class Configuration {
	/**
	 * WESV plugin instance.
	 */
	private final JavaPlugin plugin;

	/**
	 * WESV YAML configuration representation.
	 */
	private FileConfiguration config;

	/**
	 * ENUM of valid configuration values.
	 */
	private enum ConfigValue {
	    UPDATE_CHECKER("updateChecker", true, boolean.class),
		/**
		 * Size of a space left between 2 points.
		 */
		GAP_BETWEEN_POINTS("gapBetweenPoints", 0.5d, double.class),
		/**
		 * Size of a vertical space left between 2 points.
		 */
		VERTICAL_GAP("verticalGap", 1d, double.class),
		/**
		 * Interval in which particles should be updated for the MC client.
		 */
		UPDATE_PARTICLES_INTERVAL("updateParticlesInterval", 5, int.class),
		/**
		 * Interval (ms) in which the selection should be updated for the MC
		 * client.
		 */
		UPDATE_SELECTION_INTERVAL("updateSelectionInterval", 20, int.class),
		/**
		 * Whether or not to show cuboid lines.
		 */
		CUBOID_LINES("horizontalLinesForCuboid", true, boolean.class),
		/**
		 * Whether or not to show polygon lines.
		 */
		POLYGON_LINES("horizontalLinesForPolygon", true, boolean.class),
		/**
		 * Whether or not to show cylinder lines.
		 */
		CYLINDER_LINES("horizontalLinesForCylinder", true, boolean.class),
		/**
		 * Whether or not to show ellipsoid lines.
		 */
		ELLIPSOID_LINES("horizontalLinesForEllipsoid", true, boolean.class),

		CUBOID_TOP_BOTTOM("topAndBottomForCuboid", true, boolean.class),

		CYLINDER_TOP_BOTTOM("topAndBottomForCylinder", true, boolean.class),
		/**
		 * Whether or not to check for the WorldEdit tool in hand.
		 */
		CHECK_FOR_AXE("checkForAxe", false, boolean.class),

		PARTICLE_TYPE("particleEffect", ParticleType.REDSTONE, ParticleType.class),
		/**
		 * Maximum distance to see selection particles from.
		 */
		PARTICLE_DISTANCE("particleDistance", 32, int.class),
		/**
		 * Maximum size of the visualized selection itself.
		 */
		MAX_SIZE("maxSize", 10000, int.class),
		/**
		 * Language translation string from config.
		 */
		LANG_VISUALIZER_ENABLED("lang.visualizerEnabled", "&aYour visualizer has been enabled.", String.class),
		/**
		 * Language translation string from config.
		 */
		LANG_VISUALIZER_DISABLED("lang.visualizerDisabled", "&cYour visualizer has been disabled.", String.class),
		/**
		 * Language translation string from config.
		 */
		LANG_PLAYERS_ONLY("lang.playersOnly", "&cOnly a player can toggle his visualizer.", String.class),
		/**
		 * Language translation string from config.
		 */
		LANG_MAX_SELECTION("lang.maxSelection", "&6The visualizer only works with selections up to a size of %blocks% blocks", String.class),
		/**
		 * Language translation string from config.
		 */
		LANG_CONFIGRELOADED("lang.configReloaded", "&aConfiguration for visualizer was reloaded from the disk.", String.class),
		/**
		 * Language translation string from config.
		 */
		LANG_NO_PERMISSION("lang.noPermission", "&cYou don't have the permission to use this command.", String.class),
		/**
		 * Hide particles after a confgured amount of time
		 */
		FADE_DELAY("particleFadeDelay", 0, int.class),
		/**
		 * Additional data for some particles (can be a color or a material)
		 */
		PARTICLE_DATA("particleData", "255,0,0", String.class);

		private final String configValue;
		private final Object defaultValue;
		private Class<?> type;

		ConfigValue(final String configValue, final Object defaultValue, final Class<?> type) {
			this.configValue = configValue;
			this.defaultValue = defaultValue;
			this.type = type;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public String getConfigValue() {
			return configValue;
		}

		public Class<?> getType() {
			return type;
		}
	}

	private final EnumMap<ConfigValue, Object> configItems = new EnumMap<>(ConfigValue.class);

	/**
	 * Constructor, takes the WESV plugin instance as a parameter.
	 *
	 * @param plugin
	 *            WESV plugin instance.
	 */
	public Configuration(final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Loads configuration values from the config.yml YAML file.
	 */
	public void load() {
		this.plugin.saveDefaultConfig();
		this.config = this.plugin.getConfig();

		boolean found = false;
		for (final ConfigValue value : ConfigValue.values()) {
		    if (this.config.get(value.getConfigValue(), null) == null) {
				plugin.getLogger().info("Adding '" + value.getConfigValue() + "' to the config");
				found = true;
			}
		}

		if (found) {
			this.config.options().copyDefaults(true);
			this.plugin.saveConfig();
		}

		this.reloadConfig(false);
	}

	/**
	 * Loads values from config.yml file into the internal config EnumMap.
	 */
	public void reloadConfig(boolean reload) {
		if (reload) {
			this.plugin.reloadConfig();
			this.config = this.plugin.getConfig();
		}

        for (ConfigValue value : ConfigValue.values()) {
        	if (value.getType() == String.class) {
        		configItems.put(value, ChatColor.translateAlternateColorCodes('&', this.config.getString(value.getConfigValue())));
			} else if (value.getType() == boolean.class) {
				configItems.put(value, this.config.getBoolean(value.getConfigValue()));
			} else if (value.getType() == int.class) {
				configItems.put(value, this.config.getInt(value.getConfigValue()));
			} else if (value.getType() == double.class) {
				configItems.put(value, this.config.getDouble(value.getConfigValue()));
			} else if (value.getType() == ParticleType.class) {
				configItems.put(value, this.getParticleType(this.config.getString(value.getConfigValue())));
			} else {
				configItems.put(value, this.config.get(value.getConfigValue()));
			}
		}

		configItems.put(ConfigValue.PARTICLE_DATA, this.getParticleData((String) configItems.get(ConfigValue.PARTICLE_DATA)));
	}

	/**
	 * Retrieves ParticleType representation of the given name.
	 *
	 * @param name
	 *            Name of the particle type from config.
	 * @return Returns a ParticleType representation of the given name.
	 */
	public ParticleType getParticleType(final String name) {
		final ParticleType effect = ParticleType.getParticle(name);
		if (effect != null && effect.isCompatibleWithServerVersion()) {
			return effect;
		}
		this.plugin.getLogger().warning("The particle effect set in the configuration file is invalid.");
		return ParticleType.REDSTONE;
	}

	public Object getParticleData(final String name) {
		ParticleType particle = getParticle();
		if (particle.getDataType() == Color.class && !name.isEmpty()) {
			final String[] split = name.split(",");
			if (split.length == 3) {
				try {
					final int r = Integer.parseInt(split[0]);
					final int g = Integer.parseInt(split[1]);
					final int b = Integer.parseInt(split[2]);

					return Color.fromRGB(r, g, b);
				} catch (IllegalArgumentException e) {
					this.plugin.getLogger().warning("'" + name + "' is not a valid color: " + e.getMessage());
				}
			}
		} else if (particle.getDataType() == MaterialData.class) {
			final Material material = getMaterial(name);
			if (material != null) {
				return new MaterialData(material);
			}
		} else if (particle.getDataType() == ItemStack.class) {
			final Material material = getMaterial(name);
			if (material != null) {
				return new ItemStack(material);
			}
		}
		return null;
	}

	private Material getMaterial(final String mat) {
		final Material material = Material.matchMaterial(mat);
		if (material == null) {
			this.plugin.getLogger().warning("'" + mat + "' is not a valid material");
		}
		return material;
	}

	/**
	 * Checks whether WESV is enabled for the given player.
	 *
	 * @param player
	 *            Player to check if WESV is enabled for.
	 * @return Returns true if WESV is enabled for the given player, false
	 *         otherwise.
	 */
	public boolean isEnabled(final Player player) {
		final String path = "players." + player.getUniqueId().toString();
		this.config.addDefault(path, true);
		return this.config.getBoolean(path);
	}

	/**
	 * Enables or disables WESV for the given player.
	 *
	 * @param player
	 *            Player to enable or disable WESV visualization for.
	 * @param enabled
	 *            Whether to enable (true) or disable (false) WESV for the given
	 *            player.
	 */
	public void setEnabled(final Player player, final boolean enabled) {
		this.config.set("players." + player.getUniqueId().toString(), enabled);
		this.plugin.saveConfig();
	}

	/**
	 * Retrieves the "particle" property value.
	 *
	 * @return Returns the "particle" property value.
	 */
	public ParticleType getParticle() {
		return (ParticleType) this.configItems.get(ConfigValue.PARTICLE_TYPE);
	}

    /**
     * Retrieves the "gapBetweenPoints" property value.
     *
     * @return Returns the "gapBetweenPoints" property value.
     */
    public boolean isUpdateCheckerEnabled() {
        return (boolean) this.configItems.get(ConfigValue.UPDATE_CHECKER);
    }

	/**
	 * Retrieves the "gapBetweenPoints" property value.
	 *
	 * @return Returns the "gapBetweenPoints" property value.
	 */
	public double getGapBetweenPoints() {
		return (double) this.configItems.get(ConfigValue.GAP_BETWEEN_POINTS);
	}

	/**
	 * Retrieves the "verticalGap" property value.
	 *
	 * @return Returns the "verticalGap" property value.
	 */
	public double getVerticalGap() {
		return (double) this.configItems.get(ConfigValue.VERTICAL_GAP);
	}

	/**
	 * Retrieves the "updateParticlesInterval" property value.
	 *
	 * @return Returns the "updateParticlesInterval" property value.
	 */
	public int getUpdateParticlesInterval() {
		return (int) this.configItems.get(ConfigValue.UPDATE_PARTICLES_INTERVAL);
	}

	/**
	 * Retrieves the "updateSelectionInterval" property value.
	 *
	 * @return Returns the "updateSelectionInterval" property value.
	 */
	public int getUpdateSelectionInterval() {
		return (int) this.configItems.get(ConfigValue.UPDATE_SELECTION_INTERVAL);
	}

	/**
	 * Retrieves the "cuboidLines" property value.
	 *
	 * @return Returns the "cuboidLines" property value.
	 */
	public boolean isCuboidLinesEnabled() {
		return (boolean) this.configItems.get(ConfigValue.CUBOID_LINES);
	}

	/**
	 * Retrieves the "polygonLines" property value.
	 *
	 * @return Returns the "polygonLines" property value.
	 */
	public boolean isPolygonLinesEnabled() {
		return (boolean) this.configItems.get(ConfigValue.POLYGON_LINES);
	}

	/**
	 * Retrieves the "cylinderLines" property value.
	 *
	 * @return Returns the "cylinderLines" property value.
	 */
	public boolean isCylinderLinesEnabled() {
		return (boolean) this.configItems.get(ConfigValue.CYLINDER_LINES);
	}

	/**
	 * Retrieves the "ellipsoidLines" property value.
	 *
	 * @return Returns the "ellipsoidLines" property value.
	 */
	public boolean isEllipsoidLinesEnabled() {
		return (boolean) this.configItems.get(ConfigValue.ELLIPSOID_LINES);
	}

	public boolean isCuboidTopAndBottomEnabled() {
		return (boolean) this.configItems.get(ConfigValue.CUBOID_TOP_BOTTOM);
	}

	public boolean isCylinderTopAndBottomEnabled() {
		return (boolean) this.configItems.get(ConfigValue.CYLINDER_TOP_BOTTOM);
	}

	/**
	 * Retrieves the "checkForAxe" property value.
	 *
	 * @return Returns the "checkForAxe" property value.
	 */
	public boolean isCheckForAxeEnabled() {
		return (boolean) this.configItems.get(ConfigValue.CHECK_FOR_AXE);
	}

	/**
	 * Retrieves the "particleDistance" property value.
	 *
	 * @return Returns the "particleDistance" property value.
	 */
	public int getParticleDistance() {
		return (int) this.configItems.get(ConfigValue.PARTICLE_DISTANCE);
	}

	/**
	 * Retrieves the "maxSize" property value.
	 *
	 * @return Returns the "maxSize" property value.
	 */
	public int getMaxSize() {
		return (int) this.configItems.get(ConfigValue.MAX_SIZE);
	}

	/**
	 * Retrieves translation for the "langVisualizerEnabled" text.
	 *
	 * @return Translation of "langVisualizerEnabled".
	 */
	public String getLangVisualizerEnabled() {
		return (String) this.configItems.get(ConfigValue.LANG_VISUALIZER_ENABLED);
	}

	/**
	 * Retrieves translation for the "visualizerDisabled" text.
	 *
	 * @return Translation of "visualizerDisabled".
	 */
	public String getLangVisualizerDisabled() {
		return (String) this.configItems.get(ConfigValue.LANG_VISUALIZER_DISABLED);
	}

	/**
	 * Retrieves translation for the "playersOnly" text.
	 *
	 * @return Translation of "playersOnly".
	 */
	public String getLangPlayersOnly() {
		return (String) this.configItems.get(ConfigValue.LANG_PLAYERS_ONLY);
	}

	/**
	 * Retrieves translation for the "maxSelection" text.
	 *
	 * @return Translation of "maxSelection".
	 */
	public String getLangMaxSelection() {
		return (String) this.configItems.get(ConfigValue.LANG_MAX_SELECTION);
	}

	/**
	 * Retrieves translation for the "maxSelection" text.
	 *
	 * @return Translation of "maxSelection".
	 */
	public String getLangNoPermission() {
		return (String) this.configItems.get(ConfigValue.LANG_NO_PERMISSION);
	}

	/**
	 * Retrieves translation for the "configReloaded" text.
	 *
	 * @return Translation of "configReloaded".
	 */
	public String getConfigReloaded() {
		return (String) this.configItems.get(ConfigValue.LANG_CONFIGRELOADED);
	}

	// TODO JavaDoc
	public int getParticleFadeDelay() {
		return (int) this.configItems.get(ConfigValue.FADE_DELAY);
	}

	public Object getParticleData() {
		return this.configItems.get(ConfigValue.PARTICLE_DATA);
	}
}
