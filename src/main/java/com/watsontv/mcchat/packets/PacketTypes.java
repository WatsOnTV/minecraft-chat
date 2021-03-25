package com.watsontv.mcchat.packets;

@SuppressWarnings("unused")
/* Documents the packet types. Note that the majority of these are unused, but included for future versions. */
/* Accurate to version: 1.16.4 */
public final class PacketTypes {
    /** HANDSHAKING state */
    // Server Bound
    public static final int HANDSHAKE             = 0x00;

    /** STATUS State */
    // Server Bound
    public static final int REQUEST               = 0x00;
    public static final int PING                  = 0x01;
    // Client Bound
    public static final int RESPONSE              = 0x00;
    public static final int PONG                  = 0x01;

    /** LOGIN State */
    // Server Bound
    public static final int LOGIN_START           = 0x00;
    public static final int ENCRYPTION_RESPONSE   = 0x01;
    public static final int PLUGIN_RESPONSE       = 0x02;
    // Client Bound
    public static final int ENCRYPTION_REQUEST    = 0x01;
    public static final int LOGIN_SUCCESS         = 0x02;
    public static final int SET_COMPRESSION       = 0x03;
    public static final int PLUGIN_REQUEST        = 0x04;

    /** PLAY State */
    // Server Bound
    public static final int TELEPORT_CONFIRM      = 0x00;
    public static final int QUERY_BLOCK_NBT       = 0x01;
    public static final int QUERY_ENTITY_NTB      = 0x0D;
    public static final int SET_DIFFICULTY        = 0x02;
    public static final int CHAT_MESSAGE_SB       = 0x03;
    public static final int CLIENT_STATUS         = 0x04;
    public static final int CLIENT_SETTINGS       = 0x05;
    public static final int TAB_COMPLETE_SB       = 0x06;
    public static final int WINDOW_CONFIRM_SB     = 0x07;
    public static final int CLICK_WINDOW_BUTTON   = 0x08;
    public static final int CLICK_WINDOW          = 0x09;
    public static final int CLOSE_WINDOW_SB       = 0x0A;
    public static final int PLUGIN_MESSAGE_SB     = 0x0B;
    public static final int EDIT_BOOK             = 0x0C;
    public static final int INTERACT_ENTITY       = 0x0E;
    public static final int GENERATE_STRUCTURE    = 0x0F;
    public static final int KEEP_ALIVE_SB         = 0x10;
    public static final int LOCK_DIFFICULTY       = 0x11;
    public static final int PLAYER_POSITION       = 0x12;
    public static final int PLAYER_POS_ROTATE     = 0x13;
    public static final int PLAYER_ROTATION       = 0x14;
    public static final int PLAYER_MOVEMENT       = 0x15;
    public static final int VEHICLE_MOVEMENT_SB   = 0x16;
    public static final int STEER_BOAT            = 0x17;
    public static final int PICK_ITEM             = 0x18;
    public static final int CRAFT_RECIPE_REQUEST  = 0x19;
    public static final int PLAYER_ABILITIES_SB   = 0x1A;
    public static final int PLAYER_DIG            = 0x1B;
    public static final int ENTITY_ACTION         = 0x1C;
    public static final int STEER_VEHICLE         = 0x1D;
    public static final int SET_RECIPE_BOOK_STATE = 0x1E;
    public static final int SET_DISPLAYED_RECIPE  = 0x1F;
    public static final int NAME_ITEM             = 0x20;
    public static final int RESOURCE_PACK_STATUS  = 0x21;
    public static final int ADVANCEMENT_TAB       = 0x22;
    public static final int SELECT_TRADE          = 0x23;
    public static final int SET_BEACON_EFFECT     = 0x24;
    public static final int HELD_ITEM_CHANGE_SB   = 0x25;
    public static final int UPDATE_COMMAND_BLOCK  = 0x26;
    public static final int UPDATE_COMMAND_CART   = 0x27;
    public static final int CREATIVE_INV_ACTION   = 0x28;
    public static final int UPDATE_JIGSAW_BLOCK   = 0x29;
    public static final int UPDATE_STRUCT_BLOCK   = 0x2A;
    public static final int UPDATE_SIGN           = 0x2B;
    public static final int ANIMATION_SB          = 0x2C;
    public static final int SPECTATE              = 0x2D;
    public static final int PLAYER_BLOCK_PLACE    = 0x2E;
    public static final int USE_ITEM              = 0x2F;
    // Client Bound
    public static final int SPAWN_ENTITY          = 0x00;
    public static final int SPAWN_EXP_ORB         = 0x01;
    public static final int SPAWN_LIVING_ENTITY   = 0x02;
    public static final int SPAWN_PAINTING        = 0x03;
    public static final int SPAWN_PLAYER          = 0x04;
    public static final int ENTITY_ANIMATION      = 0x05;
    public static final int STATISTICS            = 0x06;
    public static final int ACK_PLAYER_DIG        = 0x07;
    public static final int BLOCK_BREAK_ANIMATION = 0x08;
    public static final int BLOCK_ENTITY_DATA     = 0x09;
    public static final int BLOCK_ACTION          = 0x0A;
    public static final int BLOCK_CHANGE          = 0x0B;
    public static final int BOSS_BAR              = 0x0C;
    public static final int SERVER_DIFFICULTY     = 0x0D;
    public static final int CHAT_MESSAGE_CB       = 0x0E;
    public static final int TAB_COMPLETE_CB       = 0x0F;
    public static final int DECLARE_COMMANDS      = 0x10;
    public static final int WINDOW_CONFIRM_CB     = 0x11;
    public static final int CLOSE_WINDOW_CB       = 0x12;
    public static final int WINDOW_ITEMS          = 0x13;
    public static final int WINDOW_PROPERTY       = 0x14;
    public static final int SET_SLOT              = 0x15;
    public static final int SET_COOLDOWN          = 0x16;
    public static final int PLUGIN_MESSAGE_CB     = 0x17;
    public static final int NAMED_SOUND_EFFECT    = 0x18;
    public static final int DISCONNECT            = 0x19;
    public static final int ENTITY_STATUS         = 0x1A;
    public static final int EXPLOSION             = 0x1B;
    public static final int UNLOAD_CHUNK          = 0x1C;
    public static final int CHANGE_GAME_STATE     = 0x1D;
    public static final int OPEN_HORSE_WINDOW     = 0x1E;
    public static final int KEEP_ALIVE_CB         = 0x1F;
    public static final int CHUNK_DATA            = 0x20;
    public static final int EFFECT                = 0x21;
    public static final int PARTICLE              = 0x22;
    public static final int UPDATE_LIGHT          = 0x23;
    public static final int JOIN_GAME             = 0x24;
    public static final int MAP_DATA              = 0x25;
    public static final int TRADE_LIST            = 0x26;
    public static final int ENTITY_POS            = 0x27;
    public static final int ENTITY_POS_ROTATION   = 0x28;
    public static final int ENTITY_ROTATION       = 0x29;
    public static final int ENTITY_MOVEMENT       = 0x2A;
    public static final int VEHICLE_MOVEMENT_CB   = 0x2B;
    public static final int OPEN_BOOK             = 0x2C;
    public static final int OPEN_WINDOW           = 0x2D;
    public static final int OPEN_SIGN_EDITOR      = 0x2E;
    public static final int CRAFT_RECIPE_RESPONSE = 0x2F;
    public static final int PLAYER_ABILITIES_CB   = 0x30;
    public static final int COMBAT_EVENT          = 0x31;
    public static final int PLAYER_INFO           = 0x32;
    public static final int FACE_PLAYER           = 0x33;
    public static final int PLAYER_POS_AND_LOOK   = 0x34;
    public static final int UNLOCK_RECIPES        = 0x35;
    public static final int DESTROY_ENTITIES      = 0x36;
    public static final int REMOVE_ENTITY_EFFECT  = 0x37;
    public static final int RESOURCE_PACK_SEND    = 0x38;
    public static final int RESPAWN               = 0x39;
    public static final int ENTITY_HEAD_LOOK      = 0x3A;
    public static final int MULTI_BLOCK_CHANGE    = 0x3B;
    public static final int SEL_ADVANCEMENT_TAB   = 0x3C;
    public static final int WORLD_BORDER          = 0x3D;
    public static final int CAMERA                = 0x3E;
    public static final int HELD_ITEM_CHANGE_CB   = 0x3F;
    public static final int UPDATE_VIEW_POSITION  = 0x40;
    public static final int UPDATE_VIEW_DISTANCE  = 0x41;
    public static final int SPAWN_POSITION        = 0x42;
    public static final int DISPLAY_SCOREBOARD    = 0x43;
    public static final int ENTITY_METADATA       = 0x44;
    public static final int ATTACH_ENTITY         = 0x45;
    public static final int ENTITY_VELOCITY       = 0x46;
    public static final int ENTITY_EQUIPMENT      = 0x47;
    public static final int SET_EXPERIENCE        = 0x48;
    public static final int UPDATE_HEALTH         = 0x49;
    public static final int SCOREBOARD_OBJECTIVE  = 0x4A;
    public static final int SET_PASSENGERS        = 0x4B;
    public static final int TEAMS                 = 0x4C;
    public static final int UPDATE_SCORE          = 0x4D;
    public static final int TIME_UPDATE           = 0x4E;
    public static final int TITLE                 = 0x4F;
    public static final int ENTITY_SOUND_EFFECT   = 0x50;
    public static final int SOUND_EFFECT          = 0x51;
    public static final int STOP_SOUND            = 0x52;
    public static final int PLAYER_LIST_HEAD_FOOT = 0x53;
    public static final int NBT_QUERY_RESPONSE    = 0x54;
    public static final int COLLECT_ITEM          = 0x55;
    public static final int ENTITY_TELEPORT       = 0x56;
    public static final int ADVANCEMENTS          = 0x57;
    public static final int ENTITY_PROPERTIES     = 0x58;
    public static final int ENTITY_EFFECT         = 0x59;
    public static final int DECLARE_RECIPES       = 0x5A;
    public static final int TAGS                  = 0x5B;
}
