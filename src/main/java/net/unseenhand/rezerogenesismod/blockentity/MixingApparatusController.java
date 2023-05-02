package net.unseenhand.rezerogenesismod.blockentity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public void createStructure() {
        if (formed) {
            // Something
        }
    }

    public int getBlocks() {
        return blockCount;
    }

    public boolean isMultiblockFormed() {
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