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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.Text;

import java.util.UUID;

public class LegacyFabricClientCommandSender extends AbstractCommandSender<FabricClientCommandSource> {
    public LegacyFabricClientCommandSender(FabricClientCommandSource commandSource) {
        super(commandSource);
    }

    public LegacyFabricClientCommandSender(ClientCommandSource commandSource) {
        this((FabricClientCommandSource) commandSource);
    }

    @Override
    public String getName() {
        return this.delegate.getPlayer().getGameProfile().getName();
    }

    @Override
    public UUID getUniqueId() {
        return this.delegate.getPlayer().getUuid();
    }

    @Override
    public void sendMessage(Component message) {
        Text component = Text.Serialization.fromJsonTree(GsonComponentSerializer.gson().serializeToTree(message), DynamicRegistryManager.EMPTY);
        this.delegate.sendFeedback(component);
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    protected Object getObjectForComparison() {
        return this.delegate.getPlayer();
    }
}
