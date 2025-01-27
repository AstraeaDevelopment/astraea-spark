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

import com.google.common.collect.ImmutableList;
import io.github.astraea.spark.bodge.ChunkPosHelper;
import io.github.astraea.spark.bodge.IGameRuleValue;
import io.github.astraea.spark.mixin.EntityTypeAccessor;
import io.github.astraea.spark.mixin.GameRuleManagerAccessor;
import me.lucko.spark.common.platform.world.AbstractChunkInfo;
import me.lucko.spark.common.platform.world.CountMap;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRuleManager;
import net.minecraft.world.World;

import java.util.*;

public abstract class LegacyFabricWorldInfoProvider implements WorldInfoProvider {

    /*
    protected abstract ResourcePackManager getResourcePackManager();

    @Override
    public Collection<DataPackInfo> pollDataPacks() {
        return getResourcePackManager().getEnabledProfiles().stream()
                .map(pack -> new DataPackInfo(
                        pack.getId(),
                        pack.getDescription().getString(),
                        resourcePackSource(pack.getSource())
                ))
                .collect(Collectors.toList());
    }

    private static String resourcePackSource(ResourcePackSource source) {
        if (source == ResourcePackSource.NONE) {
            return "none";
        } else if (source == ResourcePackSource.BUILTIN) {
            return "builtin";
        } else if (source == ResourcePackSource.WORLD) {
            return "world";
        } else if (source == ResourcePackSource.SERVER) {
            return "server";
        } else {
            return "unknown";
        }
    }
    */

    public static final class Server extends LegacyFabricWorldInfoProvider {
        private final MinecraftServer server;

        public Server(MinecraftServer server) {
            this.server = server;
        }

        @Override
        public CountsResult pollCounts() {
            int players = this.server.getCurrentPlayerCount();
            int entities = 0;
            int chunks = 0;

            for (ServerWorld world : this.server.worlds) {
                entities += world.entities.size();
                chunks += world.chunkCache.getLoadedChunksCount();
            }

            return new CountsResult(players, entities, -1, chunks);
        }

        @Override
        public ChunksResult<FabricChunkInfo> pollChunks() {
            ChunksResult<FabricChunkInfo> data = new ChunksResult<>();

            for (ServerWorld world : this.server.worlds) {
                Map<Long, FabricChunkInfo> worldInfos = new HashMap<>();

                for (Entity entity : world.entities) {
                    FabricChunkInfo info = worldInfos.computeIfAbsent(
                        ChunkPos.getIdFromCoords(entity.chunkX, entity.chunkZ), FabricChunkInfo::new);
                    info.entityCounts.increment(entity.getClass());
                }

                data.put(world.dimension.getName(), ImmutableList.copyOf(worldInfos.values()));
            }

            return data;
        }

        @Override
        public GameRulesResult pollGameRules() {
            GameRulesResult data = new GameRulesResult();

            for (ServerWorld world : this.server.worlds) {
                String worldName = world.dimension.getName();

                for (String gameruleName : world.getGameRules().method_4670()) {
                    GameRuleManager.Value gamerule = ((GameRuleManagerAccessor) world.getGameRules()).spark$getGameRules().get(gameruleName);
                    data.putDefault(gameruleName, ((IGameRuleValue) gamerule).spark$getDefaultValue());

                    data.put(gameruleName, worldName, gamerule.getStringDefaultValue());
                }
            }
            return data;
        }

        @Override
        public Collection<DataPackInfo> pollDataPacks() {
            return null;
        }
    }

    public static final class Client extends LegacyFabricWorldInfoProvider {
        private final MinecraftClient client;

        public Client(MinecraftClient client) {
            this.client = client;
        }

        @Override
        public CountsResult pollCounts() {
            ClientWorld world = this.client.world;
            if (world == null) {
                return null;
            }

            int entities = world.entities.size();
            int chunks = world.getChunkProvider().getLoadedChunksCount();

            return new CountsResult(-1, entities, -1, chunks);
        }

        @Override
        public ChunksResult<FabricChunkInfo> pollChunks() {
            ClientWorld world = this.client.world;
            if (world == null) {
                return null;
            }

            ChunksResult<FabricChunkInfo> data = new ChunksResult<>();

            Map<Long, FabricChunkInfo> worldInfos = new HashMap<>();

            for (Entity entity : world.entities) {
                FabricChunkInfo info = worldInfos.computeIfAbsent(ChunkPos.getIdFromCoords(entity.chunkX, entity.chunkZ), FabricChunkInfo::new);
                info.entityCounts.increment(entity.getClass());
            }

            data.put(world.dimension.getName(), ImmutableList.copyOf(worldInfos.values()));

            return data;
        }

        @Override
        public GameRulesResult pollGameRules() {
            // Not available on client since 24w39a
            // ^ apparently available on client in 1.8.9? todo test
            GameRulesResult data = new GameRulesResult();

            World world = this.client.world;

            String worldName = world.dimension.getName();

            for (String gameruleName : world.getGameRules().method_4670()) {
                GameRuleManager.Value gamerule = ((GameRuleManagerAccessor) world.getGameRules()).spark$getGameRules().get(gameruleName);
                data.putDefault(gameruleName, ((IGameRuleValue) gamerule).spark$getDefaultValue());

                data.put(gameruleName, worldName, gamerule.getStringDefaultValue());
            }

            return data;
        }

        @Override
        public Collection<DataPackInfo> pollDataPacks() {
            this.client.getResourcePackLoader().getSelectedResourcePacks(); //todo handle datapacks
            return null;
        }
    }

    static final class FabricChunkInfo extends AbstractChunkInfo<Class<? extends Entity>> {
        private final CountMap<Class<? extends Entity>> entityCounts;

        FabricChunkInfo(long chunkPos) {
            super(ChunkPosHelper.unpackX(chunkPos), ChunkPosHelper.unpackZ(chunkPos));

            this.entityCounts = new CountMap.Simple<>(new HashMap<>());
        }

        @Override
        public CountMap<Class<? extends Entity>> getEntityCounts() {
            return this.entityCounts;
        }

        @Override
        public String entityTypeName(Class<? extends Entity> type) {
            return EntityTypeAccessor.spark$getClassNameMap().get(type);
        }
    }
}

