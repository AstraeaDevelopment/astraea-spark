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

import me.lucko.spark.common.command.sender.AbstractCommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.legacyfabric.fabric.api.permission.v1.PermissibleCommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.UUID;

public class LegacyFabricServerCommandSender extends AbstractCommandSender<PermissibleCommandSource> {
    public LegacyFabricServerCommandSender(PermissibleCommandSource commandSource) {
        super(commandSource);
    }

    @Override
    public String getName() {
        String name = this.delegate.getName().asUnformattedString(); //todo verify if even remotely correct
        if (this.delegate.getEntity() != null && name.equals("Server")) {
            return "Console";
        }
        return name;
    }

    @Override
    public UUID getUniqueId() {
        Entity entity = this.delegate.getEntity();
        return entity != null ? entity.getUuid() : null;
    }

    @Override
    public void sendMessage(Component message) {
        Text component = Text.Serializer.deserialize(GsonComponentSerializer.gson().serializer().toJson(message)); //todo verify if even remotely correct
        this.delegate.sendMessage(component);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.delegate.hasPermission(permission); //todo verify if even remotely correct
        /*
        return Permissions.getPermissionValue(this.delegate, permission).orElseGet(() -> {
            ServerPlayerEntity player = this.delegate.getPlayer();
            MinecraftServer server = this.delegate.getServer();
            if (player != null) {
                if (server != null && server.isHost(player.getGameProfile())) {
                    return true;
                }
                return player.hasPermissionLevel(4);
            }
            return true;
        });
         */
    }

    @Override
    protected Object getObjectForComparison() {
        UUID uniqueId = getUniqueId();
        if (uniqueId != null) {
            return uniqueId;
        }
        Entity entity = this.delegate.getEntity();
        if (entity != null) {
            return entity;
        }
        return getName();
    }
}
