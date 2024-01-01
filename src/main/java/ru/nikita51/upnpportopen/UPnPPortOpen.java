package ru.nikita51.upnpportopen;

import com.dosse.upnp.UPnP;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class UPnPPortOpen extends JavaPlugin {

    private int port = 25565;
    private static final int LEAST_DURATION = 3720;
    private static final int TASK_PERIOD = 3600;

    @Override
    public void onEnable() {
        int taskPeriodTicks = TASK_PERIOD * 20;
        port = getServer().getPort();
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            String message;
            String externalIp = UPnP.getExternalIP() + (port == 25565 ? "" : ":" + port);
            if (UPnP.isMappedTCP(port) || UPnP.openPortTCP(port, "MinecraftServer(" + port + ")", LEAST_DURATION)) {
                message = "Порт был успешно открыт, скинь ip друзьям: " + externalIp;
            } else {
                message = "К сожалению не удалось открыть порт, попробуйте открыть его вручную, ваш ip:" + externalIp;
            }
            StringBuilder selectMessage = new StringBuilder();
            for (int i = 0; i <= message.length(); i++) {
                selectMessage.append('#');
            }
            getLogger().info("\n" + selectMessage + "\n\n" + message + "\n\n" + selectMessage);
            Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                    () -> UPnP.openPortTCP(port, "MinecraftServer(" + port + ")", LEAST_DURATION),
                    taskPeriodTicks,
                    taskPeriodTicks
            );
        }, 1);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler()
                .getPendingTasks()
                .stream()
                .filter(bukkitTask -> bukkitTask.getOwner() == this)
                .forEach(BukkitTask::cancel);
        UPnP.closePortTCP(port);
    }
}
