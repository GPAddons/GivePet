package com.github.gpaddons.util.lang.replacement;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for performing variable replacement in a message.
 */
public interface TextReplacer {

  /**
   * Replace variables in the given message.
   *
   * @param value the message
   * @return the modified message
   */
  @NotNull
  String replace(@NotNull String value);

}
