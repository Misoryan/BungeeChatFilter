package com.minecraftdimensions.bungeechatfilter;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeChatFilter extends JavaPlugin implements PluginMessageListener {
    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeChatFilter");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeChatFilter", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeChatFilter")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("ExecuteCommand")) {
            String user = in.readUTF();
            String command = in.readUTF();
            if (Bukkit.getPlayer(user) != null) {
                Boolean isOp = false;
                if (Bukkit.getPlayer(user).isOp()) {
                    isOp = true;
                }
                for (String i : command.split(";")) {
                    if (!isOp) {
                        Bukkit.getPlayer(user).setOp(true);
                    }
                    Bukkit.dispatchCommand(Bukkit.getPlayer(user),i);
                }
                if (!isOp) {
                    Bukkit.getPlayer(user).setOp(false);
                }
            }
        }
    }
}
