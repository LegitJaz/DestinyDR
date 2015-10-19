package net.dungeonrealms.entities.types.pets;

import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.EntityChicken;
import net.minecraft.server.v1_8_R3.World;

import java.util.UUID;

/**
 * Created by Kieran on 9/5/2015.
 */
public class Chicken extends EntityChicken {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    public Chicken(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.mobName = mobName;
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(mobName);
        this.canPickUpLoot = false;
        this.persistent = true;
        this.setAge(0);
        this.ageLocked = true;

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public Chicken(World world) {
        super(world);
    }
}
