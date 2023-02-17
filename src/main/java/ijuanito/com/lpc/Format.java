package ijuanito.com.lpc;

import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Format {
    private TextComponent chat = new TextComponent();
    private Player p;
    private String message;
    public Format(Player p, String message) {
        this.p = p;
        this.message = message;
        initialize();
    }
    public TextComponent create(Player p) {
        String format = getFormat(p);
        BaseComponent[] arrayOfBaseComponent = TextComponent.fromLegacyText(format);
        for (BaseComponent comp : arrayOfBaseComponent) {
            this.chat.addExtra(comp);
        }
        return this.chat;
    }
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public String getFormat(Player player) {
        final CachedMetaData metaData = Main.luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        final String group = metaData.getPrimaryGroup();
        String format = Main.instance.getConfig().getString(Main.instance.getConfig().getString("group-formats." + group) != null ? "group-formats." + group : "chat-format")
                .replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("{prefixes}", metaData.getPrefixes().keySet().stream().map(key -> metaData.getPrefixes().get(key)).collect(Collectors.joining()))
                .replace("{suffixes}", metaData.getSuffixes().keySet().stream().map(key -> metaData.getSuffixes().get(key)).collect(Collectors.joining()))
                .replace("{world}", player.getWorld().getName())
                .replace("{name}", player.getName())
                .replace("{displayname}", player.getDisplayName())
                .replace("{username-color}", metaData.getMetaValue("username-color") != null ? metaData.getMetaValue("username-color") : "")
                .replace("{message-color}", metaData.getMetaValue("message-color") != null ? metaData.getMetaValue("message-color") : "");

        format = colorize(translateHexColorCodes(Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") ? PlaceholderAPI.setPlaceholders(player, format) : format));
        return format.replace("{message}", player.hasPermission("lpc.colorcodes") && player.hasPermission("lpc.rgbcodes")
                ? colorize(translateHexColorCodes(message)) : player.hasPermission("lpc.colorcodes") ? colorize(message) : player.hasPermission("lpc.rgbcodes")
                ? translateHexColorCodes(message) : message).replace("%", "%%");
    }
    public static String colorize(final String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }
    static String translateHexColorCodes(final String message) {
        final char colorChar = org.bukkit.ChatColor.COLOR_CHAR;
        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            final String group = matcher.group(1);
            matcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }
        return matcher.appendTail(buffer).toString();
    }

    private void initialize() {
        setHoverEvent();
        setClickEvent();
        setSuggestCommand();
    }
    private void setHoverEvent() {
        String title = setPAPI(this.p, Main.instance.getConfig().getString("Title"));
        String hovertext = setPAPI(this.p, Main.instance.getConfig().getString("HoverText"));
        this.chat.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(setMainVariables(title))).append("\n" + setMainVariables(hovertext)).create()));
    }
    public static String setPAPI(Player p, String s) {
        return PlaceholderAPI.setPlaceholders(p, s);
    }
    private String setMainVariables(String s) {
        final CachedMetaData metaData = Main.luckPerms.getPlayerAdapter(Player.class).getMetaData(p);

        String format = s.replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("{prefixes}", metaData.getPrefixes().keySet().stream().map(key -> metaData.getPrefixes().get(key)).collect(Collectors.joining()))
                .replace("{suffixes}", metaData.getSuffixes().keySet().stream().map(key -> metaData.getSuffixes().get(key)).collect(Collectors.joining()))
                .replace("{world}", p.getWorld().getName())
                .replace("{name}", p.getName())
                .replace("{displayname}", p.getDisplayName())
                .replace("{username-color}", metaData.getMetaValue("username-color") != null ? metaData.getMetaValue("username-color") : "")
                .replace("{message-color}", metaData.getMetaValue("message-color") != null ? metaData.getMetaValue("message-color") : "");

        format = colorize(translateHexColorCodes(Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") ? PlaceholderAPI.setPlaceholders(p, format) : format));
        return format.replace("%", "%%");
    }
    private void setClickEvent() {
        if (!Main.instance.getConfig().getBoolean("PerformCommand"))
            return;
        this.chat.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, setMainVariables(Main.instance.getConfig().getString("Command"))));
    }
    private void setSuggestCommand() {
        if (!Main.instance.getConfig().getBoolean("SuggestCommand"))
            return;
        this.chat.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, setMainVariables(Main.instance.getConfig().getString("Command"))));
    }
}
