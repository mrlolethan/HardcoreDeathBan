/*
 * This document is a part of the source code and related artifacts for
 * HardcoreDeathBan, an open source Bukkit plugin for hardcore-type servers
 * where players are temporarily banned upon death.
 *
 * http://dev.bukkit.org/bukkit-plugins/hardcoredeathban/
 * http://github.com/mstiles92/HardcoreDeathBan
 *
 * Copyright (c) 2014 Matthew Stiles (mstiles92)
 *
 * Licensed under the Common Development and Distribution License Version 1.0
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the CDDL-1.0 License at
 * http://opensource.org/licenses/CDDL-1.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the license.
 */

package com.mstiles92.plugins.hardcoredeathban.data;

import com.mstiles92.plugins.hardcoredeathban.HardcoreDeathBan;
import org.bukkit.entity.Player;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private static Map<UUID, PlayerData> instances = new HashMap<>();

    private String lastSeenName;
    private long unbanTimeInMillis;
    private int revivalCredits;

    private PlayerData(JsonObject json) {
        lastSeenName = json.getString("lastSeenName");
        unbanTimeInMillis = json.getJsonNumber("unbanTimeInMillis").longValueExact();
        revivalCredits = json.getInt("revivalCredits");
    }

    private PlayerData(Player player) {
        lastSeenName = player.getName();
        unbanTimeInMillis = -1;
        revivalCredits = HardcoreDeathBan.getConfigObject().getStartingCredits(); //TODO: check for death classes as well
    }

    private JsonObject toJsonObject() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("lastSeenName", lastSeenName);
        builder.add("unbanTimeInMillis", new BigDecimal(unbanTimeInMillis));
        builder.add("revivalCredits", revivalCredits);
        return builder.build();
    }

    private static PlayerData create(Player player) {
        PlayerData data = new PlayerData(player);
        instances.put(player.getUniqueId(), data);
        return data;
    }

    public static void deserialize(JsonObject json) {
        instances.clear();

        for (Map.Entry<String, JsonValue> entry : json.entrySet()) {
            instances.put(UUID.fromString(entry.getKey()), new PlayerData((JsonObject) entry.getValue()));
        }
    }

    public static JsonObject serialize() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Map.Entry<UUID, PlayerData> entry : instances.entrySet()) {
            builder.add(entry.getKey().toString(), entry.getValue().toJsonObject());
        }
        return builder.build();
    }

    public static PlayerData get(Player player) {
        if (instances.containsKey(player.getUniqueId())) {
            PlayerData data = instances.get(player.getUniqueId());
            data.lastSeenName = player.getName();
            return data;
        } else {
            return create(player);
        }
    }

    public String getLastSeenName() {
        return lastSeenName;
    }

    public long getUnbanTimeInMillis() {
        return unbanTimeInMillis;
    }

    public int getRevivalCredits() {
        return revivalCredits;
    }
}