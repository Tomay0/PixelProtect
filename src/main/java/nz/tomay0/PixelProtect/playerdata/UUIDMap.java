package nz.tomay0.PixelProtect.playerdata;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Contains data about every player who has joined the server.
 */
public class UUIDMap implements Listener {
    private Map<String, UUID> nameToUUID= new HashMap<>();
    private File uuidMapFile;

    /**
     * New name to UUID map from a file
     *
     * @param uuidMapFile file to read from
     * @throws IOException
     */
    public UUIDMap(File uuidMapFile) {
        this.uuidMapFile = uuidMapFile;
        if (uuidMapFile.exists()) {
            try {
                // existing file - load

                BufferedReader reader = new BufferedReader(new FileReader(uuidMapFile));

                reader.lines().forEach(line -> {
                    String[] split = line.split(" ");
                    if (split.length != 2) {
                        Bukkit.getLogger().log(Level.WARNING, uuidMapFile.getName() + " contains an invalid formatted line: " + line);
                        return;
                    }

                    String name = split[0];
                    UUID uuid = UUID.fromString(split[1]);
                    nameToUUID.put(name, uuid);
                });

                reader.close();
            } catch (IOException e) {
                throw new Error(e);
            }
        } else {
            // new file, get all offline players
            OfflinePlayer[] players = Bukkit.getOfflinePlayers();
            for (OfflinePlayer player : players) {
                nameToUUID.put(player.getName().toLowerCase(), player.getUniqueId());
            }
            save();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!nameToUUID.containsKey(e.getPlayer().getName())) {
            nameToUUID.put(e.getPlayer().getName().toLowerCase(), e.getPlayer().getUniqueId());
            save();
        }
    }


    /**
     * Update the UUID map
     */
    private void save() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(uuidMapFile));

            for (String name : nameToUUID.keySet()) {
                writer.write(name + " " + nameToUUID.get(name) + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new Error(e);
        }
    }


    /**
     * Get a uuid from a player name
     *
     * @param playerName playername
     * @return uuid
     */
    public UUID getUUID(String playerName) {
        if (!nameToUUID.containsKey(playerName.toLowerCase())) return null;

        return nameToUUID.get(playerName.toLowerCase());
    }
}
