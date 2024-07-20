package com.github.gpaddons.util.lang;

import com.github.gpaddons.util.lang.replacement.ComponentReplacement;
import com.github.gpaddons.util.lang.replacement.TextReplacer;
import com.github.gpaddons.util.lang.value.CommonValues;
import com.github.gpaddons.util.lang.value.ConfigMessage;
import com.github.gpaddons.util.lang.value.ConfigRequired;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A basic translation manager.
 */
public final class Lang {

  private static final String LANG_FILE = "lang.yml";
  private static final @NotNull YamlConfiguration LANG;
  private static final LoadingCache<UUID, PlayerProfile> PROFILES = CacheBuilder.newBuilder()
      .maximumSize(20)
      .build(CacheLoader.from(uuid -> Bukkit.getOfflinePlayer(uuid).getPlayerProfile()));

  static {
    Plugin plugin = JavaPlugin.getProvidingPlugin(Lang.class);
    File file = new File(plugin.getDataFolder(), LANG_FILE);

    if (!file.exists()) {
      write(plugin, file);
    }

    LANG = YamlConfiguration.loadConfiguration(file);
  }

  private Lang() {
  }

  private static void write(@NotNull Plugin plugin, @NotNull File file) {
    try (InputStream resource = plugin.getResource(LANG_FILE)) {
      if (resource == null) {
        plugin.getLogger().log(Level.WARNING, () -> "Unable to load resource " + LANG_FILE);
        return;
      }

      Path writeTo = file.toPath();

      Files.createDirectories(writeTo.getParent());

      Files.copy(resource, writeTo);
    } catch (IOException e) {
      plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
    }
  }

  private static @Nullable String get(@NotNull ConfigMessage message) {
    String value = LANG.getString(message.getKey(), message.getDefault());
    return value.isBlank() ? null : ChatColor.translateAlternateColorCodes('&', value);
  }

  /**
   * Send a {@link ConfigMessage} to the specified CommandSender after applying any variable
   * replacements.
   *
   * <p>Messages that are configured to be null or blank are not sent.
   *
   * @param recipient     the recipient of the messagee
   * @param message       the Message to send
   * @param textReplacers the plain text variable replacement providers
   */
  public static void send(
      @NotNull CommandSender recipient,
      @NotNull ConfigMessage message,
      @NotNull TextReplacer @NotNull ... textReplacers) {
    send(recipient, message, textReplacers, new ComponentReplacement[0]);
  }

  /**
   * Send a {@link ConfigMessage} to the specified CommandSender after applying any variable
   * replacements.
   *
   * <p>Messages that are configured to be null or blank are not sent.
   *
   * @param recipient     the recipient of the messagee
   * @param message       the Message to send
   * @param textReplacers the plain text variable replacement providers
   * @param componentReplacements the component variable replacement providers
   */
  public static void send(
      @NotNull CommandSender recipient,
      @NotNull ConfigMessage message,
      @NotNull TextReplacer @NotNull [] textReplacers,
      @NotNull ComponentReplacement @NotNull ... componentReplacements) {
    String value = get(message);

    if (value == null) {
      return;
    }

    // Do plain text replacement.
    for (TextReplacer replacer : textReplacers) {
      value = replacer.replace(value);
    }

    // If there are no component replacements being done, the message is ready for sending.
    if (componentReplacements.length == 0) {
      recipient.sendMessage(value);
      return;
    }

    List<Object> segments = new ArrayList<>();
    segments.add(value);

    // Remove component placeholders in plain text and replace with components.
    for (ComponentReplacement replacement : componentReplacements) {
      insertComponents(segments, replacement);
    }

    // If there is only one segment in plain text, no replacements were made. Send plain text.
    if (segments.size() == 1 && segments.getFirst() instanceof String finalizedMessage) {
      recipient.sendMessage(finalizedMessage);
      return;
    }

    // Merge the components into one.
    // Subsequent components are children to preserve legacy formatting behavior.
    BaseComponent component = joinSegments(segments);
    if (component != null) {
      recipient.spigot().sendMessage(component);
    }
  }

