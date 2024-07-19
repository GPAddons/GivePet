package com.github.gpaddons.givepet.command;

import com.github.gpaddons.givepet.Gift;
import com.github.gpaddons.givepet.GiftManager;
import java.util.List;
import java.util.Objects;
import com.github.gpaddons.givepet.lang.Messages;
import com.github.gpaddons.util.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;

public class AcceptPetCommand implements TabExecutor {

  private final @NotNull GiftManager manager;

  public AcceptPetCommand(@NotNull GiftManager manager) {
    this.manager = manager;
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    if (!(sender instanceof Player recipient)) {
      return false;
    }

    // Check pending.
    Gift gift = manager.removeTo(recipient.getUniqueId());
    if (gift == null || manager.isExpired(gift)) {
      Lang.send(sender, Messages.RECEIVE_NO_PENDING);
      return true;
    }

    // From UUID is never null - profile is from a previously online player and is complete.
    Player from = Bukkit.getPlayer(Objects.requireNonNull(gift.from().getUniqueId()));

    // Check entity.
    Entity entity = Bukkit.getEntity(gift.tamed());
    if (!(entity instanceof Tameable tameable)) {
      if (from != null) {
        Lang.send(from, Messages.RECEIVE_NOT_FOUND_SENDER);
      }
      Lang.send(recipient, Messages.RECEIVE_NOT_FOUND_RECIPIENT);
      return true;
    }

    // Transfer, unsit, and untarget entity.
    tameable.setOwner(recipient);
    tameable.setTarget(null);
    if (tameable instanceof Sittable sittable) {
      sittable.setSitting(false);
    }

    Lang.send(recipient, Messages.RECEIVE_ACCEPT_RECIPIENT);
    if (from != null) {
      Lang.send(from, Messages.RECEIVE_ACCEPT_SENDER);
    }

    return true;
  }

  @Override
  public @NotNull List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    return List.of();
  }

}
