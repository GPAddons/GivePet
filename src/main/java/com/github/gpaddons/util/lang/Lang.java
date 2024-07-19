package com.github.gpaddons.util.lang;

import com.github.gpaddons.util.lang.replacement.ComponentReplacement;
import com.github.gpaddons.util.lang.replacement.TextReplacer;
import com.github.gpaddons.util.lang.value.CommonValues;
import com.github.gpaddons.util.lang.value.ConfigMessage;
import com.github.gpaddons.util.lang.value.ConfigReplacement;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
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

      Files.copy(resource, file.toPath());
    } catch (IOException e) {
      plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
    }
  }

  /**
   * Check if a {@link ConfigMessage} is set to a non-null and non-empty value.
   *
   * @param message the Message
   * @return true if the message has content
   */
  public static boolean isSet(@NotNull ConfigMessage message) {
    String value = get(message);
    return value != null && !value.isEmpty();
  }

  private static @Nullable String get(@NotNull ConfigMessage message) {
    String value = LANG.getString(message.getKey(), message.getDefault());
    return value.isBlank() ? null : value;
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

    if (value == null || value.isEmpty()) {
      return;
    }

    for (TextReplacer replacer : textReplacers) {
      value = replacer.replace(value);
    }

    if (componentReplacements.length == 0) {
      recipient.sendMessage(value);
      return;
    }

    List<Object> segments = new ArrayList<>();
    segments.add(value);

    for (ComponentReplacement replacement : componentReplacements) {
      insertComponents(segments, replacement);
    }

    BaseComponent component = joinSegments(segments);
    if (component != null) {
      recipient.spigot().sendMessage(component);
    }
  }

  private static void insertComponents(
      @NotNull List<Object> segments,
      @NotNull ComponentReplacement replacement) {
    for (int index = 0; index < segments.size(); ++index) {
      Object segment = segments.get(index);
      if (!(segment instanceof String string)) {
        continue;
      }

      Matcher matcher = replacement.getPattern().matcher(string);
      int lastEnd = 0;
      while (matcher.find()) {
        // Since we have a match, we're replacing the segment.
        if (lastEnd == 0) {
          segments.remove(index);
        }

        int start = matcher.start();
        // If there's unmatched text between the previous match and the current one, add it.
        if (start > lastEnd) {
          segments.add(index, string.substring(lastEnd, start));
          ++index;
        }
        lastEnd = matcher.end();

        // Add the component.
        segments.add(index, segment);
        ++index;
      }

      if (lastEnd == string.length()) {
        // If there is no substring after our last match, decrease index to point to our last match.
        --index;
      } else {
        // Otherwise, add remaining text.
        segments.add(index, string.substring(lastEnd));
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
      } else if (segment instanceof BaseComponent baseComponent) {
        // Modern component.
        nextComponent = baseComponent;
      } else {
        // Shouldn't be possible.
        JavaPlugin.getProvidingPlugin(Lang.class).getLogger()
            .warning("Unknown message segment type " + segment.getClass());
        continue;
      }

      // If this is the first segment, keep track of it - it is the one that needs to be sent.
      if (root == null) {
        root = nextComponent;
      }

      // If there is a previous component, make this one a child of it to preserve formatting.
      // This does result in some unnecessary nesting, but otherwise we would have to copy
      // formatting each time, which would inflate JSON size by about the same amount in a
      // best-case scenario - using colors is expected.
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

    // TODO offline cache
    return getName(Bukkit.getOfflinePlayer(uuid).getPlayerProfile());
  }

  /**
   * Get the value for a {@link ConfigReplacement}.
   *
   * <p>Because the values are used in replacement, they are never null or empty.
   *
   * @param message the ComponentMessage
   * @return the value set or the default if unset
   */
  public static @NotNull String get(@NotNull ConfigReplacement message) {
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
