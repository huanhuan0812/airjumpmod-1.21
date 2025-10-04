package com.example.airjump;

import com.example.airjump.network.AirJumpStatusPacket;
import com.example.airjump.server.ServerManager;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirJumpServer implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("AirJumpServer");

    @Override
    public void onInitializeServer() {
        LOGGER.info("AirJump Server initialized!");
        
        // 初始化配置
        ServerManager.initialize();
        
        // 注册网络接收器
        ServerPlayNetworking.registerGlobalReceiver(
            AirJumpStatusPacket.TYPE,
            (packet, player, responseSender) -> {
                // 服务器端验证玩家权限
                boolean allowed = ServerManager.isPlayerAllowed(player);
                
                // 发送更新的状态给玩家
                AirJumpStatusPacket response = new AirJumpStatusPacket(allowed, packet.isEnabled());
                ServerPlayNetworking.send(player, response);
            }
        );
        
        // 监听服务器启动事件
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
    }
    
    private void onServerStarted(net.minecraft.server.MinecraftServer server) {
        // 服务器启动时向所有玩家发送状态包
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerManager.sendStatusToPlayer(player);
        }
    }
}