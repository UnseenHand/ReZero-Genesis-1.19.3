package net.unseenhand.rezerogenesismod.common.manager;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.BlockEvent;
import net.unseenhand.rezerogenesismod.block.ModBlocks;
import net.unseenhand.rezerogenesismod.block.entity.DirectionState;
import net.unseenhand.rezerogenesismod.block.entity.MixingApparatus;
import net.unseenhand.rezerogenesismod.block.entity.MixingApparatusController;
import net.unseenhand.rezerogenesismod.block.entity.MultiblockBlockEntity;
import net.unseenhand.rezerogenesismod.saveddata.MultiblocksData;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.UUID;

public class MultiblockManager {
    private static final Logger LOGGER = LogUtils.getLogger();

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

    public static UUID constraintTargetAndNeighbours(BlockEvent.EntityPlaceEvent pEvent,
                                                     Level pLevel,
                                                     BlockPos pPos) {
        // Counter to decide whether to create new multiblock structure or not
        int neighbourCounter = 0;
        UUID Id = null;
        Block targetBlock = pLevel.getBlockState(pPos).getBlock();

        // The ELSE is wrong
        // Set sides' connections for the neighbours and the target block itself
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pPos.relative(direction);
            Block neighbourBlock = pLevel.getBlockState(neighbourPos).getBlock();

            // Neighbour is the same block type as the target or is 'master'
            if (neighbourBlock == ModBlocks.MIXING_APPARATUS.get()) {

                // If there is at least 1 controller
                BlockPos[] blockPositions = MultiblocksData.MAP.get(Id);

                if (blockPositions != null) {
                    // Check every single block position for the MController block
                    for (BlockPos blockPos : blockPositions) {
                        BlockEntity blockEntity = pLevel.getBlockEntity(blockPos);

                        if (blockEntity instanceof MixingApparatusController mController) {
                            // Pass
                            // Do other connections
                            UUID controllerId = mController.getID();

                            BlockEntity target = pLevel.getBlockEntity(pPos);

                            if (target instanceof MixingApparatus mBlock) {
                                // Set the UUID for the MBlock that is now the part of the controller
                                // Thus it is the part of the whole multiblock structure
                                mBlock.setID(controllerId);

                                // Saving the data
                                MultiblocksData data = MultiblocksData.retrieveData(pLevel);
                                // TODO: NOT RIGHT
                                MultiblocksData.MAP.put(controllerId, new BlockPos[]{});
                                data.setDirty();
                            }
                        }
                    }
                }

                // Set properties anyway
                // Getting block entities
                MultiblockBlockEntity targetBlockEntity = (MultiblockBlockEntity) pLevel.getBlockEntity(pPos);
                MultiblockBlockEntity neighbourBlockEntity = (MultiblockBlockEntity) pLevel.getBlockEntity(neighbourPos);

                setProperties(pLevel, direction, targetBlockEntity, neighbourBlockEntity, neighbourCounter);

                // Increase counter of neighbours
                neighbourCounter++;
            } else if (targetBlock == neighbourBlock &&
                    neighbourBlock == ModBlocks.MIXING_APPARATUS_CONTROLLER.get()) {
                Id = createNewMultiblockInstance(pLevel, pPos);

                // Increase counter of neighbours
                neighbourCounter++;
            } else if (neighbourBlock == ModBlocks.MIXING_APPARATUS_CONTROLLER.get()) {
                // Getting block entities
                MultiblockBlockEntity targetBlockEntity = (MultiblockBlockEntity) pLevel.getBlockEntity(pPos);
                MultiblockBlockEntity neighbourBlockEntity = (MultiblockBlockEntity) pLevel.getBlockEntity(neighbourPos);


                // Getting target and neighbour
                if (neighbourBlockEntity != null && targetBlockEntity != null) {
                    if (neighbourCounter == 1) {
                        // ID assigning was here
                        // Getting the multiblock entity by the ID
                        Id = neighbourBlockEntity.getID();
                        if (Id == null && pEvent.isCancelable()) { // Maybe should delete it
                            pEvent.setCanceled(true);
                        }
                        targetBlockEntity.setID(Id);
                    }

                    setProperties(pLevel, direction, targetBlockEntity, neighbourBlockEntity, neighbourCounter);
                }

                // Increase counter of neighbours
                neighbourCounter++;
            }
        }

        if (neighbourCounter == 0) {
            if (targetBlock == ModBlocks.MIXING_APPARATUS.get()) {
                // Do something
                // That means that the block has no neighbours and also is the Mixing Apparatus Block
            } else if (targetBlock == ModBlocks.MIXING_APPARATUS_CONTROLLER.get()) {
                // Create new instance of the Mixing Apparatus Multiblock through the Controller
                Id = createNewMultiblockInstance(pLevel, pPos);
            }
        }

        return Id;
    }
    // FIXME: NEED A DETAILED REVIEW

    private static UUID createNewMultiblockInstance(Level pLevel, BlockPos pPos) {
        // Get the multiblock 'master' or 'mixingApparatus' block entity
        // master - multiblock controller
        // mixingApparatus - multiblock block that fills the structure
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        UUID uuid = UUID.randomUUID();

        if (blockEntity instanceof MixingApparatusController mixingApparatusController) {
            // Set the ID for the mixingApparatusController block
            mixingApparatusController.setID(uuid);

            // Get the MAP data
            MultiblocksData multiblocksSD = MultiblocksData.retrieveData(pLevel);
            // Map (Add) to all entities in the list
            // MappedList should be available through the whole lifecycle of the program
            BlockPos controllerPos = mixingApparatusController.getBlockPos();
            MultiblocksData.MAP.put(uuid, new BlockPos[]{controllerPos});

            // Set changes
            mixingApparatusController.setChanged();
            multiblocksSD.setDirty();
        }

        return uuid;
    }
    // Gets closed and connected directions form the block entity and adds to arrays

    private static void getNotDefaultDirections(ArrayList<Direction> connected,
                                                ArrayList<Direction> closed,
                                                MultiblockBlockEntity blockEntity) {
        for (Direction direction : Direction.values()) {
            DirectionState property = blockEntity.getDSProperty(direction);
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
        DirectionState neighbourProperty = neighbour.getDSProperty(oppositeDirection);
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