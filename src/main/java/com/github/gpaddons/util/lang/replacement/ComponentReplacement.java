package com.github.gpaddons.util.lang.replacement;

import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ComponentReplacement {

  @NotNull Pattern getPattern();

  @Contract("-> new")
  @NotNull BaseComponent getReplacement();

}
