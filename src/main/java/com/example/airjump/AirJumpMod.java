package com.example.airjump;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class AirJumpMod implements ModInitializer, ClientModInitializer {
    private static boolean airJumpEnabled = false;
    private static KeyBinding airJumpKey;

    @Override
    public void onInitialize() {
        // 空实现，保留接口
    }

    @Override
    public void onInitializeClient() {
        // 注册按键绑定（默认使用J键）
        airJumpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.airjump.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.airjump.keys"
        ));

        // 监听客户端tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 检查按键是否按下
            while (airJumpKey.wasPressed()) {
                airJumpEnabled = !airJumpEnabled;
                if (client.player != null) {
                    MutableText message = Text.translatable("message.airjump.toggle");
                    MutableText status = airJumpEnabled
                            ? Text.literal("ON").formatted(Formatting.GREEN)
                            : Text.literal("OFF").formatted(Formatting.RED);

                    client.player.sendMessage(message.append(" ").append(status), true);
                }
            }

            // 空中跳跃逻辑
            if (airJumpEnabled && client.player != null &&
                    client.options.jumpKey.isPressed() && !client.player.isOnGround() &&
                    !client.player.isCreative() && !client.player.isSpectator()) {

                if (client.player.getVelocity().y < 0) {
                    client.player.jump();
                }
            }
        });
    }

    public static boolean isAirJumpEnabled() {
        return airJumpEnabled;
    }
}