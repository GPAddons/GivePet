package com.github.gpaddons.givepet.lang;

import com.github.gpaddons.util.lang.Lang;
import com.github.gpaddons.util.lang.replacement.ComponentReplacement;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentTameable implements ComponentReplacement {

  private static final Pattern PATTERN = Pattern.compile("\\$tamed");

  private final @NotNull Tameable tameable;

  public ComponentTameable(@NotNull Tameable tameable) {
    this.tameable = tameable;
  }

  @Override
  public Pattern getPattern() {
    return PATTERN;
  }

  @Override
  public BaseComponent getReplacement() {
    BaseComponent component;
    String customName = tameable.getCustomName();
    if (customName != null) {
      component = TextComponent.fromLegacy(customName);
    } else {
      component = new TranslatableComponent(tameable.getType().getTranslationKey());
    }
    component.setHoverEvent(getHover(customName));
    return component;
  }

  private HoverEvent getHover(@Nullable String customName) {

    BaseComponent type = new TranslatableComponent(tameable.getType().getTranslationKey());
    if (customName != null) {
      type = new TranslatableComponent(
          "commands.list.nameAndId", // %s (%s)
          TextComponent.fromLegacy(customName),
          type);
    }

    HoverEvent hover = new HoverEvent(Action.SHOW_TEXT, new Text(type));

    AnimalTamer owner = tameable.getOwner();
    if (owner instanceof OfflinePlayer player) {
      hover.addContent(new Text(Lang.getName(player.getPlayerProfile())));
    }

    return hover;
  }
}
