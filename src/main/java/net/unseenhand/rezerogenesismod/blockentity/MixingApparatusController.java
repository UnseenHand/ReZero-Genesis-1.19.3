package net.unseenhand.rezerogenesismod.blockentity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.unseenhand.rezerogenesismod.common.managers.MultiblockManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class MixingApparatusController extends MultiblockBlockEntity {
    public static final Map<UUID, MixingApparatusController> MAP = new HashMap<>();
    private static final Logger LOGGER = LogUtils.getLogger();
    private int blockCount = 0;
    private boolean formed;

    public MixingApparatusController(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.MIXING_APPARATUS_CONTROLLER.get(), blockPos, blockState);
    }

    public void addBlock() {
        blockCount++;
    }

    // Should set all the custom block state properties for each block that is related to the multiblock structure
    // That is needed to get the Minecraft to know about change of the block state to render it and give a
    // corresponding result, in my case change the multiblock blocks textures
    public void createStructure() {
        UUID Id = getID();

        // Get the block entity that is in the chunk you're interested in
        BlockEntity controllerEntity = MixingApparatusController.MAP.get(Id);
        Level level = controllerEntity.getLevel();

        if (formed) {
            // Find all block entities with the same ID
            List<BlockEntity> blockEntities = MultiblockManager.findAll(level, controllerEntity);
            for (BlockEntity blockEntity : blockEntities) {
                MultiblockManager.assignBlockState(blockEntity, level, Id);
            }
        }

        controllerEntity.setChanged();
    }

    public int getBlocks() {
        return blockCount;
    }

    public boolean isMultiblockFull() {
        return getBlocks() == 8; // Upgrade
    }

    public void markAsFormed() {
        this.formed = true;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.load(nbt);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // Will get tag from #getUpdateTag
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        String multiblockTagName = "multiblock";

        for (String key : tag.getAllKeys()) {
            if (key.equals(multiblockTagName)) {
                ListTag multiblockList = tag.getList(multiblockTagName, 10);
                for (int i = 0; i < multiblockList.size(); i++) {
                    CompoundTag multiblockTag = multiblockList.getCompound(i);
                    MAP.put(multiblockTag.getUUID("id"), (MixingApparatusController) multiblockTag.get("data"));
                }
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        //Write your data into the tag
        for (Map.Entry<UUID, MixingApparatusController> entry : MAP.entrySet()) {
            CompoundTag multiblockTag = new CompoundTag(); // create a new CompoundTag object
            multiblockTag.putUUID("id", entry.getKey()); // create an "id" tag with the UUID value
            multiblockTag.put("data", entry.getValue().getPersistentData()); // Should convert entity to tag
            tag.put("multiblock", multiblockTag);
        }

        super.saveAdditional(tag);
    }

    @Override
    public void tick() {

    }
}