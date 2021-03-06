package net.dungeonrealms.database;

import com.google.common.collect.Lists;
import com.mysql.jdbc.StatementImpl;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.util.StringUtils;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.util.CharacterType;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.punishment.Punishments;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievementLevel;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.donation.Buff;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.donation.overrides.CosmeticOverrides;
import net.dungeonrealms.game.donation.overrides.EquipmentSlot;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler.EnumPlayerAlignments;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemArmorShield;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.item.items.core.setbonus.EnergyBonus;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonus;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonuses;
import net.dungeonrealms.game.item.items.functional.accessories.EnchantTrinketData;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketItem;
import net.dungeonrealms.game.mastery.*;
import net.dungeonrealms.game.mechanic.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.mechanic.data.MuleTier;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.CurrencyTab;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticleEffect;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.PendingPurchaseable;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.quests.QuestPlayerData;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.world.WorldType;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PlayerWrapper {

    @Getter
    private static Map<UUID, PlayerWrapper> playerWrappers = new ConcurrentHashMap<>();

    @Getter
    private UUID uuid;

    @Getter
    @Setter
    private int enjinId = -1;

    @Getter
    private int accountID, characterID;

    @Getter
    private CharacterType characterType;

    @Getter
    @Setter
    private int guildID;

    @Getter
    @Setter
    private long lastBlockMovement = 0L;

    @Setter
    @Getter
    private DyeColor lastIndependantColor = DyeColor.RED;

    @Getter
    private PlayerGameStats playerGameStats;

    @Getter
    @Setter
    private long timeCreated;

    @Getter
    @Setter
    private Map<String, Long> lastDungeonRuns = new HashMap<>();

    @Getter
    @Setter
    private int health, level, experience;

    @Getter
    private int gems, ecash;

    @Getter
    @Setter
    private long lastLogin, lastLogout, lastShardTransfer, lastFreeEcash, firstLogin;

    @Getter
    private Inventory pendingInventory, pendingArmor, pendingMuleInventory;

    @Getter
    @Setter
    private String pendingInventoryString, pendingArmorString, storedLocationString, storedCollectionBinString, pendingParticleEffectString;

    @Getter
    @Setter
    private SpecialParticles activeChestEffect, activeRealmEffect, activePetEffect;

    @Getter
    @Setter
    private String shardPlayingOn, questData, username;

    @Getter
    @Setter
    private EnumMounts activeMount;
    @Getter
    @Setter
    private EnumPets activePet;
    @Getter
    private ParticleEffect activeTrail;
    @Getter
    @Setter
    private EnumMountSkins activeMountSkin;

    @Getter
    @Setter
    private int bankLevel, shopLevel, muleLevel, storedFoodLevel;

    @Getter
    private boolean isPlaying = false;

    @Getter
    @Setter
    private String shopDescription;
    @Getter
    private LinkedList<String> purchaseHistory = new LinkedList<>();

    @Getter
    @Setter
    private boolean combatLogged = false, shopOpened = false, loggerDied = false;

    @Getter
    @Setter
    private String lastIP;

    @Getter
    @Setter
    private TeleportLocation hearthstone;

    @Getter
    @Setter
    private long lastTimeIPUsed;

    @Getter
    @Setter
    private boolean enteringRealm = false, uploadingRealm, upgradingRealm;

    @Getter
    @Setter
    private long lastRealmReset;

    @Getter
    @Setter
    private String realmTitle, realmDescription;

    @Getter
    @Setter
    private int realmTier = 1;

    @Getter
    private EnumPlayerAlignments alignment = EnumPlayerAlignments.LAWFUL;

    @Getter
    @Setter
    private int alignmentTime = 0;

    @Getter
    @Setter
    private Location storedLocation;

    @Setter
    @Getter
    private CurrencyTab currencyTab;

    private Map<ShardTier, Integer> keyShards = new HashMap<>();

    @Getter
    @Setter
    private long muteExpire = -1, banExpire = -1;

    @Getter
    @Setter
    private String muteReason, banReason;

    @Getter
    @Setter
    private Integer whoBannedMeID, whoMutedMeID;

    @Getter
    @Setter
    private boolean firstTimePlaying = false;

    @Getter
    private HashMap<UUID, Integer> friendsList = new HashMap<>(), ignoredFriends = new HashMap<>(), pendingFriends = new HashMap<>();

    @Getter
    private Set<EnumAchievements> achievements = new HashSet<>();
    @Getter
    private Set<ParticleEffect> particles = new HashSet<>();
    //    @Getter
//    private Set<ParticleEffect> trails = new HashSet<>();
    @Getter
    private Set<EnumMountSkins> mountSkins = new HashSet<>();
    @Getter
    private Set<EnumMounts> mountsUnlocked = new HashSet<>();
    @Getter
    private Map<EnumPets, String> petsUnlocked = new HashMap<>();

    @Getter
    private Map<Purchaseables, Integer> purchaseablesUnlocked = new HashMap<>();

    @Getter
    private List<PendingPurchaseable> pendingPurchaseablesUnlocked = new ArrayList<>();


    @Getter
    @Setter
    private Integer rankExpiration;

    @Getter
    @Setter
    private String lastViewedBuild;

    @Getter
    @Setter
    private int lastNoteSize;

    @Getter
    @Setter
    private long lastVote;

    @Getter
    private PlayerToggles toggles;

    @Getter
    private PlayerStats playerStats;

    @Getter
    private Storage pendingBankStorage;

    @Getter
    private CosmeticOverrides activeHatOverride;


    // Non database data:
    @Setter
    private Player player;

    @Getter
    @Setter
    private SpecialParticleEffect activeSpecialEffect = null;

    @Getter
    private AttributeList attributes = new AttributeList();
    private String currentWeapon;
    @Getter
    private boolean attributesLoaded;

    @Getter
    private boolean loadedSuccessfully = false;

    @Getter
    private Map<Item.ItemTier, Integer> dryLoot = new HashMap<>();

    public PlayerWrapper(UUID uuid) {
        this.uuid = uuid;
    }

    public PlayerWrapper(Player player) {
        this(player.getUniqueId());
        this.player = player;
    }

    public void loadData(boolean async) {
        this.loadData(async, null, null);
    }


    public void loadData(boolean async, Consumer<PlayerWrapper> callback) {
        loadData(async, null, callback);
    }

    /**
     * Load the playerWrapper data, Must be thread safe.
     *
     * @param async    async this method
     * @param callback callback to call after its loaded.
     */
    public void loadData(boolean async, Integer characterID, Consumer<PlayerWrapper> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> loadData(false, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }

        try {
            long start = System.currentTimeMillis();
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
                    "SELECT * FROM `users` LEFT JOIN `ranks` ON `users`.`account_id` = `ranks`.`account_id` " +
                            "LEFT JOIN `toggles` ON `users`.`account_id` = `toggles`.`account_id` " +
                            "LEFT JOIN `ip_addresses` ON `users`.`account_id` = `ip_addresses`.`account_id` " +
                            "LEFT JOIN `guild_members` ON `users`.`account_id` = `guild_members`.`account_id` " +
                            "LEFT JOIN `guilds` ON `guild_members`.`guild_id` = `guilds`.`guild_id` " +
                            "LEFT JOIN `characters` ON `characters`.`character_id` = " + (characterID == null ? "`users`.`selected_character_id` " : characterID.intValue()) +
                            "LEFT JOIN `attributes` ON `characters`.`character_id` = `attributes`.`character_id` " +
                            "LEFT JOIN `realm` ON `characters`.`character_id` = `realm`.`character_id` " +
                            "LEFT JOIN `statistics` ON `characters`.`character_id` = `statistics`.`character_id` " +
                            "WHERE `users`.`uuid` = ?;");
            statement.setString(1, uuid.toString());

            ResultSet result = statement.executeQuery();
            if (result.first()) {
                this.accountID = result.getInt("users.account_id");
                this.enjinId = result.getInt("users.enjin_id");
                this.toggles = new PlayerToggles(this);
                this.username = result.getString("users.username");
                this.isPlaying = result.getBoolean("users.is_online");
                this.shardPlayingOn = result.getString("users.currentShard");
                this.characterID = result.getInt("characters.character_id");

                if (this.characterID == 0) {
                    //No character ID??

                }
                this.health = result.getInt("characters.health");
                this.level = result.getInt("characters.level");

                this.username = result.getString("users.username");

                this.characterType = CharacterType.getCharacterType(result.getString("characters.character_type"));


                this.loadBanks(result);
                this.loadPlayerPendingInventory(result);
                this.loadPlayerPendingEquipment(result);
                this.loadMuleInventory(result);
                this.loadLocation(result);

                loadLastDungeonRuns(result.getString("users.dungeon_cooldown"));

                this.guildID = result.getInt("guilds.guild_id");

                this.lastLogin = result.getLong("users.last_login");
                this.lastLogout = result.getLong("users.last_logout");
                this.lastShardTransfer = result.getLong("users.last_shard_transfer");

                this.lastViewedBuild = result.getString("users.lastViewedBuild");
                this.lastNoteSize = result.getInt("users.lastNoteSize");

                this.lastVote = result.getLong("users.lastVote");

                this.playerGameStats = new PlayerGameStats(this.characterID);
                this.playerGameStats.extractData(result);

                this.toggles.extractData(result);

                this.playerStats = new PlayerStats(uuid, this.characterID);
                this.playerStats.extractData(result);

                this.ecash = result.getInt("users.ecash");
                this.lastFreeEcash = result.getLong("users.last_free_ecash");
                this.gems = result.getInt("characters.gems");
                this.experience = result.getInt("characters.experience");

                //String activeMount, activePet, activeTrail, activeMountSkin;
                this.activeMount = EnumMounts.getByName(result.getString("characters.activeMount"));
                this.activePet = EnumPets.getByName(result.getString("characters.activePet"));
                this.activeTrail = ParticleEffect.getByName(result.getString("characters.activeTrail"));
                this.activeMountSkin = EnumMountSkins.getByName(result.getString("characters.activeMountSkin"));
                setActiveHatOverride(CosmeticOverrides.getByName(result.getString("characters.activeHatOverride")));
                this.achievements = StringUtils.deserializeEnumListToSet(result.getString("characters.achievements"), EnumAchievements.class);

                this.questData = result.getString("characters.questData");
                this.shopLevel = result.getInt("characters.shop_level");
                this.storedFoodLevel = result.getInt("characters.foodLevel");
                this.combatLogged = result.getBoolean("characters.combatLogged");
                this.shopOpened = result.getBoolean("characters.shopOpened");
                this.loggerDied = result.getBoolean("characters.loggerDied");
                //We need to get the most updated last_used variable when pulling this..
                this.lastIP = result.getString("ip_addresses.ip_address");
                this.lastTimeIPUsed = result.getLong("ip_addresses.last_used");
                this.hearthstone = TeleportLocation.getByName(result.getString("characters.currentHearthStone"));

                this.alignmentTime = result.getInt("characters.alignmentTime");
                this.alignment = EnumPlayerAlignments.get(result.getString("characters.alignment"));

                this.currencyTab = new CurrencyTab(this.uuid).deserializeCurrencyTab(result.getString("users.currencyTab"));

                PlayerRank rank = PlayerRank.getFromInternalName(result.getString("ranks.rank"));
                if (rank != null && rank != PlayerRank.DEFAULT)
                    Rank.getCachedRanks().put(this.uuid, rank);
                else {
                    Rank.getCachedRanks().remove(this.uuid);
                }
                this.rankExpiration = result.getInt("ranks.expiration");

                this.realmTitle = result.getString("realm.title");
                this.realmDescription = result.getString("realm.description");

                //enteringRealm = false, uploading, upgrading;
                this.enteringRealm = result.getBoolean("realm.enteringRealm");
                this.uploadingRealm = result.getBoolean("realm.uploading");
                this.upgradingRealm = result.getBoolean("realm.upgrading");
                this.realmTier = result.getInt("realm.tier");
                this.firstLogin = result.getLong("users.firstLogin");
                this.lastRealmReset = result.getLong("realm.lastReset");

                loadSpecialParticleEffect(result);

                this.loadDryLoot(result);
                this.mountsUnlocked = StringUtils.deserializeEnumListToSet(result.getString("characters.mounts"), EnumMounts.class);
                //Unlockables.
                this.loadUnlockables(result);

                for (ShardTier tier : ShardTier.values())
                    this.keyShards.put(tier, result.getInt(tier.getDBField()));

                this.timeCreated = result.getLong("characters.created");

                this.loadFriends();


                if (Constants.debug)
                    Bukkit.getLogger().info("Loaded " + this.username + "'s PlayerWrapper data in " + (System.currentTimeMillis() - start) + "ms.");

                if (callback != null) {
                    loadedSuccessfully = true;
                    callback.accept(this);
                    return;
                }
            }
            loadedSuccessfully = true;
        } catch (Exception e) {
            e.printStackTrace();
            loadedSuccessfully = false;
        }

        if (callback != null)
            callback.accept(null);
    }

    public GuildWrapper getGuild() {
        return getGuildID() != 0 ? GuildDatabase.getAPI().getGuildWrapper(getGuildID()) : null;
    }

    public boolean isInGuild() {
        return getGuild() != null;
    }

    public int getPortalShards(ShardTier tier) {
        return keyShards.containsKey(tier) ? keyShards.get(tier) : 0;
    }

    public void setPortalShards(ShardTier tier, int amt) {
        keyShards.put(tier, amt);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }


    public void loadDryLoot(ResultSet rs) throws SQLException {
        String loot = rs.getString("characters.loot");
        if (loot != null && loot.contains(":")) {

            for (String dry : loot.split(":")) {
                if (dry.isEmpty()) continue;
                try {
                    String numString = dry.split("@")[0];
                    Item.ItemTier tier;
                    if (org.apache.commons.lang.StringUtils.isNumeric(numString)) {
                        Integer t = Integer.parseInt(numString);
                        tier = Item.ItemTier.getByTier(t);
                    } else {
                        tier = Item.ItemTier.valueOf(numString);
                    }
                    int amount = Integer.parseInt(dry.split("@")[1]);
                    this.dryLoot.put(tier, amount);
                } catch (Exception e) {
                    Bukkit.getLogger().info("unable to parse: " + dry + " from " + username);
                }
            }
        }
    }

    public void setGems(int gems) {

        if (gems < 0 || gems > 10_000_000) {
            GameAPI.sendWarning("Tried to set " + getPlayer().getName() + "'s gems to " + gems + " on shard {SERVER}.");
            gems = 0;
        }

        if (gems > 2_000_000) {
            GameAPI.sendWarning("Set " + getPlayer().getName() + "'s gems to " + gems + " on shard {SERVER}.");
        }

        if (gems != getGems())
            SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_GEMS, gems, getCharacterID());
        this.gems = gems;
    }

    public void addGems(int gems) {
        assert gems >= 0;
        setGems(getGems() + gems);
    }

    public void setActiveHatOverride(CosmeticOverrides override) {
        if (override != null && !override.getEquipSlot().equals(EquipmentSlot.HEAD))
            throw new IllegalArgumentException("Only hat overrides!");
        this.activeHatOverride = override;
        Player ourPlayer = getPlayer();
        if (ourPlayer != null) {
            if (override == null)
                MetadataUtils.Metadata.ACTIVE_HAT.remove(ourPlayer);
            else
                MetadataUtils.Metadata.ACTIVE_HAT.set(ourPlayer, override.name());
        }
    }

    public void subtractGems(int gems) {
        assert gems >= 0;
        setGems(getGems() - gems);
    }

    public String getPetName(EnumPets pets) {
        String petName = this.petsUnlocked.get(pets);
        return petName != null ? petName : pets.getDisplayName();
    }

    public void fullyReloadPurchaseables(Consumer<Boolean> callback) {
        SQLDatabaseAPI.getInstance().executeQuery(getQuery(QueryType.SELECT_PURCHASES, getAccountID()), true, (set) -> {
            try {
                if (set.first()) {
                    loadPurchaseables(set);
                    if (callback != null) callback.accept(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) callback.accept(false);
            }
        });
    }

    private void loadPurchaseables(ResultSet result) throws SQLException {
        this.purchaseablesUnlocked = StringUtils.deserializeNumberMap(result.getString("users.purchaseables"), Purchaseables.class, Integer.class);
        loadPendingPurchaseables(result.getString("users.pending_purchaseables"));
    }

    public void loadUnlockables(ResultSet result) throws SQLException {
        loadPurchaseables(result);
        this.mountSkins = StringUtils.deserializeEnumListToSet(result.getString("users.mountSkin"), EnumMountSkins.class);
        this.particles = StringUtils.deserializeEnumListToSet(result.getString("users.particles"), ParticleEffect.class);
//        this.trails = StringUtils.deserializeEnumListToSet(result.getString("users.trails"), ParticleEffect.class);

        List<String> list = StringUtils.deserializeList(result.getString("users.pets"), ",");
        if (list != null) {
            for (String str : list) {
                String type;
                String name = null;
                if (str.contains("@")) {
                    String[] contents = str.split("@");
                    type = contents[0];
                    name = contents[1];
                } else {
                    type = str;
                }

                EnumPets pets = EnumPets.getByName(type);
                if (pets == null) {
                    Bukkit.getLogger().info("Invalid pet type: " + str + " for " + getUsername());
                    continue;
                }

                this.petsUnlocked.put(pets, name);
            }
        }
    }

    public String getSerializePetString() {
        if (this.petsUnlocked.isEmpty()) return null;
        StringBuilder builder = new StringBuilder();

        this.petsUnlocked.forEach((pet, name) -> builder.append(pet.getName()).append(name != null ? "@" + name : "@" + pet.getDisplayName()).append(","));

        return builder.toString();
    }

    public void loadAllPunishments(boolean async, Consumer<Punishments> callback) {
        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ALL_PUNISHMENTS.getQuery(this.uuid.toString()), async, rs -> {
            Punishments punishments = new Punishments();
            try {
                punishments.extractData(rs);
            } catch (Exception e) {
                e.printStackTrace();
            }
            callback.accept(punishments);
        });
    }

    //tfw greg pays $10 for a burger from macers, 8.1 is decent on that flavor scale
    public void loadPunishment(boolean async, Consumer<Long> muteLoadedCallback) {
        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_VALID_PUNISHMENTS.getQuery(this.uuid.toString()), async, result -> {
            try {
                while (result.next()) {
                    //Not valid.
                    if (result.getBoolean("quashed")) continue;
                    if (result.getString("type").equals("mute")) {
                        this.muteExpire = result.getLong("expiration");
                        this.muteReason = result.getString("reason");
                        this.whoMutedMeID = result.getInt("punisher_id");
                        if (muteLoadedCallback != null)
                            muteLoadedCallback.accept(this.muteExpire);
                    } else {
                        this.banExpire = result.getLong("expiration");
                        this.banReason = result.getString("reason");
                        this.whoBannedMeID = result.getInt("punisher_id");
                    }
                }
                result.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadFriends() throws SQLException {
        //Not too sure if this query is correct for what I am trying to do. Can not test atm because we have no data in the database. If it doesn't work I will fix it.
        @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
                "SELECT friend_id, status FROM friends WHERE account_id = ?;");
        statement.setInt(1, this.accountID);
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            int friendId = result.getInt("friends.friend_id");
            String status = result.getString("friends.status");
            UUID friendUUID = SQLDatabaseAPI.getInstance().getUUIDFromAccountID(friendId);
            if (friendUUID == null) {
                Bukkit.getLogger().info("Unable to get friend UUID for ID " + friendId + " for " + this.username);
                continue;
            }

            if (status.equalsIgnoreCase("friends")) {
                friendsList.put(friendUUID, friendId);
            } else if (status.equalsIgnoreCase("pending")) {
                pendingFriends.put(friendUUID, friendId);
            } else if (status.equalsIgnoreCase("blocked")) {
                ignoredFriends.put(friendUUID, friendId);
            }
        }
    }


    @SneakyThrows
    private void loadLocation(ResultSet set) {
        this.storedLocationString = set.getString("characters.location");
        if (storedLocationString == null || storedLocationString.isEmpty() || storedLocationString.equalsIgnoreCase("null")) {
            firstTimePlaying = true;
            this.storedLocation = TeleportLocation.STARTER.getLocation();
        }
        if (!firstTimePlaying) {
            try {
                String[] locArray = storedLocationString.split(",");

                String world = GameAPI.getMainWorld().getName();
                int start = 0;
                if (locArray.length == 6) {
                    start = 1;
                    world = locArray[0];
                }
                double x = Double.parseDouble(locArray[start++]);
                double y = Double.parseDouble(locArray[start++]);
                double z = Double.parseDouble(locArray[start++]);
                float yaw = Float.parseFloat(locArray[start++]);
                float pitch = Float.parseFloat(locArray[start++]);
                //Success.
                this.storedLocation = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                return;
            } catch (Exception e) {
                Bukkit.getLogger().info("Unable to load location for " + storedLocationString);
                e.printStackTrace();
                //Rip.
            }
            this.storedLocation = TeleportLocation.NETYLI.getLocation();
        }
    }

    private String getLocationString(Player player) {
        return getLocationString(player.getLocation());
    }

    /**
     * Used to get the location to store. If they are in a realm or some other world it will returned the last saved valid location.
     *
     * @param location
     * @return
     */
    public String getLocationString(Location location) {
        if (WorldType.getWorld(location.getWorld()) == null)
            return storedLocationString; // Don't save if we're in an instanced world.
        return new StringBuilder().append(location.getWorld().getName()).append(",").append(location.getX()).append(',').append(location.getY() + 0.3).append(',').append(location.getZ()).append(',').append(location.getYaw()).append(',').append(location.getPitch()).toString();
    }

    public String getFormattedShardName() {
        if (!isPlaying()) return "None";
        ShardInfo shard = ShardInfo.getByPseudoName(getShardPlayingOn());
        return shard != null ? shard.getShardID() : "";
    }

    public void setPlayingStatus(boolean playing) {
        //if (playing == this.isPlaying) return;
        this.isPlaying = playing;

        SQLDatabaseAPI.getInstance().executeUpdate(updates -> {
            //Set...
        }, QueryType.SET_ONLINE_STATUS.getQuery(this.isPlaying ? 1 : 0, this.isPlaying ? "'" + DungeonRealms.getShard().getPseudoName() + "'" : null, accountID));
    }

    public void saveData(boolean async, Boolean isOnline) {
        this.saveData(async, isOnline, null);
    }


    public void saveData(boolean async, Boolean isOnline, Consumer<PlayerWrapper> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> saveData(false, isOnline, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }


        try {
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("");

            statement.addBatch(getCharacterReplaceQuery(player));
            //User query.
            statement.addBatch(getUsersUpdateQuery(isOnline));
            if (this.getPlayerGameStats() != null)
                statement.addBatch(this.getPlayerGameStats().getUpdateStatement());
            String playerStats = getPlayerStats().getUpdateStatement();
            statement.addBatch(playerStats);
            Bukkit.getLogger().info("Player Stats: " + playerStats);
            statement.addBatch(getToggles().getUpdateStatement());
            if (hasFriendData()) getFriendUpdateQuery(statement);
            if (this.player != null) {
                statement.addBatch(String.format("REPLACE INTO ip_addresses (account_id, ip_address, last_used) VALUES ('%s', '%s', '%s')", getAccountID(), player.getAddress().getAddress().getHostAddress(), System.currentTimeMillis()));
            }
            statement.addBatch(String.format("REPLACE INTO ranks (account_id, rank, expiration) VALUES ('%s', '%s', '%s')", getAccountID(), getRank().getInternalName(), getRankExpiration()));
            //Realm info
            statement.addBatch(getQuery(QueryType.UPDATE_REALM, getRealmTitle(), getRealmDescription(), isUploadingRealm(), isUpgradingRealm(), getRealmTier(), isEnteringRealm(), getLastRealmReset(), getCharacterID()));

            long start = System.currentTimeMillis();
            if (Constants.debug)
                Bukkit.getLogger().info("Preparing to execute batch: " + toString(((StatementImpl) statement).getBatchedArgs()));
            statement.executeBatch();

            if (Constants.debug)
                Bukkit.getLogger().info("Batch executed in " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            GameAPI.sendDevMessage("Failed to Player Data for " + username + " on {SERVER}! Exception Message: " + e.getMessage());
            e.printStackTrace();
        }


        if (callback != null) {
            //System.out.println("Save data debug 15! isAsync: " + async + " isOnline: " + isOnline + " hasCallback: " + (callback != null));
            callback.accept(this);
        }
    }

    private String getDryLootString() {
        StringBuilder builder = new StringBuilder();

        dryLoot.forEach((t, val) -> builder.append(t.name()).append("@").append(val).append(":"));

        return builder.toString();
    }

    private String toString(List<Object> objects) {
        StringBuilder builder = new StringBuilder();
        for (Object object : objects)
            builder.append(object != null ? object.toString() : "null").append(", ");
        return builder.toString();
    }

    private String getCharacterReplaceQuery(Player player) {
        Storage bankStorage = BankMechanics.getStorage(uuid);
        //Doesnt exist, offline data?
        if (bankStorage == null && player == null)
            bankStorage = pendingBankStorage;

        String collectionBinString = null;
        if (bankStorage != null && bankStorage.collection_bin != null)
            collectionBinString = ItemSerialization.toString(bankStorage.collection_bin);

        String bankString = bankStorage != null && bankStorage.inv != null ? ItemSerialization.toString(bankStorage.inv) : null;


        Inventory mule = MountUtils.getInventory(uuid);
        if (mule == null && player == null)
            mule = pendingMuleInventory;

        String muleString = null;
        if (mule != null) muleString = ItemSerialization.toString(mule);


        String locationString = player == null ? storedLocationString : getLocationString(player);

        List<Object> array = new ArrayList<Object>();

        QuestPlayerData data = Quests.getInstance().playerDataMap.get(player);
        this.questData = data != null ? data.toJSON().toString() : null;
        array.addAll(Lists.newArrayList(
                getTimeCreated(), getLevel(), getExperience(), getAlignment(), player == null ? this.pendingInventoryString : player.getInventory(),
                player == null ? this.pendingArmorString : getEquipmentString(player), getGems(), bankString, getBankLevel(),
                getShopLevel(), muleString, getMuleLevel(), getHealth(), locationString, getMountsUnlocked(),
                getActiveMount(), getActivePet(), getActiveTrail(), getActiveMountSkin(), getActiveHatOverride() != null ? getActiveHatOverride().name() : null,
                getQuestData(), collectionBinString, player == null ? storedFoodLevel : player.getFoodLevel(), isCombatLogged(),
                isShopOpened(), isLoggerDied(), getHearthstone(), getDryLootString(), getAlignmentTime()));

        for (ShardTier tier : ShardTier.values())
            array.add(getPortalShards(tier));

        String specialParticle = null;
        if (this.getActiveSpecialEffect() != null && this.getActiveSpecialEffect().getParticleEnum() != null)
            specialParticle = this.getActiveSpecialEffect().getParticleEnum().getInternalName();
        array.add(specialParticle);
        array.add(activeChestEffect == null ? null : activeChestEffect.getInternalName());
        array.add(activeRealmEffect == null ? null : activeRealmEffect.getInternalName());
        array.add(activePetEffect == null ? null : activePetEffect.getInternalName());

        array.add(getCharacterID());
        return getQuery(QueryType.CHARACTER_UPDATE, array.toArray(new Object[array.size()]));
    }

    public void runQuery(QueryType t, Object... args) {
        SQLDatabaseAPI.getInstance().addQuery(getQuery(t, args));
    }

    public void executeUpdate(QueryType t, Consumer<Integer> callback, Object... args) {
        SQLDatabaseAPI.getInstance().executeUpdate(callback, getQuery(t, args), true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public String getQuery(QueryType t, Object... args) {

        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];

            if (obj instanceof Boolean)
                obj = (Boolean) obj ? "1" : "0";

            if (obj instanceof List<?>) {
                List<?> list = (List<?>) obj;

                if (list.isEmpty()) {
                    obj = "";
                } else {
                    Object example = list.get(0);

                    if (example instanceof Enum)
                        obj = StringUtils.serializeEnumList((List<Enum>) list);
                }
            }

            if (obj instanceof Map<?, ?>) {
                Map<?, ?> theMap = (Map) obj;
                if (theMap.isEmpty()) obj = "";
                else {
                    Map.Entry<?, ?> entry = theMap.entrySet().iterator().next();
                    Object exampleKey = entry.getKey();
                    Object exampleValue = entry.getValue();
                    if ((exampleKey instanceof Enum) && (exampleValue instanceof Number)) {
                        obj = StringUtils.serializeEnumNumberMap((Map<Enum, Number>) obj);
                    }
                }
            }


            if (obj instanceof Set<?>) {
                Set<?> set = (Set<?>) obj;

                if (set.isEmpty()) {
                    obj = "";
                } else {
                    Object example = set.stream().findFirst().get();

                    if (example instanceof Enum)
                        obj = StringUtils.serializeEnumList((Set<Enum>) set);
                }
            }

            if (obj instanceof Location)
                obj = getLocationString((Location) obj);

            if (obj instanceof Enum)
                obj = ((Enum<?>) obj).name();

            if (obj instanceof Inventory)
                obj = ItemSerialization.toString((Inventory) obj);

            if (obj instanceof String)
                obj = SQLDatabaseAPI.escape((String) obj);

            args[i] = obj;
        }

        return t.getQuery(args);
    }

    public void setRank(PlayerRank newRank) {
        Rank.getCachedRanks().put(getUuid(), newRank);
    }

    public PlayerRank getRank() {
        return Rank.getPlayerRank(this.uuid);
    }

    public void sendDebug(String str) {
        if (getToggles().getState(Toggles.DEBUG)) {
            getPlayer().sendMessage(str);
            GameAPI.runAsSpectators(getPlayer(), p -> p.sendMessage(str));
        }
    }

    private String getUsersUpdateQuery(Boolean isOnline) {
        if (isOnline == null) isOnline = isPlaying();

        String currencyTab = getCurrencyTab() != null ? getCurrencyTab().getSerializedScrapTab() : null;

        return getQuery(QueryType.USER_UPDATE, getUsername(), getCharacterID(), getEcash(), getTimeCreated(), getLastLogin(),
                getLastLogout(), getLastFreeEcash(), getLastShardTransfer(), isOnline, isPlaying ? DungeonRealms.getShard().getPseudoName() : "null",
                currencyTab, getFirstLogin(), getLastViewedBuild(), getLastNoteSize(), getLastVote(), getSerializePetString(), getParticles(), getMountSkins(),
                getPurchaseablesUnlocked(), getSerializedPendingPurchaseables(), getSerializedDungeonCooldownString(), getEnjinId() > 0 ? getEnjinId() : null, getAccountID());
    }

    @SneakyThrows
    private boolean getFriendUpdateQuery(PreparedStatement statement) {
        if (friendsList.size() == 0 && ignoredFriends.size() == 0 && pendingFriends.size() == 0) return false;

        //StringBuilder builder = new StringBuilder("");
        StringBuilder toReturn = new StringBuilder("INSERT INTO friends (account_id, friend_id, status) VALUES ");
        boolean isFirstValues = true;
        for (int friendID : friendsList.values()) {
            statement.addBatch(QueryType.INSERT_FRIENDS.getQuery(getAccountID(), friendID, "friends", "friends"));
            if (!isFirstValues) toReturn.append(", ");
            toReturn.append('(');
            toReturn.append(getAccountID());
            toReturn.append(',');
            toReturn.append(friendID);
            toReturn.append(',');
            toReturn.append("'friends'");
            toReturn.append(')');
            isFirstValues = false;
        }

        for (int friendID : ignoredFriends.values()) {
//            statement.addBatch(QueryType.INSERT_FRIENDS.getQuery(getCharacterID(), friendID, "blocked", "blocked"));
            if (!isFirstValues) toReturn.append(", ");
            toReturn.append('(');
            toReturn.append(getAccountID());
            toReturn.append(',');
            toReturn.append(friendID);
            toReturn.append(',');
            toReturn.append("'blocked'");
            toReturn.append(')');
            isFirstValues = false;
        }

        for (int friendID : pendingFriends.values()) {
            if (!isFirstValues) toReturn.append(", ");
            toReturn.append('(');
            toReturn.append(getAccountID());
            toReturn.append(',');
            toReturn.append(friendID);
            toReturn.append(',');
            toReturn.append("'pending'");
            toReturn.append(')');
            isFirstValues = false;
        }

        toReturn.append(" ON DUPLICATE KEY UPDATE `status` = VALUES(`status`)");

        if (!isFirstValues) {
            System.out.println("Attempting to update the friends with the query: " + toReturn.toString());
            statement.executeUpdate(toReturn.toString());
        }

        return true;
    }

    private boolean hasFriendData() {
        return !friendsList.isEmpty() || !ignoredFriends.isEmpty() || !pendingFriends.isEmpty();
    }

    public static PlayerWrapper getWrapper(Player get) {
        return getPlayerWrapper(get);
    }

    public static PlayerWrapper getWrapperByCharacterID(int id) {
        for (PlayerWrapper wrapper : getPlayerWrappers().values()) {
            if (wrapper.getCharacterID() == id) {
                //Found em..
                return wrapper;
            }
        }
        return null;
    }

    public static PlayerWrapper getPlayerWrapper(Player toGet) {
        return getPlayerWrapper(toGet.getUniqueId());
    }

    /**
     * Callback will be async if we had to fetch the profile from the database.
     *
     * @param uuid
     * @param hadToLoadCallback
     */
    public static void getPlayerWrapper(UUID uuid, boolean storeWrapper, boolean getIfCached, Consumer<PlayerWrapper> hadToLoadCallback) {
        getPlayerWrapper(uuid, null, storeWrapper, getIfCached, hadToLoadCallback);
    }

    public static void getPlayerWrapper(UUID uuid, Integer characterID, boolean storeWrapper, boolean getIfCached, Consumer<PlayerWrapper> hadToLoadCallback) {
        PlayerWrapper wrapper;
        if (getIfCached) {
            wrapper = getPlayerWrapper(uuid);
            if (wrapper != null) {
                if (hadToLoadCallback != null) {
                    hadToLoadCallback.accept(wrapper);
                    return;
                }
            }
        }

        //Load offline playerwrapper..
        wrapper = new PlayerWrapper(uuid);
        //Store that shit..
        if (storeWrapper) {
            Bukkit.getLogger().info("Storing player wrapper for " + uuid.toString());
            PlayerWrapper.setWrapper(uuid, wrapper);
        }
        Bukkit.getLogger().info("Loading " + uuid.toString() + "'s offline wrapper.");
        wrapper.loadData(true, characterID, hadToLoadCallback);
    }

    public boolean isOnline() {
        Player p = getPlayer();
        return p != null && p.isOnline();
    }

    public static void getPlayerWrapper(UUID uuid, Consumer<PlayerWrapper> hadToLoadCallback) {
        getPlayerWrapper(uuid, true, true, hadToLoadCallback);
    }

    public static PlayerWrapper getPlayerWrapper(UUID toGet) {
        return playerWrappers.get(toGet);
    }

    public static void setWrapper(UUID uuid, PlayerWrapper wrapper) {
        playerWrappers.put(uuid, wrapper);
    }


    @SneakyThrows
    private void loadBanks(ResultSet set) {
        setBankLevel(set.getInt("characters.bank_level"));
        int level = getBankLevel();
        String serializedStorage = set.getString("characters.bank_storage");
        if (serializedStorage == null) {
            this.pendingBankStorage = new Storage(uuid, this.characterID, level);
            return;
        }

        //Auto set the inventory size based off level? min 9, max 54
        Inventory inv = ItemSerialization.fromString(serializedStorage, Math.max(9, Math.min(54, getBankLevel() * 9)));
        this.pendingBankStorage = new Storage(uuid, inv, this.characterID, level);
        loadCollectionBin(set, this.pendingBankStorage);
    }

    @SneakyThrows
    private void loadCollectionBin(ResultSet set, Storage storage) {
        String stringInv = set.getString("characters.collection_storage");
        if (stringInv != null && stringInv.length() > 1) {
            Inventory inv = ItemSerialization.fromString(stringInv);
            if (inv == null) return;
            int items = (int) Arrays.stream(inv.getContents()).filter(item -> item != null && item.getType() != Material.AIR).count();
            if (items > 0)
                storage.collection_bin = inv;
        }

    }

    @SneakyThrows
    private void loadPlayerPendingInventory(ResultSet set) {
        this.pendingInventoryString = set.getString("characters.inventory_storage");
        if (pendingInventoryString != null && pendingInventoryString.length() > 0 && !pendingInventoryString.equalsIgnoreCase("null")) {
            pendingInventory = ItemSerialization.fromString(pendingInventoryString, 36);
        }
    }

    @SneakyThrows
    private void loadSpecialParticleEffect(ResultSet set) {
        this.pendingParticleEffectString = set.getString("characters.activeSpecialEffect");
        String pendingChestEffectString = set.getString("characters.activeChestEffect");
        String pendingRealmEffectString = set.getString("characters.activeRealmEffect");
        String pendingPetEffectString = set.getString("characters.activePetEffect");

        if (pendingChestEffectString != null && pendingChestEffectString.length() > 0 && !pendingChestEffectString.equalsIgnoreCase("null")) {
            setActiveChestEffect(SpecialParticles.fromInternal(pendingChestEffectString));
        }

        if (pendingRealmEffectString != null && pendingRealmEffectString.length() > 0 && !pendingRealmEffectString.equalsIgnoreCase("null")) {
            setActiveRealmEffect(SpecialParticles.fromInternal(pendingRealmEffectString));
        }

        if (pendingPetEffectString != null && pendingPetEffectString.length() > 0 && !pendingPetEffectString.equalsIgnoreCase("null")) {
            setActivePetEffect(SpecialParticles.fromInternal(pendingPetEffectString));
        }
    }

    @SneakyThrows
    private void loadPlayerPendingEquipment(ResultSet set) {
        this.pendingArmorString = set.getString("characters.armour_storage");
        if (pendingArmorString != null && pendingArmorString.length() > 0 && !pendingArmorString.equalsIgnoreCase("null")) {
            pendingArmor = ItemSerialization.fromString(pendingArmorString, 9);
        }
    }

    /**
     * Update the held weapon stats, if changed.
     */
    public void updateWeapon() {
        updateWeapon(getPlayer().getInventory().getItemInMainHand());
//        ItemStack item = getPlayer().getInventory().getItemInMainHand();
//        if (item == null)
//            return;
//        String epoch = AntiDuplication.getUniqueEpochIdentifier(item);
//        if (epoch == null || !epoch.equals(this.currentWeapon))
//            calculateAllAttributes();
    }

    public void updateWeapon(ItemStack item) {
        if (item != null) {
            String epoch = AntiDuplication.getUniqueEpochIdentifier(item);
            if (epoch == null || !epoch.equals(this.currentWeapon))
                calculateAllAttributes(item);
        } else {
            this.currentWeapon = null;
            this.calculateAllAttributes(null);
        }
    }

    private void checkForIllegalItems() {
        Player player = getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (getRank().isAtLeast(PlayerRank.DEV) || !ItemWeapon.isWeapon(held))
            return;

        ModifierRange range = ((ItemWeapon) PersistentItem.constructItem(held)).getAttributes().getAttribute(WeaponAttributeType.DAMAGE);
        if (range.getValHigh() < 1200) // Should always be above the max possible damage of an item.
            return;

        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        player.sendMessage(ChatColor.YELLOW + "The weapon you posses is not of this world and has been returned to the gods.");

        GameAPI.sendWarning("Destroyed illegal item (" + range.toString() + ") from " + player.getName() + " on shard {SERVER}.");
    }

    public void calculateAllAttributes() {
        this.calculateAllAttributes(getPlayer().getInventory().getItemInMainHand());
    }

    /**
     * Calculate all stat attributes from gear.
     */
    public void calculateAllAttributes(ItemStack hand) {
        if (!isOnline())
            return;

        getAttributes().clear(); // Reset stats.

        //  CALCULATE FROM ITEMS  //
        checkForIllegalItems();

//        if (hand == null)
//            hand = getPlayer().getInventory().getItemInMainHand();

        if (hand != null && hand.getType() != Material.AIR && ItemWeapon.isWeapon(hand))
            getAttributes().addStats(hand);

        for (ItemStack armor : getPlayer().getInventory().getArmorContents())
            getAttributes().addStats(armor);

        ItemStack offHand = getPlayer().getInventory().getItemInOffHand();
        if (offHand != null && offHand.getType() != Material.AIR && ItemArmorShield.isShield(offHand)) {
            getAttributes().addStats(offHand);
        }

        getAttributes().addStat(Item.ArmorAttributeType.HEALTH_POINTS, 50); //default 50

        this.currentWeapon = AntiDuplication.getUniqueEpochIdentifier(hand);

        for (Stats stat : Stats.values())
            getAttributes().addStat(stat.getType(), this.getPlayerStats().getStat(stat));

        TrinketItem activeTrinket = Trinket.getActiveTrinketItem(getPlayer());
        if (activeTrinket != null && activeTrinket.getTrinketData() instanceof EnchantTrinketData && activeTrinket.getValue() != null) {
            EnchantTrinketData data = (EnchantTrinketData) activeTrinket.getTrinketData();
            if (!(data.getType() instanceof Item.PickaxeAttributeType) && !(data.getType() instanceof Item.FishingAttributeType)) {
                if (data.getType() == null) {
                    Bukkit.getLogger().info("Null enchant: " + data.getType() + " Tinker: " + activeTrinket.toString());
                } else {
                    getAttributes().addStat(data.getType(), activeTrinket.getValue());
                }
            }
        }

        SetBonuses active = SetBonus.getSetBonus(getPlayer());
        if (active != null && active.getSetBonus() instanceof EnergyBonus) {
            EnergyBonus bonus = (EnergyBonus) active.getSetBonus();
            getAttributes().addStat(Item.ArmorAttributeType.ENERGY_REGEN, bonus.getEnergyAmount());
        }


        // apply stat bonuses (str, dex, int, and vit)
        getAttributes().applyStatBonuses(this);
        int intellect = getAttributes().getAttribute(Item.ArmorAttributeType.INTELLECT).getValue();
        int strength = getAttributes().getAttribute(Item.ArmorAttributeType.STRENGTH).getValue();
        int dexterity = getAttributes().getAttribute(Item.ArmorAttributeType.DEXTERITY).getValue();
        int vitality = getAttributes().getAttribute(Item.ArmorAttributeType.VITALITY).getValue();

        if (strength > 0) {
            /*int armor = getAttributes().getAttribute(Item.ArmorAttributeType.ARMOR).getValue();
            int armorToAdd = (int)(armor * (strength * .03));
            if(armorToAdd > 0)getAttributes().addStat(Item.ArmorAttributeType.ARMOR, armorToAdd);*/


            /*int block = getAttributes().getAttribute(Item.ArmorAttributeType.BLOCK).getValue();
            int blockToAdd = (int)(block * (strength * .0002));
            if(blockToAdd > 0)getAttributes().addStat(Item.ArmorAttributeType.BLOCK, blockToAdd);*/

            getAttributes().addStat(Item.ArmorAttributeType.ARMOR, (int) (strength * .03));
            getAttributes().addStat(Item.ArmorAttributeType.BLOCK, (int) (strength * .017));
        }

        if (dexterity > 0) {
            /*int dodge = getAttributes().getAttribute(Item.ArmorAttributeType.DODGE).getValue();
            int dodgeToAdd = (int)(dodge * (dexterity * .0002));
            if(dodgeToAdd > 0)getAttributes().addStat(Item.ArmorAttributeType.DODGE, dodgeToAdd);

            int dps = getAttributes().getAttribute(Item.ArmorAttributeType.DAMAGE).getValue();
            int dpsToAdd = (int)(dps * (dexterity * .0003));
            if(dpsToAdd > 0)getAttributes().addStat(Item.ArmorAttributeType.DAMAGE, dpsToAdd);

            int armorPen = getAttributes().getAttribute(WeaponAttributeType.ARMOR_PENETRATION).getValue();
            int penToAdd = (int)(armorPen * (dexterity * .0002));
            if(penToAdd > 0)getAttributes().addStat(WeaponAttributeType.ARMOR_PENETRATION, penToAdd);*/

            getAttributes().addStat(Item.ArmorAttributeType.DODGE, (int) (dexterity * .017));
            getAttributes().addStat(Item.ArmorAttributeType.DAMAGE, (int) (dexterity * .03));
            getAttributes().addStat(WeaponAttributeType.VS_MONSTERS, (int) (dexterity * .02));


        }

        if (vitality > 0) {
            int health = getAttributes().getAttribute(Item.ArmorAttributeType.HEALTH_POINTS).getValue();
            int healthToAdd = (int) (health * (vitality * .00034));
            if (healthToAdd > 0) getAttributes().addStat(Item.ArmorAttributeType.HEALTH_POINTS, healthToAdd);

            int regen = getAttributes().getAttribute(Item.ArmorAttributeType.HEALTH_REGEN).getValue();
            int regenToAdd = (int) (regen * (vitality * .003));
            if (regenToAdd > 0) getAttributes().addStat(Item.ArmorAttributeType.HEALTH_REGEN, regenToAdd);

            /*int fireResist = getAttributes().getAttribute(Item.ArmorAttributeType.FIRE_RESISTANCE).getValue();
            int fireResistToAdd = (int)(fireResist * (vitality * .0004));
            if(fireResistToAdd > 0)getAttributes().addStat(Item.ArmorAttributeType.FIRE_RESISTANCE, fireResistToAdd);

            int iceResist = getAttributes().getAttribute(Item.ArmorAttributeType.ICE_RESISTANCE).getValue();
            int iceResistToAdd = (int)(iceResist * (vitality * .0004));
            if(iceResistToAdd > 0)getAttributes().addStat(Item.ArmorAttributeType.ICE_RESISTANCE, iceResistToAdd);

            int poisonResist = getAttributes().getAttribute(Item.ArmorAttributeType.POISON_RESISTANCE).getValue();
            int poisonResistToAdd = (int)(poisonResist * (vitality * .0004));
            if(poisonResistToAdd > 0)getAttributes().addStat(Item.ArmorAttributeType.POISON_RESISTANCE, poisonResistToAdd);*/

            getAttributes().addStat(Item.ArmorAttributeType.FIRE_RESISTANCE, (int) (vitality * .04));
            getAttributes().addStat(Item.ArmorAttributeType.ICE_RESISTANCE, (int) (vitality * .04));
            getAttributes().addStat(Item.ArmorAttributeType.POISON_RESISTANCE, (int) (vitality * .04));
        }

        if (intellect > 0) {
            /*int energy = getAttributes().getAttribute(Item.ArmorAttributeType.ENERGY_REGEN).getValue();
            int energyToAdd = (int)(energy * (intellect * .00015));
            if(energyToAdd > 0)getAttributes().addStat(Item.ArmorAttributeType.ENERGY_REGEN, energyToAdd);*/
            getAttributes().addStat(Item.ArmorAttributeType.ENERGY_REGEN, (int) (intellect * 0.015));

            /*int fireDamage = getAttributes().getAttribute(WeaponAttributeType.FIRE_DAMAGE).getValue();
            int fireDamageToAdd = (int)(fireDamage * (intellect * .0005));
            if(fireDamageToAdd > 0)getAttributes().addStat(WeaponAttributeType.FIRE_DAMAGE, fireDamageToAdd);

            int iceDamage = getAttributes().getAttribute(WeaponAttributeType.ICE_DAMAGE).getValue();
            int iceDamageToAdd = (int)(iceDamage * (intellect * .0005));
            if(iceDamageToAdd > 0)getAttributes().addStat(WeaponAttributeType.ICE_DAMAGE, iceDamageToAdd);

            int poisonDamage = getAttributes().getAttribute(WeaponAttributeType.POISON_DAMAGE).getValue();
            int poisonDamageToAdd = (int)(poisonDamage * (intellect * .0005));
            if(poisonDamageToAdd > 0)getAttributes().addStat(WeaponAttributeType.POISON_DAMAGE, poisonDamageToAdd);*/

            if (getAttributes().hasAttribute(WeaponAttributeType.FIRE_DAMAGE))
                getAttributes().addStat(WeaponAttributeType.FIRE_DAMAGE, (int) (intellect * .05));
            else if (getAttributes().hasAttribute(WeaponAttributeType.ICE_DAMAGE))
                getAttributes().addStat(WeaponAttributeType.ICE_DAMAGE, (int) (intellect * .05));
            else if (getAttributes().hasAttribute(WeaponAttributeType.POISON_DAMAGE))
                getAttributes().addStat(WeaponAttributeType.POISON_DAMAGE, (int) (intellect * .05));
        }
        HealthHandler.updatePlayerHP(getPlayer());

        // so energy regen doesn't start before attributes have been loaded
        this.attributesLoaded = true;
    }

    public boolean hasEcash(int ecash) {
        return getEcash() >= ecash;
    }

    public void setAlignment(EnumPlayerAlignments alignmentTo) {
        if (alignmentTo == null)
            alignmentTo = EnumPlayerAlignments.LAWFUL;

        if (alignmentTo != getAlignment()) {
            player.sendMessage("");
            player.sendMessage(alignmentTo.getColor() + "              * YOU ARE NOW " + ChatColor.BOLD + ChatColor.UNDERLINE + alignmentTo.name() + alignmentTo.getColor() + " ALIGNMENT *");
            player.sendMessage(ChatColor.GRAY + alignmentTo.getLongDescription());
            player.sendMessage("");
        }

        setAlignmentTime(Math.min(getAlignmentTime() + alignmentTo.getTimer(), alignmentTo.getMaxTimer()));

        if (getAlignment() != alignmentTo) {
            this.alignment = alignmentTo;
            ScoreboardHandler.getInstance().updatePlayerName(getPlayer());
        }
    }

    public void setActiveTrail(ParticleEffect e) {
        this.activeTrail = e;

        if (e == null) {
            disableTrail();
        } else {
            DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(getPlayer(), e);
        }
    }

    public void disableTrail() {
        DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.remove(getPlayer());
    }

    public boolean hasEffectUnlocked(ParticleEffect effect) {
        return getParticles().contains(effect);
    }

    /**
     * Is this player vulnerable to damage?
     */
    public boolean isVulnerable() {
        return getPlayer().getGameMode() == GameMode.SURVIVAL && !getToggles().getState(Toggles.INVULNERABLE) && !getPlayer().isInvulnerable();
    }

    /**
     * Gets our display name.
     * Contains our rank and our username, and alignment.
     */
    public String getDisplayName() {
        PlayerRank rank = getRank();
        ChatColor nameColor = rank.isAtLeast(PlayerRank.TRIALGM) ? ChatColor.AQUA : getAlignment().getNameColor();
        return rank.getChatPrefix() + nameColor + (isOnline() ? getPlayer().getName() : getUsername());
    }

    /**
     * Gets our prefixed chat name.
     */
    public String getChatName() {
        String name = getDisplayName();
        if (isInGuild() && getGuild() != null && getGuild().isAnAcceptedPlayer(getUuid()))
            name = getGuild().getChatPrefix() + name;

        return name;
    }

    @SneakyThrows
    public void loadMuleInventory(ResultSet set) {
        String invString = set.getString("characters.mule_storage");
        muleLevel = Math.max(set.getInt("characters.mule_level"), 1);
        MuleTier tier = MuleTier.getByTier(muleLevel);
        if (tier != null) {
            this.pendingMuleInventory = Bukkit.createInventory(null, tier.getSize(), "Mule Storage");
            if (invString != null && !invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4) {
                //Make sure the inventory is as big as we need
                this.pendingMuleInventory = ItemSerialization.fromString(invString, tier.getSize());
            }
        }
    }

    public String getEquipmentString(Player player) {

        int itemsSaved = 0;
        Inventory toSave = Bukkit.createInventory(null, 9);
        for (int index = 0; index < 4; index++) {
            ItemStack equipment = player.getEquipment().getArmorContents()[index];
            toSave.setItem(index, equipment);
            if (equipment != null && equipment.getType() != Material.AIR)
                itemsSaved++;
        }

        if (player.getInventory().getItemInOffHand() != null && player.getInventory().getItemInOffHand().getType() != Material.AIR)
            itemsSaved++;

        toSave.setItem(4, player.getInventory().getItemInOffHand());

        return itemsSaved == 0 ? null : ItemSerialization.toString(toSave);
    }

    public String getEquipmentString(Inventory inv) {
        int itemsSaved = 0;
        Inventory toSave = Bukkit.createInventory(null, 9);
        for (int index = 0; index < 5; index++) {
            ItemStack equipment = inv.getContents()[index];
            toSave.setItem(index, equipment);
//            toSave.getContents()[index] = equipment;
            if (equipment != null && equipment.getType() != Material.AIR) {
                itemsSaved++;
            }
        }

        return itemsSaved == 0 ? null : ItemSerialization.toString(toSave);
    }

    public void loadPlayerAfterLogin(Player player) {
        this.player = player;
        player.teleport(this.storedLocation);
        this.loadPlayerInventory(player);
        this.loadPlayerArmor(player);

        //Only apply this storages to the cache when they join and are online...
        BankMechanics.storage.put(uuid, this.pendingBankStorage);
        //Actually apply this on login.
        if (this.pendingMuleInventory != null)
            MountUtils.getInventories().put(uuid, pendingMuleInventory);

        if (this.activeTrail != null) {
            setActiveTrail(this.activeTrail);
        }
        if (this.activeHatOverride != null)
            setActiveHatOverride(this.activeHatOverride);

        if (pendingParticleEffectString != null && pendingParticleEffectString.length() > 0 && !pendingParticleEffectString.equalsIgnoreCase("null")) {
            setActiveSpecialEffect(SpecialParticles.constrauctEffectFromName(pendingParticleEffectString, player));
        }
    }

    public void loadPlayerInventory(Player player) {
        if (pendingInventory == null) return;
        player.getInventory().setContents(pendingInventory.getContents());
        pendingInventory = null;
    }

    public void loadPlayerArmor(Player player) {
        if (pendingArmor == null) return;
        ItemStack[] items = new ItemStack[4];
        for (int index = 0; index < 4; index++) {
            //We are doing 5 for the new shield slot.
            ItemStack current = pendingArmor.getContents()[index];

            items[index] = current;
        }
        player.getEquipment().setArmorContents(items);
        player.getInventory().setItemInOffHand(pendingArmor.getContents()[4]);

        player.updateInventory();
    }

    /**
     * FRIENDS STUFF
     **/

    public void ignorePlayer(UUID uuidToIgnore, boolean alreadyIgnored) {
        int accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuidToIgnore);
        if (alreadyIgnored) {
            this.ignoredFriends.remove(uuidToIgnore);
            SQLDatabaseAPI.getInstance().addQuery(QueryType.INSERT_FRIENDS, this.accountID, accountID, "blocked", "blocked");
        } else {
            this.pendingFriends.remove(uuidToIgnore);
            this.ignoredFriends.put(uuidToIgnore, accountID);
            this.friendsList.remove(uuidToIgnore);
            SQLDatabaseAPI.getInstance().addQuery(QueryType.INSERT_FRIENDS, this.accountID, accountID, "blocked", "blocked");

        }
    }

    public EnumMounts getHighestHorseUnlocked() {
        List<EnumMounts> mounts = Lists.newArrayList(EnumMounts.values());
        EnumMounts currentHighest = null;
        for (EnumMounts mount : mounts) {
            if (mount.getHourseTierNumber() <= 0) continue;
            if (!getMountsUnlocked().contains(mount)) continue;
            if (currentHighest == null) {
                currentHighest = mount;
                continue;
            }
            if (mount.getHourseTierNumber() <= currentHighest.getHourseTierNumber()) continue;
            currentHighest = mount;
        }
        return currentHighest;
    }

    public MuleTier getMuleTier() {
        MuleTier tier = MuleTier.getByTier(getMuleLevel());
        return tier == null ? MuleTier.OLD : tier;
    }

    public void saveFriends(boolean async, Consumer<Boolean> afterSave) {
        if (friendsList.size() == 0 && ignoredFriends.size() == 0 && pendingFriends.size() == 0) return;
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> saveFriends(false, afterSave), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }

        boolean couldSave = true;
        try {
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("");
            getFriendUpdateQuery(statement);
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            couldSave = false;
        }

        if (afterSave != null) afterSave.accept(couldSave);
    }

    public boolean isBanned() {
        return this.banExpire >= System.currentTimeMillis() || this.banExpire == 0;
    }

    public boolean isMuted() {
        return this.muteExpire >= System.currentTimeMillis() || this.muteExpire == 0;
    }

    public String getTimeWhenBanExpires() {
        return TimeUtil.formatDifference((this.banExpire - System.currentTimeMillis()) / 1_000);
    }

    public String getTimeWhenMuteExpires() {
        return TimeUtil.formatDifference((this.muteExpire - System.currentTimeMillis()) / 1_000);
    }

    public void addExperience(int experienceToAdd, boolean isParty, boolean displayMessage, boolean giveBonus) {
        int level = getLevel();
        if (level >= 100 || experienceToAdd <= 0)
            return;

        int experience = getExperience();
        String expPrefix = ChatColor.YELLOW.toString() + ChatColor.BOLD + "        + ";
        if (isParty)
            expPrefix = ChatColor.YELLOW.toString() + ChatColor.BOLD + "            " + ChatColor.AQUA.toString() + ChatColor.BOLD + "P " + ChatColor.RESET + ChatColor.GRAY + " >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD + "+";

        // Bonuses
        int expBonus = 0;
        if (giveBonus) {
            if (getRank().isSubPlus()) {
                expBonus = (int) (experienceToAdd * 0.1);
            } else if (getRank().isSUB()) {
                expBonus = (int) (experienceToAdd * 0.05);
            }
        }

        int futureExperience = experience + experienceToAdd + expBonus;
        int levelBuffBonus = 0;

        if (DonationEffects.getInstance().hasBuff(EnumBuff.LEVEL)) {
            Buff levelBuff = DonationEffects.getInstance().getBuff(EnumBuff.LEVEL);
            levelBuffBonus = Math.round(experienceToAdd * (levelBuff.getBonusAmount() / 100f));
            experienceToAdd += levelBuffBonus;
        }

        int xpNeeded = getEXPNeeded(level);
        if (futureExperience >= xpNeeded) {
            int continuedExperience = futureExperience - xpNeeded;
            updateLevel(level + 1, true);
            addExperience(continuedExperience, isParty, displayMessage, giveBonus);
        } else {
            setExperience(futureExperience);
            if (displayMessage) {
                sendDebug(expPrefix + ChatColor.YELLOW + Math.round(experienceToAdd) + ChatColor.BOLD + " EXP " + ChatColor.GRAY + "[" + Math.round(futureExperience - expBonus - levelBuffBonus) + ChatColor.BOLD + "/" + ChatColor.GRAY + Math.round(getEXPNeeded(level)) + " EXP]");
                if (expBonus > 0)
                    getPlayer().sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "        " + getRank().getPrefix() + ChatColor.RESET + ChatColor.GRAY + " >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD + "+" + ChatColor.YELLOW + Math.round(expBonus) + ChatColor.BOLD + " EXP " + ChatColor.GRAY + "[" + Math.round(futureExperience - levelBuffBonus) + ChatColor.BOLD + "/" + ChatColor.GRAY + Math.round(getEXPNeeded(level)) + " EXP]");

                if (levelBuffBonus > 0)
                    getPlayer().sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "        " + ChatColor.GOLD
                            .toString() + ChatColor.BOLD + "XP BUFF >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD
                            + "+" + ChatColor.YELLOW + Math.round(levelBuffBonus) + ChatColor.BOLD + " EXP " +
                            ChatColor.GRAY + "[" + Math.round(futureExperience) + ChatColor.BOLD + "/" +
                            ChatColor.GRAY + Math.round(getEXPNeeded(level)) + " EXP]");
            }
        }
    }

    public int getEXPNeeded() {
        return getEXPNeeded(getLevel());
    }

    public int getEXPNeeded(int level) {
        if (level >= 101) {
            return 0;
        }
        double difficulty = 1;
        if (level >= 1 && level < 40) {
            difficulty = 1.3;
        } else if (level >= 40 && level < 60) {
            difficulty = 1.6;
        } else if (level >= 60 && level < 80) {
            difficulty = 2.2;
        } else if (level >= 80) {
            difficulty = 2.6;
        }

        return (int) ((100 * Math.pow(level, 2)) * difficulty + 500);
    }

    public void withdrawEcash(int ecash) {
        setEcash(getEcash() - ecash);
        if (player != null)
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "- " + ChatColor.RED + ecash + ChatColor.BOLD + " E-Cash");
    }

    public void withdrawGems(int cash) {
        setGems(getGems() - cash);
        if (player != null)
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "- " + ChatColor.RED + gems + ChatColor.BOLD + " GEM(s)");
    }

    public void setEcash(int ecash) {
        if (ecash < 0 || ecash > 1_000_000) {
            GameAPI.sendWarning("Tried to set " + getPlayer().getName() + "'s E-Cash to " + ecash + " on shard {SERVER}.");
            ecash = 0;
        }

        if (ecash != getEcash())
            SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_ECASH, ecash, getAccountID());

        this.ecash = ecash;
    }

    public void updateLevel(int newLevel, boolean natural) {
        setExperience(0);
        setLevel(getLevel() + 1);

        if (natural && newLevel == getLevel()) { // natural level up
            getPlayerStats().lvlUp();

            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, .4F);
            getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1F);

            Firework firework = (Firework) getPlayer().getLocation().getWorld().spawnEntity(getPlayer().getLocation().clone(), EntityType.FIREWORK);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(Color.LIME).withFade(Color.WHITE).with(FireworkEffect.Type.BALL_LARGE).trail(true).build();
            fireworkMeta.addEffect(effect);
            fireworkMeta.setPower(1);
            firework.setFireworkMeta(fireworkMeta);

            getPlayer().sendMessage("");
            Utils.sendCenteredMessage(getPlayer(), ChatColor.AQUA.toString() + ChatColor.BOLD + "******************************");
            Utils.sendCenteredMessage(getPlayer(), ChatColor.GREEN.toString() + ChatColor.BOLD + "LEVEL UP");
            getPlayer().sendMessage("");
            Utils.sendCenteredMessage(getPlayer(), ChatColor.GRAY + "You are now level: " + ChatColor.GREEN + newLevel);
            Utils.sendCenteredMessage(getPlayer(), ChatColor.GRAY + "EXP to next level: " + ChatColor.GREEN + getEXPNeeded(newLevel));
            Utils.sendCenteredMessage(getPlayer(), ChatColor.GRAY + "Free stat points: " + ChatColor.GREEN + getPlayerStats().getFreePoints());
            Utils.sendCenteredMessage(getPlayer(), ChatColor.AQUA.toString() + ChatColor.BOLD + "******************************");
            getPlayer().sendMessage("");

        } else { // level was set
            setLevel(newLevel);
            Utils.sendCenteredMessage(getPlayer(), ChatColor.YELLOW + "Your level has been set to: " + ChatColor.LIGHT_PURPLE + newLevel);
            getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
        }

        ScoreboardHandler.getInstance().updatePlayerName(getPlayer());

        for (EnumAchievementLevel ael : EnumAchievementLevel.values())
            if (ael.getLevelRequirement() == newLevel)
                Achievements.giveAchievement(player, ael.getAchievement());
    }

    public String getSerializedPendingPurchaseables() {
        StringBuilder toReturn = new StringBuilder("");
        for (int k = 0; k < getPendingPurchaseablesUnlocked().size(); k++) {
            PendingPurchaseable item = getPendingPurchaseablesUnlocked().get(k);
            toReturn.append(item.toString());
            if (k < (getPendingPurchaseablesUnlocked().size() - 1)) toReturn.append("%&&%");
        }


        return toReturn.toString();
    }

    public boolean loadPendingPurchaseables(String serialized) {
        List<PendingPurchaseable> purchases = new ArrayList<>();
        if (serialized == null || serialized.isEmpty() || serialized == "null") {
            this.pendingPurchaseablesUnlocked = purchases;
            return true;//They dont have any.
        }
        String[] contents = serialized.split("%&&%");
        for (String part : contents) {
            if (part == null || part.isEmpty()) continue;
            try {
                PendingPurchaseable toAdd = PendingPurchaseable.fromString(part);
                if (toAdd == null) {
                    Constants.log.info("An error occurred while parsing " + getUsername() + "'s pending purchaseables for string: " + part + " with the whole: " + serialized);
                    return false;
                }
                purchases.add(toAdd);
            } catch (Exception e) {
                e.printStackTrace();
                Constants.log.info("2 An error occurred while parsing " + getUsername() + "'s pending purchaseables for string: " + part + " with the whole: " + serialized);
                return false;
            }
        }
        this.pendingPurchaseablesUnlocked = purchases;
        return true;
    }

    public String getAttribute(Item.AttributeType type) {
        return getAttributes().getAttribute(type).toString();
    }

    public void updatePurchaseLog(String action, String transaction_id, long date, String uuid) {
        SQLDatabaseAPI.getInstance().executeUpdate(null, getQuery(QueryType.INSERT_PURCHASE_LOG, action, transaction_id, date, uuid), true);
    }

    public static final void tickSpecialEffects() {
        for (PlayerWrapper wrapper : playerWrappers.values()) {
            if (wrapper.getActiveSpecialEffect() == null) continue;
            if (!wrapper.getActiveSpecialEffect().canTick()) continue;
            Player player = wrapper.getPlayer();
            if (player == null || !player.isOnline()) continue;
            long lastMovement = wrapper.getLastBlockMovement();
            if (!wrapper.getActiveSpecialEffect().tickWhileMoving() && System.currentTimeMillis() - lastMovement < 1000)
                continue;

            wrapper.getActiveSpecialEffect().tick();
        }
    }

    public void changeIndependentColor() {
        if (lastIndependantColor.equals(DyeColor.RED)) lastIndependantColor = DyeColor.WHITE;
        else if (lastIndependantColor.equals(DyeColor.WHITE)) lastIndependantColor = DyeColor.BLUE;
        else lastIndependantColor = DyeColor.RED;
    }

    private String getSerializedDungeonCooldownString() {
        if (getLastDungeonRuns().isEmpty()) return null;
        String toReturn = "";
        for (Map.Entry<String, Long> entry : getLastDungeonRuns().entrySet()) {
            if (!toReturn.isEmpty()) toReturn = toReturn + "%";
            toReturn = toReturn + entry.getKey() + ";" + entry.getValue().longValue();
        }


        return toReturn;
    }

    private void loadLastDungeonRuns(String raw) {
        if (raw == null || raw.isEmpty()) return;
        String[] entries = raw.split("%");
        for (String entry : entries) {
            String[] parts = entry.split(";");
            getLastDungeonRuns().put(parts[0], Long.valueOf(parts[1]));
        }
    }


}
