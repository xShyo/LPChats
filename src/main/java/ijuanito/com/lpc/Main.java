package ijuanito.com.lpc;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public final class Main extends JavaPlugin implements Listener {
    PluginDescriptionFile pdfFile = getDescription();
    private final String pluginVersion = pdfFile.getVersion();
    private final List<String> pluginauthor = pdfFile.getAuthors();

    public static LuckPerms luckPerms;

    public static Main instance;
    @Override
    public void onEnable() {
        long l = System.nanoTime();
        luckPerms = getServer().getServicesManager().load(LuckPerms.class);

        instance = this;
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA+"       __   ___");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA+" |    |__) |     "+ChatColor.GREEN+"Chat Format "+ChatColor.AQUA +pluginVersion);
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA+" |___ |    |___  "+ChatColor.GREEN+" By "+ ChatColor.DARK_GRAY+pluginauthor);
        Bukkit.getConsoleSender().sendMessage("");

        saveDefaultConfig();
        Metrics metrics = new Metrics(this, 17534);
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().log(Level.INFO, "Done and enabled in %time%ms"
                .replace("%time%", nanosToMillis(System.nanoTime() - l)));
    }
    private static final DecimalFormat NUMBER_FORMAT_NANO = new DecimalFormat("0.00");

    public static String nanosToMillis(long paramLong) {
        return NUMBER_FORMAT_NANO.format(paramLong / 1000000.0D);
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final String[] args) {
        if (args.length == 1 && "reload".equals(args[0])) {
            reloadConfig();
            sender.sendMessage(Format.colorize("&aLPChat has been reloaded."));
            return true;
        }


        return false;
    }

    @Override
    public List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String alias, final String[] args) {
        if (args.length == 1)
            return Collections.singletonList("reload");

        return new ArrayList<>();
    }
    static Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");


    @EventHandler(priority = EventPriority.HIGHEST)
    public void ComponentBuilder(AsyncPlayerChatEvent e) {
        if (e.isCancelled())
            return;
        Player p = e.getPlayer();
        String msg = e.getMessage();
        TextComponent chat = (new Format(p, msg)).create(p);
        Bukkit.getServer().getLogger().info( (new Format(p, msg)).getFormat(p));
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            if (ess != null) {
                User user = ess.getUser(p);
                User receiverUser = ess.getUser(receiver);
                if (receiverUser.isIgnoredPlayer(user)) {
                    continue;
                }
            }
            receiver.spigot().sendMessage(chat);
        }
        e.setCancelled(true);
    }
}