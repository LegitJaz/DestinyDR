package net.dungeonrealms.game.mongo;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nick on 8/29/2015.
 */
public class DatabaseAPI {

    private static DatabaseAPI instance = null;

    public static DatabaseAPI getInstance() {
        if (instance == null) {
            instance = new DatabaseAPI();
        }
        return instance;
    }

    public volatile ConcurrentHashMap<UUID, Document> PLAYERS = new ConcurrentHashMap<>();
    public volatile ConcurrentHashMap<UUID, Integer> PLAYER_TIME = new ConcurrentHashMap<>();

    /**
     * Updates a players information in Mongo and returns the updated result.
     *
     * @param uuid
     * @param EO
     * @param variable
     * @param object
     * @param requestNew TRUE = WILL GET NEW DATA FROM MONGO.
     * @since 1.0
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean requestNew) {
        Database.collection.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable.getKey(), object)));
        if (requestNew) {
            requestPlayer(uuid);
        }
                /*(result, exception) -> {
                    Utils.log.info("DatabaseAPI update() called ...");
                    if (exception == null && requestNew) {
                        requestPlayer(uuid);
                    }
                });*/
    }

    /**
     * Returns the object that's requested.
     *
     * @param data
     * @param uuid
     * @return
     * @since 1.0
     */
    public Object getData(EnumData data, UUID uuid) {

        Document doc;

        if (PLAYERS.containsKey(uuid)) {
            // GRABBED CACHED DATA
            doc = PLAYERS.get(uuid);

        } else {
            doc = Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        }

        switch (data) {
            /*
            Player Variables Main Document().
             */
            case USERNAME:
                return ((Document) doc.get("info")).get("username", String.class);
            case HEALTH:
                return ((Document) doc.get("info")).get("health", Integer.class);
            case FIRST_LOGIN:
                return ((Document) doc.get("info")).get("firstLogin", Long.class);
            case LAST_LOGIN:
                return ((Document) doc.get("info")).get("lastLogin", Long.class);
            case LAST_LOGOUT:
                return ((Document) doc.get("info")).get("lastLogout", Long.class);
            case IS_PLAYING:
                return ((Document) doc.get("info")).get("isPlaying", Boolean.class);
            case LEVEL:
                return ((Document) doc.get("info")).get("netLevel", Integer.class);
            case EXPERIENCE:
                return ((Document) doc.get("info")).get("experience", Integer.class);
            case GEMS:
                return ((Document) doc.get("info")).get("gems", Integer.class);
            case HEARTHSTONE:
                return ((Document) doc.get("info")).get("hearthstone", String.class);
            case ECASH:
                return ((Document) doc.get("info")).get("ecash", Integer.class);
            case FRIENDS:
                return ((Document) doc.get("info")).get("friends", ArrayList.class);
            case GUILD:
                return ((Document) doc.get("info")).get("guild", String.class);
            case GUILD_INVITATION:
                return ((Document) doc.get("notices")).get("guildInvitation", Document.class);
            case FRIEND_REQUSTS:
                return ((Document) doc.get("notices")).get("friendRequest", ArrayList.class);
            case MAILBOX:
                return ((Document) doc.get("notices")).get("mailbox", ArrayList.class);
            case ALIGNMENT:
                return ((Document) doc.get("info")).get("alignment", String.class);
            case ALIGNMENT_TIME:
                return ((Document) doc.get("info")).get("alignmentTime", Integer.class);
            case CURRENT_LOCATION:
                return ((Document) doc.get("info")).get("currentLocation", String.class);
            case CURRENT_FOOD:
                return ((Document) doc.get("info")).get("foodLevel", Integer.class);
            case SHOPLEVEL:
                return ((Document) doc.get("info")).get("shopLevel", Integer.class);
            case MULELEVEL:
                return ((Document) doc.get("info")).get("muleLevel", Integer.class);
            case LOGGERDIED:
                return ((Document) doc.get("info")).get("loggerdied", Boolean.class);
            case CURRENTSERVER:
                return ((Document) doc.get("info")).get("current", String.class);
            case ENTERINGREALM:
                return ((Document) doc.get("info")).get("enteringrealm", String.class);
            case ACTIVE_MOUNT:
                return ((Document) doc.get("info")).get("activemount", String.class);
            case ACTIVE_PET:
                return ((Document) doc.get("info")).get("activepet", String.class);
            case ACTIVE_TRAIL:
                return ((Document) doc.get("info")).get("activetrail", String.class);
            case ACTIVE_MOUNT_SKIN:
                return ((Document) doc.get("info")).get("activemountskin", String.class);
            /*
            Rank Things. Different Sub-Document().
             */
            case RANK:
                return ((Document) doc.get("rank")).get("rank", String.class);
            case RANK_EXISTENCE:
                return ((Document) doc.get("rank")).get("lastPurchase", Long.class);
            case PURCHASE_HISTORY:
                return ((Document) doc.get("rank")).get("purchaseHistory", ArrayList.class);
            /*
            Player Attribute Variables
             */
            case STRENGTH:
                return ((Document) doc.get("attributes")).get("strength", Integer.class);
            case DEXTERITY:
                return ((Document) doc.get("attributes")).get("dexterity", Integer.class);
            case INTELLECT:
                return ((Document) doc.get("attributes")).get("intellect", Integer.class);
            case VITALITY:
                return ((Document) doc.get("attributes")).get("vitality", Integer.class);
            case BUFFER_POINTS:
                return ((Document) doc.get("attributes")).get("bufferPoints", Integer.class);
            case RESETS:
                return ((Document) doc.get("attributes")).get("resets", Integer.class);
            case FREERESETS:
                return ((Document) doc.get("attributes")).get("freeresets", Integer.class);
            /*
            Player Storage
             */
            case INVENTORY_LEVEL:
                return ((Document) doc.get("inventory")).get("level", Integer.class);
            case INVENTORY_COLLECTION_BIN:
                return ((Document) doc.get("inventory")).get("collection_bin", String.class);
            case INVENTORY_MULE:
                return ((Document) doc.get("inventory")).get("mule", String.class);
            case INVENTORY_STORAGE:
                return ((Document) doc.get("inventory")).get("storage", String.class);
            case INVENTORY:
                return ((Document) doc.get("inventory")).get("player", String.class);
            case HASSHOP:
                return ((Document) doc.get("info")).get("shopOpen", Boolean.class);
            case ARMOR:
                return ((Document) doc.get("inventory")).get("armor", ArrayList.class);
            /*
            Toggles
             */
            case TOGGLE_DEBUG:
                return ((Document) doc.get("toggles")).get("debug", Boolean.class);
            case TOGGLE_TRADE:
                return ((Document) doc.get("toggles")).get("trade", Boolean.class);
            case TOGGLE_TRADE_CHAT:
                return ((Document) doc.get("toggles")).get("tradeChat", Boolean.class);
            case TOGGLE_GLOBAL_CHAT:
                return ((Document) doc.get("toggles")).get("globalChat", Boolean.class);
            case TOGGLE_RECEIVE_MESSAGE:
                return ((Document) doc.get("toggles")).get("receiveMessage", Boolean.class);
            case TOGGLE_PVP:
                return ((Document) doc.get("toggles")).get("pvp", Boolean.class);
            case TOGGLE_DUEL:
                return ((Document) doc.get("toggles")).get("duel", Boolean.class);
            case TOGGLE_CHAOTIC_PREVENTION:
                return ((Document) doc.get("toggles")).get("chaoticPrevention", Boolean.class);
            /*
            Portal Key Shards
             */
            case PORTAL_SHARDS_T1:
                return ((Document) doc.get("portalKeyShards")).get("tier1", Integer.class);
            case PORTAL_SHARDS_T2:
                return ((Document) doc.get("portalKeyShards")).get("tier2", Integer.class);
            case PORTAL_SHARDS_T3:
                return ((Document) doc.get("portalKeyShards")).get("tier3", Integer.class);
            case PORTAL_SHARDS_T4:
                return ((Document) doc.get("portalKeyShards")).get("tier4", Integer.class);
            case PORTAL_SHARDS_T5:
                return ((Document) doc.get("portalKeyShards")).get("tier5", Integer.class);
            /*
            Player Collectibles
             */
            case MOUNTS:
                return ((Document) doc.get("collectibles")).get("mounts", ArrayList.class);
            case PETS:
                return ((Document) doc.get("collectibles")).get("pets", ArrayList.class);
            case PARTICLES:
                return ((Document) doc.get("collectibles")).get("particles", ArrayList.class);
            case ACHIEVEMENTS:
                return ((Document) doc.get("collectibles")).get("achievements", ArrayList.class);
            case MOUNT_SKINS:
                return ((Document) doc.get("collectibles")).get("mountskins", ArrayList.class);
            default:
        }
        return null;
    }

    /**
     * Starts the Initialization of DatabaseAPI.
     *
     * @since 1.0
     */
    public void startInitialization() {
    }

    /**
     * Is fired to grab a player from Mongo
     * if they don't exist. Fire addNewPlayer() creation.
     *
     * @param uuid
     * @since 1.0
     */
    public void requestPlayer(UUID uuid) {
        Document doc = Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        if (doc == null) {
            addNewPlayer(uuid);
        } else {
            PLAYERS.put(uuid, doc);
        }
        /*Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first((document) -> {
            if (document != null) {
                Utils.log.info("Fetched information for uuid: " + uuid.toString());
                PLAYERS.put(uuid, document);
            } else {
                addNewPlayer(uuid);
            }
        });*/
    }

    public String getUUIDFromName(String playerName) {
        Document doc = Database.collection.find(Filters.eq("info.username", playerName.toLowerCase())).first();
        if (doc == null) {
            return "";
        } else {
            return ((Document) doc.get("info")).get("uuid", String.class);
        }
    }

    public String getOfflineName(UUID uuid) {
        Document doc = Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        if (doc == null) {
            return "";
        } else {
            return ((Document) doc.get("info")).get("username", String.class);
        }
    }

    /**
     * Adds a new player to Mongo Creates Document here.
     *
     * @param uuid
     * @since 1.0
     */

    private void addNewPlayer(UUID uuid) {
        Document newPlayerDocument =
                new Document("info",
                        new Document("uuid", uuid.toString())
                                .append("username", "")
                                .append("health", 50)
                                .append("gems", 0)
                                .append("ecash", 0)
                                .append("firstLogin", System.currentTimeMillis() / 1000L)
                                .append("lastLogin", 0L)
                                .append("lastLogout", 0L)
                                .append("netLevel", 1)
                                .append("experience", 0)
                                .append("hearthstone", "Cyrennica")
                                .append("currentLocation", "")
                                .append("isPlaying", true)
                                .append("friends", new ArrayList<>())
                                .append("alignment", "lawful")
                                .append("alignmentTime", 0)
                                .append("guild", "")
                                .append("shopOpen", false)
                                .append("foodLevel", 20)
                                .append("shopLevel", 1)
                                .append("muleLevel", 1)
                                .append("loggerdied", false)
                                .append("current", DungeonRealms.getInstance().bungeeName)
                                .append("enteringrealm", "")
                                .append("activepet", "")
                                .append("activemount", "")
                                .append("activetrail", "")
                                .append("activemountskin", ""))
                        .append("attributes",
                                new Document("bufferPoints", 6)
                                        .append("strength", 0)
                                        .append("dexterity", 0)
                                        .append("intellect", 0)
                                        .append("vitality", 0)
                                        .append("resets", 0)
                                        .append("freeresets", 0))
                        .append("collectibles",
                                new Document("achievements", new ArrayList<String>())
                                        .append("mounts", new ArrayList<String>())
                                        .append("pets", new ArrayList<String>())
                                        .append("particles", new ArrayList<String>())
                                        .append("mountskins", new ArrayList<String>()))
                        .append("toggles",
                                new Document("debug", false)
                                        .append("trade", false)
                                        .append("tradeChat", false)
                                        .append("globalChat", false)
                                        .append("receiveMessage", true)
                                        .append("pvp", false)
                                        .append("duel", true)
                                        .append("chaoticPrevention", true))
                        .append("portalKeyShards",
                                new Document("tier1", 0)
                                        .append("tier2", 0)
                                        .append("tier3", 0)
                                        .append("tier4", 0)
                                        .append("tier5", 0))
                        .append("notices",
                                new Document("guildInvitation", null)
                                        .append("friendRequest", new ArrayList<String>())
                                        .append("mailbox", new ArrayList<String>()))
                        .append("rank",
                                new Document("lastPurchase", 0L)
                                        .append("purchaseHistory", new ArrayList<String>())
                                        .append("rank", "DEFAULT"))
                        .append("inventory",
                                new Document("collection_bin", "")
                                        .append("mule", "empty")
                                        .append("storage", "")
                                        .append("level", 1)
                                        .append("player", "")
                                        .append("armor", new ArrayList<String>()));
        Database.collection.insertOne(newPlayerDocument);
        requestPlayer(uuid);
        Utils.log.info("Requesting new data for : " + uuid);
    }
}
