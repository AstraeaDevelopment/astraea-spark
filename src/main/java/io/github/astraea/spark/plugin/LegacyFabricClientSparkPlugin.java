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

package io.github.astraea.spark.plugin;

import io.github.astraea.spark.*;
import io.github.astraea.spark.mixin.MinecraftClientAccessor;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import net.legacyfabric.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import java.util.stream.Stream;

public class LegacyFabricClientSparkPlugin extends LegacyFabricSparkPlugin implements Command<FabricClientCommandSource>, SuggestionProvider<FabricClientCommandSource> {

    public static void register(LegacyFabricSparkMod mod, MinecraftClient client) {
        LegacyFabricClientSparkPlugin plugin = new LegacyFabricClientSparkPlugin(mod, client);
        plugin.enable();
    }

    private final MinecraftClient minecraft;
    private final ThreadDumper.GameThread gameThreadDumper;

    public LegacyFabricClientSparkPlugin(LegacyFabricSparkMod mod, MinecraftClient minecraft) {
        super(mod);
        this.minecraft = minecraft;
        this.gameThreadDumper = new ThreadDumper.GameThread(() -> ((MinecraftClientAccessor) minecraft).spark$getCurrentThread());
    }

    @Override
    public void enable() {
        super.enable();

        // events
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onDisable);
        ClientCommandRegistrationCallback.EVENT.register(this::onCommandRegister);
    }

    private void onDisable(MinecraftClient stoppingClient) {
        if (stoppingClient == this.minecraft) {
            disable();
        }
    }

    public void onCommandRegister(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        registerCommands(dispatcher, this, this, "sparkc", "sparkclient");
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String[] args = processArgs(context, false, "sparkc", "sparkclient");
        if (args == null) {
            return 0;
        }

        this.platform.executeCommand(new LegacyFabricClientCommandSender(context.getSource()), args);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String[] args = processArgs(context, true, "/sparkc", "/sparkclient");
        if (args == null) {
            return Suggestions.empty();
        }

        return generateSuggestions(new LegacyFabricClientCommandSender(context.getSource()), args, builder);
    }

    @Override
    public Stream<LegacyFabricClientCommandSender> getCommandSenders() {
        ClientPlayNetworkHandler networkHandler = this.minecraft.getNetworkHandler();
        if (networkHandler == null) {
            return Stream.empty();
        }
        return Stream.of(new LegacyFabricClientCommandSender(networkHandler.getCommandSource()));
    }

    @Override
    public void executeSync(Runnable task) {
        this.minecraft.submit(task);
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return this.gameThreadDumper.get();
    }

    @Override
    public TickHook createTickHook() {
        return new LegacyFabricTickHook.Client();
    }

    @Override
    public TickReporter createTickReporter() {
        return new LegacyFabricTickReporter.Client();
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new LegacyFabricWorldInfoProvider.Client(this.minecraft);
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new LegacyFabricPlatformInfo(PlatformInfo.Type.CLIENT);
    }

    @Override
    public String getCommandName() {
        return "sparkc";
    }

}
