package com.github.gpaddons.givepet;

import com.github.gpaddons.givepet.command.AcceptPetCommand;
import com.github.gpaddons.givepet.command.DeclinePetCommand;
import com.github.gpaddons.givepet.command.GivePetCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class GivePet extends JavaPlugin {

  @Override
  public void onEnable() {
    boolean componentsAvailable;
    try {
      Class.forName("net.md_5.bungee.api.ChatColor");
      componentsAvailable = true;
    } catch (ClassNotFoundException ignored) {
      componentsAvailable = false;
    }

    if (!componentsAvailable) {
      getLogger().severe(
          "GivePet requires Spigot or a Spigot fork for access to modern component messages!");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    GiftManager manager = new GiftManager();
    registerCommand("givepet", new GivePetCommand(manager));
    registerCommand("acceptpet", new AcceptPetCommand(manager));
    registerCommand("declinepet", new DeclinePetCommand(manager));
  }

  private void registerCommand(String name, TabExecutor executor) {
    PluginCommand command = getCommand(name);
    if (command == null) {
      throw new IllegalStateException("No command " + name + " available!");
    }
    command.setExecutor(executor);
  }

}
