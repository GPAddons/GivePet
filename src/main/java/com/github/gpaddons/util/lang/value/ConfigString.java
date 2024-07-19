package com.github.gpaddons.util.lang.value;

import org.jetbrains.annotations.NotNull;

/**
 * Interface representing a customizable message.
 */
public interface ConfigString {

  /**
   * Get the configuration key of the message.
   *
   * @return the configuration key
   */
  @NotNull
  String getKey();

  /**
   * Get the default message value.
   *
   * @return the default value
   */
  @NotNull
  String getDefault();

}