  private static void insertComponents(
      @NotNull List<Object> segments,
      @NotNull ComponentReplacement replacement) {
    for (int index = 0; index < segments.size(); ++index) {
      if (!(segments.get(index) instanceof String segment)) {
        continue;
      }

      Matcher matcher = replacement.getPattern().matcher(segment);
      int lastEnd = 0;
      while (matcher.find()) {
        // Since we have a match, we're replacing the segment.
        if (lastEnd == 0) {
          segments.remove(index);
        }

        int start = matcher.start();
        // If there's unmatched text between the previous match and the current one, add it.
        if (start > lastEnd) {
          segments.add(index, segment.substring(lastEnd, start));
          ++index;
        }
        lastEnd = matcher.end();

        // Add the component.
        segments.add(index, replacement.getReplacement());
        ++index;
      }

      // If there was no match, nothing to do.
      if (lastEnd == 0) {
        continue;
      }

      if (lastEnd == segment.length()) {
        // If there is no substring after the last match, decrease index to point to last addition.
        --index;
      } else {
        // Otherwise, add remaining text.
        segments.add(index, segment.substring(lastEnd));
      }
    }
  }

  private static @Nullable BaseComponent joinSegments(@NotNull List<Object> segments) {
    BaseComponent root = null;
    BaseComponent lastComponent = null;

    for (Object segment : segments) {
      BaseComponent nextComponent;

      if (segment instanceof String string) {
        // Legacy colored text.
        nextComponent = TextComponent.fromLegacy(string);

        // If this is the first segment, keep track of it - it is the one that needs to be sent.
        if (root == null) {
          root = nextComponent;
        }

        // If there is a previous component, make this one a child of it to preserve formatting.
        if (lastComponent != null) {
          lastComponent.addExtra(nextComponent);
        }

        // Update the new final component.
        lastComponent = nextComponent;
        List<BaseComponent> extra = lastComponent.getExtra();
        while (extra != null && !extra.isEmpty()) {
          lastComponent = extra.getLast();
          extra = lastComponent.getExtra();
        }
      } else if (segment instanceof BaseComponent baseComponent) {
        // Modern component.
        nextComponent = baseComponent;

        // If we're starting with a component, use a blank component as the root.
        // This prevents hover and click events from applying to the whole message.
        if (root == null) {
          root = new TextComponent();
          lastComponent = root;
        }

        // If there is a previous component, make this one a child of it to preserve formatting.
        // Do not consider this component to be the new final component - see root above.
        lastComponent.addExtra(nextComponent);
      } else {
        // Shouldn't be possible.
        JavaPlugin.getProvidingPlugin(Lang.class).getLogger()
            .warning("Unknown message segment type " + segment.getClass());
      }
    }

    return root;
  }

  /**
   * Get a name for a UUID.
   *
   * <p>If the UUID is null, the name is the configured administrator name message.
   *
   * @param uuid the UUID
   * @return the name for the UUID
   * @see #getName(PlayerProfile)
   */
  public static @NotNull String getName(@Nullable UUID uuid) {
    if (uuid == null) {
      return get(CommonValues.ADMIN);
    }

    return getName(PROFILES.getUnchecked(uuid));
  }

  /**
   * Get the value for a {@link ConfigRequired}.
   *
   * <p>Because the values are used in replacement, they are never null or empty.
   *
   * @param message the ComponentMessage
   * @return the value set or the default if unset
   */
  public static @NotNull String get(@NotNull ConfigRequired message) {
    String value = LANG.getString(message.getKey(), null);
    return value != null && !value.isBlank() ? value : message.getDefault();
  }

  /**
   * Get a name for an OfflinePlayer.
   *
   * <p>If the player has been removed from the user cache, the name will be the configured unnamed
   * player message.
   *
   * @param offlinePlayer the player
   * @return the name of the player
   */
  public static @NotNull String getName(@NotNull PlayerProfile offlinePlayer) {
    String name = offlinePlayer.getName();

    if (name != null) {
      return name;
    }

    return get(CommonValues.UNNAMED_PLAYER).replace("$uuid",
        Objects.requireNonNull(offlinePlayer.getUniqueId()).toString());
  }

}
