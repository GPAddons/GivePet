package com.github.gpaddons.givepet.command;

import com.github.gpaddons.givepet.Gift;
import com.github.gpaddons.givepet.GiftManager;
import java.util.ArrayList;
import java.util.List;
import com.github.gpaddons.givepet.lang.ComponentCommand;
import com.github.gpaddons.givepet.lang.Messages;
import com.github.gpaddons.givepet.lang.ComponentTameable;
import com.github.gpaddons.util.lang.Lang;
import com.github.gpaddons.util.lang.replacement.TextReplacer;
import com.github.gpaddons.util.lang.replacement.TextReplacerOwner;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class GivePetCommand implements TabExecutor {

  private static final ComponentCommand ACCEPTPET = new ComponentCommand("/acceptpet");
  private static final ComponentCommand DECLINEPET = new ComponentCommand("/declinepet");

  private final @NotNull GiftManager manager;

  public GivePetCommand(@NotNull GiftManager manager) {
    this.manager = manager;
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    if (!(sender instanceof Player senderPlayer) || args.length < 1) {
      return false;
    }

    // Check for pending gifts from sender.
    Gift pendingGift = manager.getActiveFrom(senderPlayer.getUniqueId());
    if (pendingGift != null) {
      Lang.send(
          sender,
          Messages.SEND_PENDING_FROM,
          new TextReplacerOwner("recipient", pendingGift.to()));
      return true;
    }

    // Try to locate transfer target.
    Location eyeLocation = senderPlayer.getEyeLocation();
    RayTraceResult traceResult = senderPlayer.getWorld().rayTraceEntities(
        eyeLocation,
        eyeLocation.getDirection(),
        5.0,
        Tameable.class::isInstance);
    if (traceResult == null
        || !(traceResult.getHitEntity() instanceof Tameable tameable)
        || !tameable.isTamed()) {
      Lang.send(sender, Messages.SEND_TARGET_TAMEABLE);
      return true;
    }

    PlayerData senderData = GriefPrevention.instance.dataStore.getPlayerData(
        senderPlayer.getUniqueId());
    if (!senderData.ignoreClaims && !senderPlayer.equals(tameable.getOwner())) {
      Lang.send(sender, Messages.SEND_TARGET_TAMEABLE);
      return true;
    }

    // Determine recipient.
    List<Player> targets = Bukkit.matchPlayer(args[0]);
    Player recipient = null;
    for (Player target : targets) {
      if (!senderPlayer.equals(target)
          && senderPlayer.canSee(target)
          && !senderData.ignoredPlayers.containsKey(target.getUniqueId())
          && !GriefPrevention.instance.dataStore.getPlayerData(
          target.getUniqueId()).ignoredPlayers.containsKey(senderPlayer.getUniqueId())) {
        recipient = target;
        break;
      }
    }
    if (recipient == null) {
      Lang.send(sender, Messages.SEND_NO_RECIPIENT);
      return true;
    }

    // Check for active pending gifts to recipient. If an expired gift exists, we will clobber it.
    pendingGift = manager.getActiveTo(recipient.getUniqueId());
    if (pendingGift != null) {
      Lang.send(
          sender,
          Messages.SEND_PENDING_TO,
          new TextReplacerOwner("recipient", recipient.getPlayerProfile()));
      return true;
    }

    manager.addGift(
        senderPlayer.getPlayerProfile(),
        recipient.getPlayerProfile(),
        tameable.getUniqueId());

    // Remove the gift's target, if any, and make it sit to keep it safer.
    tameable.setTarget(null);
    if (tameable instanceof Sittable sittable) {
      sittable.setSitting(true);
    }

    ComponentTameable componentTameable = new ComponentTameable(tameable);
    Lang.send(
        recipient,
        Messages.SEND_OFFER,
        new TextReplacer[]{ new TextReplacerOwner(senderPlayer.getPlayerProfile()) },
        componentTameable,
        ACCEPTPET,
        DECLINEPET);
    Lang.send(
        sender,
        Messages.SEND_OFFERED,
        new TextReplacer[]{ new TextReplacerOwner("recipient", recipient.getPlayerProfile()) },
        componentTameable);

    return true;
  }

  @Override
  public @NotNull List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    if (args.length != 1 || !(sender instanceof Player senderPlayer)) {
      return List.of();
    }

    List<String> completions = new ArrayList<>();
    String completing = args[0];
    PlayerData senderData = GriefPrevention.instance.dataStore.getPlayerData(
        senderPlayer.getUniqueId());

    for (Player player : Bukkit.getOnlinePlayers()) {
      // Don't suggest self, vanished players, or ignored players.
      if (senderPlayer.equals(player)
          || !senderPlayer.canSee(player)
          || senderData.ignoredPlayers.containsKey(player.getUniqueId())) {
        continue;
      }
      // Ensure name starts with section being completed.
      String name = player.getName();
      if (!StringUtil.startsWithIgnoreCase(name, completing)) {
        continue;
      }
      // Finally, if the potential recipient is ignoring giver, don't suggest them.
      PlayerData targetData = GriefPrevention.instance.dataStore.getPlayerData(
          player.getUniqueId());
      if (targetData.ignoredPlayers.containsKey(senderPlayer.getUniqueId())) {
        continue;
      }

      completions.add(name);
    }

    return completions;
  }

}
