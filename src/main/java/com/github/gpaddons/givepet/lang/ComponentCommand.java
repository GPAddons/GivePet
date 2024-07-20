package com.github.gpaddons.givepet.lang;

import com.github.gpaddons.util.lang.replacement.ComponentReplacement;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;

public class ComponentCommand implements ComponentReplacement {

  private final Pattern pattern;
  private final @NotNull String name;
  private final @NotNull String execution;

  public ComponentCommand(@NotNull String name) {
    this(name, name);
  }

  public ComponentCommand(@NotNull String name, @NotNull String execution) {
    pattern = Pattern.compile("\\$" + name);
    this.name = name;
    this.execution = execution;
  }

  @Override
  public Pattern getPattern() {
    return pattern;
  }

  @Override
  public BaseComponent getReplacement() {
    BaseComponent component = new TextComponent(name);
    // Hover: "Left click to activate"
    component.setHoverEvent(new HoverEvent(
        Action.SHOW_TEXT,
        new Text(new TranslatableComponent("narration.button.usage.hovered")),
        new Text("\n" + execution)));
    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, execution));
    return component;
  }

}
