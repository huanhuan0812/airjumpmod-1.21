package com.example.airjump;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.Objects;


@Environment(EnvType.CLIENT)
public class getServerAdress implements ClientModInitializer {
    static boolean isBanned = false;
    String[] bannedServers = {
        "mc.hypixel.net",
    };
    @Override
    public void onInitializeClient() {
        // 监听客户端加入服务器事件
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.isInSingleplayer()) {
                // 如果是单人游戏，直接返回
                return;
            }

            // 获取当前服务器地址
            String serverAddress = Objects.requireNonNull(client.getCurrentServerEntry()).address;

            for (String server : bannedServers) {
                if (serverAddress.contains(server)) {
                    isBanned = true;
                    break;
                }
            }
        });
    }
    public static boolean getIsBanned() {
        return isBanned;
    }
}
