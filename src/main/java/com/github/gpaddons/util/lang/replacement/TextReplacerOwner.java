package com.github.gpaddons.util.lang.replacement;

import com.github.gpaddons.util.lang.Lang;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * MessageReplacement providing variables representing the owner of an object. The owner may be
 * null, indicating administrative ownership.
 *
 * <p>Supports $Id and $Name.
 *
 * <p>By default, values are prefixed with "owner" (i.e. $ownerId, $ownerName) but may have
 * differing prefixes to support multiple replacements in a single string (i.e. $previousOwnerId,
 * $nextOwnerName)
 */
public class TextReplacerOwner implements TextReplacer {

  private final @NotNull String replaceId;
  private final @NotNull String replaceName;
  private final String uuidVal;
  private final @NotNull String nameVal;

  public TextReplacerOwner(@NotNull PlayerProfile player) {
    this("owner", player);
  }

  public TextReplacerOwner(@NotNull String prefix, @NotNull PlayerProfile player) {
    this(prefix, player.getUniqueId(), player);
  }

  private TextReplacerOwner(
      @NotNull String prefix,
      @Nullable UUID uuid,
      @Nullable PlayerProfile player) {
    if (prefix.isEmpty()) {
      throw new IllegalArgumentException("Prefix may not be empty.");
    }

    this.replaceId = '$' + prefix + "Id";
    this.replaceName = '$' + prefix + "Name";
    this.uuidVal = Objects.requireNonNullElseGet(uuid, () -> new UUID(0, 0)).toString();
    this.nameVal = player == null ? Lang.getName(uuid) : Lang.getName(player);
  }

  public TextReplacerOwner(@Nullable UUID uuid) {
    this("owner", uuid);
  }

  public TextReplacerOwner(@NotNull String prefix, @Nullable UUID uuid) {
    this(prefix, uuid, null);
  }

  @Override
  public @NotNull String replace(@NotNull String value) {
    value = value.replace(replaceId, uuidVal);
    value = value.replace(replaceName, nameVal);
    return value;
  }

}
