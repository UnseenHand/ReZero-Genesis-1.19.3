package net.unseenhand.rezerogenesismod.saveddata;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class MultiblocksData extends SavedData {
    public static final String LIST_KEY = "list";
    public static final String MAP_KEY = "map";
    private static final String BLOCK_POS_LIST_KEY = "Positions";
    private static final String DATA_KEY = "multiblock";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String UUID_KEY = "UUID";
    /**
     * Stores the connected positions
     */
    public static List<BlockPos[]> LIST;
    /**
     * Stores the mapped positions (when there is an CONTROLLER INSTANCE attached)
     */
    public static Map<UUID, BlockPos[]> MAP;

    protected MultiblocksData(Map<UUID, BlockPos[]> map, List<BlockPos[]> list) {
        MAP = map;
        LIST = list;
    }

    // OW - OverWorld
    private static MultiblocksData attachToOWLevel(Level pLevel) {
        MinecraftServer server = pLevel.getServer();
        if (server != null) {
            return MultiblocksData.getOrCreate(server.overworld());
        } else {
            // TODO:
            // Write the proper error message
            LOGGER.error("Cannot get the server from the level instance!");
            throw new RuntimeException("The ServerLevel class returned null as a server instance.");
        }
    }

    public static MultiblocksData create() {
        return new MultiblocksData(new HashMap<>(), new ArrayList<>());
    }

    public static MultiblocksData getOrCreate(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(MultiblocksData::load, MultiblocksData::create, DATA_KEY);
    }

    // TODO:
    // Should read the NBT data and assign it to where needed
    public static MultiblocksData load(CompoundTag nbt) {
        MultiblocksData data = create();

        // Load saved data
        CompoundTag multiblockData = nbt.getCompound(DATA_KEY);

        // Retrieving the LIST of `BLOCKS` only
        ListTag listTag = multiblockData.getList(LIST_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            ListTag positionsTag = listTag.getList(i);

            // BlockPos[]
            BlockPos[] positions = new BlockPos[positionsTag.size()];
            for (int j = 0; j < positionsTag.size(); j++) {
                CompoundTag positionTag = positionsTag.getCompound(j);
                positions[i] = NbtUtils.readBlockPos(positionTag);
            }

            LIST.add(positions);
        }

        // Retrieving the MAP of `UUID -> BLOCKS + CONTROLLER`
        ListTag mapTag = multiblockData.getList(MAP_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < multiblockData.size(); i++) {
            CompoundTag mapEntry = mapTag.getCompound(i);

            // UUID
            UUID uuid = mapEntry.getUUID(UUID_KEY);

            // BlockPos[]
            ListTag positionsTag = mapEntry.getList(BLOCK_POS_LIST_KEY, Tag.TAG_COMPOUND);
            BlockPos[] positions = new BlockPos[positionsTag.size()];
            for (int j = 0; j < positionsTag.size(); j++) {
                CompoundTag positionTag = positionsTag.getCompound(i);
                positions[i] = NbtUtils.readBlockPos(positionTag);
            }

            MAP.put(uuid, positions);
        }

        return data;
    }

    public static MultiblocksData retrieveData(Level pLevel) {
        return attachToOWLevel(pLevel);
    }

    public static void updateMD(Level pLevel) {
        // Set as changed
        MultiblocksData data = MultiblocksData.retrieveData(pLevel);
        data.setDirty();
    }

    public void removeMapDataSaved(UUID uuid) {
        MAP.remove(uuid);

        this.setDirty();
    }

    // TODO: 21.05.2023 dada
    // Added list recently
    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        CompoundTag dataTag = new CompoundTag();

        ListTag listTag = new ListTag();
        for (BlockPos[] positions : LIST) {
            ListTag positionsTag = new ListTag();

            for (BlockPos pos : positions) {
                positionsTag.add(NbtUtils.writeBlockPos(pos));
            }

            listTag.add(positionsTag);
        }

        ListTag mapTag = new ListTag();
        for (Map.Entry<UUID, BlockPos[]> entry : MAP.entrySet()) {
            CompoundTag entryTag = new CompoundTag();

            UUID uuid = entry.getKey();
            BlockPos[] blockPositions = entry.getValue();

            ListTag blockPositionsTag = new ListTag();
            for (BlockPos pos : blockPositions) {
                blockPositionsTag.add(NbtUtils.writeBlockPos(pos));
            }

            entryTag.putUUID(UUID_KEY, uuid);
            entryTag.put(BLOCK_POS_LIST_KEY, blockPositionsTag);

            mapTag.add(entryTag);
        }

        dataTag.put(LIST_KEY, listTag);
        dataTag.put(MAP_KEY, mapTag);

        pCompoundTag.put(DATA_KEY, dataTag);
        return pCompoundTag;
    }
}