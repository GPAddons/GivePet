package com.github.gpaddons.util.lang.value;

import org.jetbrains.annotations.NotNull;

/**
 * An enum for common {@link ConfigRequired ConfigReplacements}.
 */
public enum CommonValues implements ConfigRequired {

  ADMIN("general.admin", "an administrator"),
  UNNAMED_PLAYER("general.unnamed_player", "someone ($uuid)");

  private final String key;
  private final String defaultVal;

  CommonValues(String key, String defaultVal) {
    this.key = key;
    this.defaultVal = defaultVal;
  }

  @Override
  public @NotNull String getKey() {
    return this.key;
  }

  @Override
  public @NotNull String getDefault() {
    return defaultVal;
  }

}
