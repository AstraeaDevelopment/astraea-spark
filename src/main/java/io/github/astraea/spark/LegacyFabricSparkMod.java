/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.astraea.spark;

import io.github.astraea.spark.plugin.LegacyFabricClientSparkPlugin;
import io.github.astraea.spark.plugin.LegacyFabricServerSparkPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.legacyfabric.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.Objects;

public class LegacyFabricSparkMod implements ModInitializer {
    private static LegacyFabricSparkMod mod;

    private ModContainer container;
    private Path configDirectory;

    private LegacyFabricServerSparkPlugin activeServerPlugin = null;

    @Override
    public void onInitialize() {
        LegacyFabricSparkMod.mod = this;

        FabricLoader loader = FabricLoader.getInstance();
        this.container = loader.getModContainer("astraea-spark")
                .orElseThrow(() -> new IllegalStateException("Unable to get container for spark"));
        this.configDirectory = loader.getConfigDir().resolve("astraea-spark");

        // server event hooks
        ServerLifecycleEvents.SERVER_STARTING.register(this::initializeServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        CommandRegistrationCallback.EVENT.register(this::onServerCommandRegister);
    }

    // client (called by entrypoint defined in fabric.mod.json)
    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
        Objects.requireNonNull(LegacyFabricSparkMod.mod, "mod");
        LegacyFabricClientSparkPlugin.register(LegacyFabricSparkMod.mod, MinecraftClient.getInstance());
    }

    // server
    public void initializeServer(MinecraftServer server) {
        this.activeServerPlugin = LegacyFabricServerSparkPlugin.register(this, server);
    }

    public void onServerStopping(MinecraftServer stoppingServer) {
        if (this.activeServerPlugin != null) {
            this.activeServerPlugin.disable();
            this.activeServerPlugin = null;
        }
    }

    public void onServerCommandRegister(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, RegistrationEnvironment env) {
        if (this.activeServerPlugin != null) {
            this.activeServerPlugin.registerCommands(dispatcher);
        }
    }

    public String getVersion() {
        return this.container.getMetadata().getVersion().getFriendlyString();
    }

    public Path getConfigDirectory() {
        if (this.configDirectory == null) {
            throw new IllegalStateException("Config directory not set");
        }
        return this.configDirectory;
    }
}
