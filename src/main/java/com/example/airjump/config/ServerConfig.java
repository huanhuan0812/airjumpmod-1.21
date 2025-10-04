package com.example.airjump.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ServerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("AirJumpConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("airjump-server.json");
    
    private Set<String> allowedServers;
    private Set<String> allowedPlayers;
    private boolean globalEnabled;
    private int maxJumpsPerSecond;
    private boolean allowOnAllServers;
    
    public ServerConfig() {
        this.allowedServers = new HashSet<>();
        this.allowedPlayers = new HashSet<>();
        this.globalEnabled = true;
        this.maxJumpsPerSecond = 5;
        this.allowOnAllServers = false;
    }
    
    public static ServerConfig load() {
        ServerConfig config = new ServerConfig();
        
        if (Files.exists(CONFIG_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                config.fromJson(json);
                LOGGER.info("Loaded AirJump server config from {}", CONFIG_PATH);
            } catch (Exception e) {
                LOGGER.error("Failed to load config, using defaults: {}", e.getMessage());
                config.save(); // 保存默认配置
            }
        } else {
            // 创建默认配置文件
            config.save();
            LOGGER.info("Created default AirJump server config at {}", CONFIG_PATH);
        }
        
        return config;
    }
    
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
                writer.write(GSON.toJson(this.toJson()));
                LOGGER.info("Saved AirJump server config to {}", CONFIG_PATH);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }
    
    private JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("global_enabled", globalEnabled);
        json.addProperty("max_jumps_per_second", maxJumpsPerSecond);
        json.addProperty("allow_on_all_servers", allowOnAllServers);
        
        // 服务器列表
        JsonObject servers = new JsonObject();
        for (String server : allowedServers) {
            servers.addProperty(server, true);
        }
        json.add("allowed_servers", servers);
        
        // 玩家列表
        JsonObject players = new JsonObject();
        for (String player : allowedPlayers) {
            players.addProperty(player, true);
        }
        json.add("allowed_players", players);
        
        return json;
    }
    
    private void fromJson(JsonObject json) {
        this.globalEnabled = json.has("global_enabled") ? 
            json.get("global_enabled").getAsBoolean() : true;
        this.maxJumpsPerSecond = json.has("max_jumps_per_second") ? 
            json.get("max_jumps_per_second").getAsInt() : 5;
        this.allowOnAllServers = json.has("allow_on_all_servers") ? 
            json.get("allow_on_all_servers").getAsBoolean() : false;
        
        this.allowedServers.clear();
        if (json.has("allowed_servers")) {
            JsonObject servers = json.getAsJsonObject("allowed_servers");
            for (String server : servers.keySet()) {
                this.allowedServers.add(server);
            }
        }
        
        this.allowedPlayers.clear();
        if (json.has("allowed_players")) {
            JsonObject players = json.getAsJsonObject("allowed_players");
            for (String player : players.keySet()) {
                this.allowedPlayers.add(player);
            }
        }
    }
    
    // Getter 方法
    public boolean isGlobalEnabled() { return globalEnabled; }
    public int getMaxJumpsPerSecond() { return maxJumpsPerSecond; }
    public boolean isAllowOnAllServers() { return allowOnAllServers; }
    public Set<String> getAllowedServers() { return new HashSet<>(allowedServers); }
    public Set<String> getAllowedPlayers() { return new HashSet<>(allowedPlayers); }
    
    public boolean isServerAllowed(String serverAddress) {
        return allowOnAllServers || allowedServers.contains(serverAddress.toLowerCase()) || allowedServers.contains("*");
    }
    
    public boolean isPlayerAllowed(String playerName) {
        return allowedPlayers.contains(playerName) || allowedPlayers.contains("*");
    }
}
