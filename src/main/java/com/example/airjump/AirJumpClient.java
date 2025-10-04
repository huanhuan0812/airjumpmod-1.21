package com.example.airjump;

import com.example.airjump.network.AirJumpStatusPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class AirJumpClient implements ClientModInitializer {
    private static boolean airJumpEnabled = false;
    private static boolean serverAllowsAirJump = false;
    private static KeyBinding airJumpKey;

    @Override
    public void onInitializeClient() {
        // 注册按键绑定（默认使用J键）
        airJumpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.airjump.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.airjump.keys"
        ));

        // 注册网络接收器
        ClientPlayNetworking.registerGlobalReceiver(
            AirJumpStatusPacket.TYPE,
            (client, handler, buf, responseSender) -> {
                AirJumpStatusPacket packet = new AirJumpStatusPacket(buf);
                serverAllowsAirJump = packet.isAllowed();
                
                client.execute(() -> {
                    if (serverAllowsAirJump) {
                        client.player.sendMessage(
                            Text.translatable("text.airjump.enabled").formatted(Formatting.GREEN), 
                            true
                        );
                    } else {
                        client.player.sendMessage(
                            Text.translatable("text.airjump.disabled").formatted(Formatting.RED), 
                            true
                        );
                        airJumpEnabled = false; // 禁用airjump
                    }
                });
            }
        );

        // 监听客户端tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 检查按键是否按下
            while (airJumpKey.wasPressed()) {
                if (!serverAllowsAirJump) {
                    if (client.player != null) {
                        client.player.sendMessage(
                            Text.translatable("text.warn.unable").formatted(Formatting.RED), 
                            true
                        );
                    }
                    continue;
                }
                
                airJumpEnabled = !airJumpEnabled;
                if (client.player != null) {
                    MutableText message = Text.translatable("message.airjump.toggle");
                    MutableText status = airJumpEnabled
                            ? Text.literal("ON").formatted(Formatting.GREEN)
                            : Text.literal("OFF").formatted(Formatting.RED);

                    client.player.sendMessage(message.append(" ").append(status), true);
                    
                    // 向服务器发送状态更新
                    AirJumpStatusPacket packet = new AirJumpStatusPacket(serverAllowsAirJump, airJumpEnabled);
                    ClientPlayNetworking.send(packet);
                }
            }

            // 空中跳跃逻辑
            if (airJumpEnabled && client.player != null && 
                    client.options.jumpKey.isPressed() && 
                    !client.player.isOnGround() &&
                    !client.player.isCreative() && 
                    !client.player.isSpectator() &&
                    serverAllowsAirJump) {

                if (client.player.getVelocity().y <= 0 && client.player.fallDistance > 0.1f) {
                    client.player.jump();
                }
            }
        });
    }

    public static boolean isAirJumpEnabled() {
        return airJumpEnabled;
    }
    
    public static boolean isServerAllowsAirJump() {
        return serverAllowsAirJump;
    }
}