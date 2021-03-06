package net.dungeonrealms.game.mechanic.dungeons.rifts;

import lombok.Getter;
import org.bukkit.Location;

@Getter
public enum MapData {
//        VARENGLADE("varenglade", new Location(null,-363,59,16),new Location(null,-363,59,-12), new Location(null,-364,59,-2),10),
//        INFERNAL("infernalAbyss", new Location(null,-55,157,670),new Location(null,-55,157,647), new Location(null,-55,157,660),12),
        RIFT("lurker_room", new Location(null, 68.291, 96.66, -293, 179.9F, 36.6F), new Location(null, 67, 79, -295), new Location(null, 67, 79, -295), 20);

        private String worldName;
        private Location spawnLocation;
        private Location bossLocation;
        private Location centerLocation;
        private int mapRadius;
        MapData(String worldName, Location spawnLocation, Location bossLocation, Location center, int radius) {
            this.worldName = worldName;
            this.spawnLocation = spawnLocation;
            this.bossLocation = bossLocation;
            this.centerLocation = center;
            this.mapRadius = radius;
        }
}