package com.github.gpaddons.util.lang.replacement;

import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;

public interface ComponentReplacement {

  Pattern getPattern();

  BaseComponent getReplacement();

}
