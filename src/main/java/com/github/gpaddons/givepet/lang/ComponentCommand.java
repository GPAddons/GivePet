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

public class ComponentCommand implements ComponentReplacement {

  private final Pattern pattern;
  private final BaseComponent component;

  public ComponentCommand(String name) {
    this(name, name);
  }

  public ComponentCommand(String name, String execution) {
    pattern = Pattern.compile("\\$" + name);
    component = new TextComponent(name);
    // Hover: "Left click to activate"
    component.setHoverEvent(new HoverEvent(
        Action.SHOW_TEXT,
        new Text(new TranslatableComponent("narration.button.usage.hovered")),
        new Text("\n" + execution)));
    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, execution));
  }

  @Override
  public Pattern getPattern() {
    return pattern;
  }

  @Override
  public BaseComponent getReplacement() {
    return component;
  }

}
