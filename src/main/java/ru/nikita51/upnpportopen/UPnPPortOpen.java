package ru.nikita51.upnpportopen;

import com.dosse.upnp.PortMappingEntity;
import com.dosse.upnp.UPnP;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class UPnPPortOpen extends JavaPlugin {

    private int port = 25565;
    private static final int LEAST_DURATION = 3600;
    private static final int TASK_PERIOD = 3500;

    @Override
    public void onEnable() {
        int taskPeriodTicks = TASK_PERIOD * 20;
        port = getServer().getPort();
        String description = "MinecraftServer TCP (" + port + ")";
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            String externalIp = UPnP.getExternalIP() + (port == 25565 ? "" : ":" + port);
            PortMappingEntity entity = UPnP.getPortMappingTCP(port);
            if (entity != null && entity.getLeaseDuration() == 0) {
                prettyPrint("Порт был успешно открыт, скинь ip друзьям: " + externalIp);
                return;
            }
            UPnP.closePortTCP(port);
            if (UPnP.openPortTCP(port, description, LEAST_DURATION)) {
                prettyPrint("Порт был успешно открыт, скинь ip друзьям: " + externalIp);
                Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                        () -> {
                            PortMappingEntity e = UPnP.getPortMappingTCP(port);
                            if (e == null || e.getLeaseDuration() != 0) {
                                UPnP.closePortTCP(port);
                                UPnP.openPortTCP(port, description, LEAST_DURATION);
                            }
                        },
                        taskPeriodTicks,
                        taskPeriodTicks
                );
                return;
            }
            prettyPrint("К сожалению не удалось открыть порт, попробуйте открыть его вручную, ваш ip:" + externalIp);
        }, 1);
    }

    public void prettyPrint(String message) {
        StringBuilder selectMessage = new StringBuilder();
        for (int i = 0; i <= message.length(); i++) {
            selectMessage.append('#');
        }
        message = String.format("%n%s%n%n%s%n%n%s", selectMessage, message, selectMessage);
        getLogger().info(message);
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
