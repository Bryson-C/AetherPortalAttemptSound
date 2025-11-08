package org.bc.aetherNotificationSound;

import org.bukkit.plugin.java.JavaPlugin;

public final class AetherNotificationSound extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new AetherPortalListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
