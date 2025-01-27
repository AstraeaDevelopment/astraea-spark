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
import me.lucko.spark.common.monitor.ping.PlayerPingProvider;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.serverconfig.ServerConfigProvider;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import net.minecraft.server.MinecraftServer;

import java.util.stream.Stream;

public class LegacyFabricServerSparkPlugin extends LegacyFabricSparkPlugin implements Command<ServerCommandSource>, SuggestionProvider<ServerCommandSource> {

    public static LegacyFabricServerSparkPlugin register(LegacyFabricSparkMod mod, MinecraftServer server) {
        LegacyFabricServerSparkPlugin plugin = new LegacyFabricServerSparkPlugin(mod, server);
        plugin.enable();
        return plugin;
    }

    private final MinecraftServer server;
    private final ThreadDumper gameThreadDumper;

    public LegacyFabricServerSparkPlugin(LegacyFabricSparkMod mod, MinecraftServer server) {
        super(mod);
        this.server = server;
        this.gameThreadDumper = new ThreadDumper.Specific(server.getThread());
    }

    @Override
    public void enable() {
        super.enable();

        // register commands
        registerCommands(this.server.getCommandManager().getDispatcher());
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerCommands(dispatcher, this, this, "spark");
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String[] args = processArgs(context, false, "/spark", "spark");
        if (args == null) {
            return 0;
        }

        this.platform.executeCommand(new LegacyFabricServerCommandSender(context.getSource()), args);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String[] args = processArgs(context, true, "/spark", "spark");
        if (args == null) {
            return Suggestions.empty();
        }

        return generateSuggestions(new LegacyFabricServerCommandSender(context.getSource()), args, builder);
    }

    @Override
    public Stream<LegacyFabricServerCommandSender> getCommandSenders() {
        return Stream.concat(
                this.server.getPlayerManager().getPlayerList().stream().map(ServerPlayerEntity::getCommandSource),
                Stream.of(this.server.getCommandSource())
        ).map(LegacyFabricServerCommandSender::new);
    }

    @Override
    public void executeSync(Runnable task) {
        this.server.submit(task);
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return this.gameThreadDumper;
    }

    @Override
    public TickHook createTickHook() {
        return new LegacyFabricTickHook.Server();
    }

    @Override
    public TickReporter createTickReporter() {
        return new LegacyFabricTickReporter.Server();
    }

    @Override
    public PlayerPingProvider createPlayerPingProvider() {
        return new LegacyFabricPlayerPingProvider(this.server);
    }

    @Override
    public ServerConfigProvider createServerConfigProvider() {
        return new LegacyFabricServerConfigProvider();
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new LegacyFabricWorldInfoProvider.Server(this.server);
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new LegacyFabricPlatformInfo(PlatformInfo.Type.SERVER);
    }

    @Override
    public String getCommandName() {
        return "spark";
    }
}
