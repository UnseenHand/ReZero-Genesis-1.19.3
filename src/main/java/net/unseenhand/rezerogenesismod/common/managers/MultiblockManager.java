package net.unseenhand.rezerogenesismod.common.managers;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.level.BlockEvent;
import net.unseenhand.rezerogenesismod.blockentity.*;
import net.unseenhand.rezerogenesismod.block.ModBlocks;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.unseenhand.rezerogenesismod.block.MultiblockBlock.MULTIBLOCK_PART_TYPE;

public class MultiblockManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void assignBlockState(BlockEntity blockEntity, Level level, UUID Id) {
        if (blockEntity instanceof MixingApparatus mixingApparatus) {
            if (mixingApparatus.getID() == Id) {
                // Change the state of the block (that fills up the structure)
                BlockPos pos = mixingApparatus.getBlockPos();
                BlockState state = mixingApparatus.getBlockState();
                state = state.setValue(MULTIBLOCK_PART_TYPE, EnumMultiblockPartType.BLOCK);
                level.setBlockAndUpdate(pos, state); // Maybe

                LOGGER.info(mixingApparatus.getBlockState().getValue(MULTIBLOCK_PART_TYPE).toString());
            }
        } else if (blockEntity instanceof MixingApparatusController mixingApparatusController) {
            if (mixingApparatusController.getID() == Id) {
                // Change the state of the controller
                BlockPos controllerPos = mixingApparatusController.getBlockPos();
                BlockState controllerState = mixingApparatusController.getBlockState();
                controllerState = controllerState.setValue(MULTIBLOCK_PART_TYPE, EnumMultiblockPartType.CONTROLLER);
                level.setBlockAndUpdate(controllerPos, controllerState); // Maybe

                LOGGER.info(mixingApparatusController.getBlockState().getValue(MULTIBLOCK_PART_TYPE).toString());
            }
        }
    }

    private static void closeNeighborSides(Level level, MultiblockBlockEntity target) {
        ArrayList<Direction> connectedDirections = new ArrayList<>();
        ArrayList<Direction> closedDirections = new ArrayList<>();

        getNotDefaultDirections(connectedDirections, closedDirections, target);

        for (Direction connectedDirection : connectedDirections) {
            // Gets the block entity of the relatively connected multiblock block
            BlockPos connectedPos = target.getBlockPos().relative(connectedDirection);
            BlockEntity connectedEntity = level.getBlockEntity(connectedPos);

            // Gets the connected direction of the 'connectedEntity' connectedDirection.getOpposite()
            // Should also create arrays for
            ArrayList<Direction> neighbourConnectedDirections = new ArrayList<>();
            ArrayList<Direction> neighbourClosedDirections = new ArrayList<>();

            if (connectedEntity instanceof MultiblockBlockEntity multiblockTileEntity) {
                // Fills the array with the appropriate directions
                getNotDefaultDirections(neighbourConnectedDirections,
                        neighbourClosedDirections,
                        multiblockTileEntity);

                for (Direction neighbourConnectedDirection : neighbourConnectedDirections) {

                    ArrayList<Direction> directionsCopy = new ArrayList<>(neighbourClosedDirections);
                    directionsCopy.remove(neighbourConnectedDirection.getOpposite());

                    // Represents every single neighbour entities by the directions that are connected
                    // Used to close the directions that are not possible to extend the multiblock through
                    BlockPos relativePos = connectedEntity.getBlockPos().relative(neighbourConnectedDirection);
                    BlockEntity relativeBlockEntity = level.getBlockEntity(relativePos);

                    if (relativeBlockEntity instanceof MultiblockBlockEntity relative) {
                        // HERE IS THE ISSUE!!!
                        for (Direction direction : directionsCopy) {
                            relative.setCustomProperty(direction, DirectionState.CLOSED);
                        }
                    }

                    // Set changes
                    multiblockTileEntity.setChanged();
                }
            }
        }
    }

    // Returns the ID that was set to the target block entity
    public static UUID constraintTargetAndNeighbours(BlockEvent.EntityPlaceEvent event,
                                                     Level level,
                                                     Block block,
                                                     BlockPos pos) {
        // Counter to decide whether to create new multiblock structure or not
        int neighbourCounter = 0;
        UUID Id = null;

        // Set sides' connections for the neighbours and the target block itself
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pos.relative(direction);
            Block neighbourBlock = level.getBlockState(neighbourPos).getBlock();

            // Neighbour is the same block type as the target or is 'master'
            if (neighbourBlock == block ||
                    neighbourBlock == ModBlocks.MIXING_APPARATUS_CONTROLLER.get()) {
                // Increase counter of neighbours
                neighbourCounter++;

                // Getting block entities
                MultiblockBlockEntity targetBlockEntity = (MultiblockBlockEntity) level.getBlockEntity(pos);
                MultiblockBlockEntity neighbourBlockEntity = (MultiblockBlockEntity) level.getBlockEntity(neighbourPos);


                // Getting target and neighbour
                if (neighbourBlockEntity != null && targetBlockEntity != null) {
                    if (neighbourCounter == 1) {
                        // ID assigning was here
                        // Getting the multiblock entity by the ID
                        Id = neighbourBlockEntity.getID();
                        if (Id == null && event.isCancelable()) { // Maybe should delete it
                            event.setCanceled(true);
                        }
                        targetBlockEntity.setID(Id);
                    }

                    setProperties(level, direction, targetBlockEntity, neighbourBlockEntity, neighbourCounter);
                }
            }
        }

        // New instance of the multiblock creation (ID)
        if (neighbourCounter == 0) {
            // Get the multiblock 'master' or 'fill' block entity
            // master - multiblock controller
            // fill - multiblock block that fills the structure
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof MixingApparatus fill) {
                // Set the ID for the filling block
                Id = UUID.randomUUID();
                fill.setID(Id);

                // Set changes
                fill.setChanged();
            } else if (blockEntity instanceof MixingApparatusController master) {
                // Set the ID for the master block
                Id = UUID.randomUUID();
                master.setID(Id);

                // Map (Add) to all entities in the list
                // MappedList should be available through the whole lifecycle of the program
                MixingApparatusController.MAP.put(Id, master);

                // Set changes
                master.setChanged();
            }
        }

        return Id;
    }

    public static List<BlockEntity> findAll(Level level, BlockEntity controllerEntity) {
        List<BlockEntity> blockEntities = new ArrayList<>();

        if (level != null) {
            // Get the chunk that contains that block entity
            ChunkAccess chunk = level.getChunk(controllerEntity.getBlockPos());

            // Iterate over the block entities in that chunk and find the ones with the desired ID
            for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                blockEntities.add(blockEntity);
            }
        }

        return blockEntities;
    }

    // Gets closed and connected directions form the block entity and adds to arrays
    private static void getNotDefaultDirections(ArrayList<Direction> connected,
                                                ArrayList<Direction> closed,
                                                MultiblockBlockEntity blockEntity) {
        for (Direction direction : Direction.values()) {
            DirectionState property = blockEntity.getCustomProperty(direction);
            if (property == DirectionState.CONNECTED) {
                connected.add(direction);
            } else if (property == DirectionState.CLOSED) {
                closed.add(direction);
            }
        }
    }

    private static void setProperties(Level level,
                                      Direction direction,
                                      MultiblockBlockEntity target,
                                      MultiblockBlockEntity neighbour,
                                      int neighbourCounter) {
        // Get opposite direction from the connected side
        Direction oppositeDirection = direction.getOpposite();

        // Not set properties (conn, clos) if you cannot access the structure
        DirectionState neighbourProperty = neighbour.getCustomProperty(oppositeDirection);
        if (neighbourProperty != DirectionState.OPENED) {
            return;
        }

        // Connect
        target.setCustomProperty(direction, DirectionState.CONNECTED);
        neighbour.setCustomProperty(oppositeDirection, DirectionState.CONNECTED);

        // Close
        target.setCustomProperty(oppositeDirection, DirectionState.CLOSED);
        neighbour.setCustomProperty(direction, DirectionState.CLOSED);

        if (neighbour.hasConnections(1)) {
            closeNeighborSides(level, target);
        }

        // Setting changes
        target.setChanged();
        neighbour.setChanged();
    }
}