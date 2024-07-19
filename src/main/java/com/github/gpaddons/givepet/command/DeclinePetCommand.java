package com.github.gpaddons.givepet.command;

import com.github.gpaddons.givepet.Gift;
import com.github.gpaddons.givepet.GiftManager;
import com.github.gpaddons.givepet.lang.ComponentCommand;
import com.github.gpaddons.givepet.lang.Messages;
import com.github.gpaddons.util.lang.Lang;
import com.github.gpaddons.util.lang.replacement.TextReplacer;
import com.github.gpaddons.util.lang.replacement.TextReplacerOwner;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeclinePetCommand implements TabExecutor {

  private final @NotNull GiftManager manager;

  public DeclinePetCommand(@NotNull GiftManager manager) {
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
    if (gift == null) {
      Lang.send(sender, Messages.RECEIVE_NO_PENDING);
      return true;
    }

    Lang.send(
        sender,
        Messages.RECEIVE_DECLINE_RECIPIENT,
        new TextReplacer[]{ new TextReplacerOwner(gift.from()) },
        new ComponentCommand("/ignore", "/griefprevention:ignore " + gift.from().getName()));

    if (!manager.isExpired(gift)) {
      Player from = Bukkit.getPlayer(Objects.requireNonNull(gift.from().getUniqueId()));
      if (from != null) {
        Lang.send(from, Messages.RECEIVE_DECLINE_SENDER);
      }
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
