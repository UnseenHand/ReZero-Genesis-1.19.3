package net.unseenhand.rezerogenesismod.common.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.unseenhand.rezerogenesismod.block.ModBlocks;
import net.unseenhand.rezerogenesismod.block.entity.DirectionState;
import net.unseenhand.rezerogenesismod.block.entity.MultiblockBlockEntity;
import net.unseenhand.rezerogenesismod.saveddata.MultiblocksData;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

/**
 * @// TODO: 20.05.2023
 */
public class MultiblockDirectionStateHandler {
    /**
     * The list of the currently connected blocks
     */
    private static final List<BlockPos> accumulationList = new ArrayList<>();
    /**
     * The list of the `target` block connection {@link Direction}s
     */
    private static final List<Direction> connections = new ArrayList<>();
    /**
     * The list of those {@link #accumulationList} instances
     * which elements should be removed from/added
     * to the {@link MultiblocksData#LIST}
     */
    private static final List<BlockPos[]> positionsArrayList = new ArrayList<>();
    /**
     * Index of the {@link MultiblocksData#LIST} list element
     * that contains `target` block
     */
    private static int index;
    /**
     * The array retrieved by the {@link #index}
     * in the {@link MultiblocksData#LIST}
     */
    private static BlockPos[] targetArray;

    /**
     * This method adds the {@link #accumulationList} list
     * at the end of the {@link MultiblocksData#LIST} arrays list,
     * which represents the `target` block <i>Multiblock Structure</i>
     */
    private static void addToList() {
        // Add the whole structure part to the end of the LIST
        BlockPos[] positions = accumulationList.toArray(BlockPos[]::new);
        MultiblocksData.LIST.add(positions);

        accumulationList.clear();
    }

    /**
     * This method accumulates the connected block chunks to the `target`
     * and cleans up the {@link MultiblocksData#LIST} list
     * from the redundant {@link BlockPos} data
     * @param pPos the `target` block position
     */
    private static void addConnectedChunks(BlockPos pPos) {
        for (Direction direction : connections) {
            BlockPos relative = pPos.relative(direction);

            // Removes every single INSTANCE (LIST element) that contains the redundant neighbour
            for (int i = 0; i < MultiblocksData.LIST.size(); i++) {
                boolean shouldRemove = false;
                BlockPos[] positions = MultiblocksData.LIST.get(i);

                for (BlockPos pos : positions) {
                    if (pos == relative) {
                        shouldRemove = true;

                        break;
                    }
                }

                if (shouldRemove) {
                    MultiblocksData.LIST.remove(positions);

                    List<BlockPos> posList = List.of(positions);
                    accumulationList.addAll(posList);
                }
            }
        }

        // Add the target itself
        accumulationList.add(pPos);
    }

    /**
     * This method checks if the mapped stream of int results has the same
     * values
     *
     * @param list           the provided list of the {@link BlockPos} to go through
     * @param fieldExtractor the function that returns {@code int} value
     * @return {@code true} if the {@code int} axis values are all the same
     */
    private static boolean planeAll(List<BlockPos> list, ToIntFunction<BlockPos> fieldExtractor) {
        IntStream stream = list.stream().mapToInt(fieldExtractor).distinct();

        return sameAxis(stream);
    }

    /**
     * @param stream the steam of integer values
     * @return {@code true} if the number of the stream elements is equal to 1
     */
    private static boolean sameAxis(IntStream stream) {
        return stream.count() == 1;
    }

    /**
     * TODO: Doesn't work
     * @param pLevel
     * @param pPos
     */
    private static void closeAdditional(Level pLevel, BlockPos pPos) {
        setIndex(pPos);

        // TODO: FIX THE CONSISTENCY
        // Opposites, but it is not evaluated based on the new 'connections' list (created using new keyword)
        // Those are the 'Directions' that depend on the connections of the current block and 'connections' list
        // Also, it should probably reassign the connections every time it goes to the 'different block'
        // By 'different block' I mean NOT the TARGET one
        List<Direction> oppositeConnections = getOpposite();

        // Go through the CONNECTIONS that were based on the TARGET connection DIRECTIONS with the neighbours
        // So, that they are opposite to those that are currently on the TARGET
        BlockPos[] positions = MultiblocksData.LIST.get(index);

        if (positions.length == 4 && samePlane(positions)) {
            for (BlockPos pos : positions) {
                // Neighbour BE
                BlockEntity blockEntity = pLevel.getBlockEntity(pos);

                for (Direction oppositeDirection : oppositeConnections) {
                    // Is MBE
                    if (blockEntity instanceof MultiblockBlockEntity multiblockBlock) {
                        // TODO: HERE
                        // If the NEIGHBOUR DCP is not 'CONNECTED' -> It should be 'CLOSED'
                        if (multiblockBlock.getDSProperty(oppositeDirection) != DirectionState.CONNECTED) {
                            multiblockBlock.setCustomProperty(oppositeDirection, DirectionState.CLOSED);

                            multiblockBlock.setChanged();
                        }
                    }
                }
            }
        } else {
            for (BlockPos pos : positions) {
                // Neighbour BE
                BlockEntity blockEntity = pLevel.getBlockEntity(pos);

                for (Direction oppositeDirection : oppositeConnections) {
                    // Is MBE
                    if (blockEntity instanceof MultiblockBlockEntity multiblockBlock) {
                        // TODO: HERE
                        // If the NEIGHBOUR DCP is not 'CONNECTED' -> It should be 'CLOSED'
                        if (multiblockBlock.getDSProperty(oppositeDirection) != DirectionState.CONNECTED) {
                            multiblockBlock.setCustomProperty(oppositeDirection, DirectionState.CLOSED);

                            multiblockBlock.setChanged();
                        }
                    }
                }
            }
        }
    }

    /**
     * This method closes the 'outer' side
     * for both the `target` and the `neighbour` blocks.
     * By 'outer' the description implies that those
     * sides are <b>NOT CONNECTED</b>, and are <b>OPPOSITE</b>,
     * thus they should be closed when the block is placed
     *
     * @param pDirection the direction from the `target` block
     *                   towards the `neighbour` block
     * @param target     the `target` block
     * @param neighbour  the `neighbour` block
     */
    private static void closeOn(Direction pDirection, MultiblockBlockEntity target, MultiblockBlockEntity neighbour) {
        target.setCustomProperty(pDirection.getOpposite(), DirectionState.CLOSED);
        neighbour.setCustomProperty(pDirection, DirectionState.CLOSED);
    }

    /**
     * This method checks each blocks surrounding `target` block for its
     * identity, and if it is the right one,then the specifically provided
     * operation is performed on both the `target` and the `neighbour`
     *
     * @param pLevel         the world level on which the block was placed/destroyed
     * @param pPos           the `target` block position in the world
     * @param commonConsumer the method reference that should be used
     *                       in this particular case,
     *                       it might be the <i>common</i> <b>CC/OO<b/>
     */
    private static void common(Level pLevel,
                               BlockPos pPos,
                               TriConsumer<Direction, MultiblockBlockEntity, MultiblockBlockEntity> commonConsumer) {
        BlockEntity targetBE = pLevel.getBlockEntity(pPos);

        for (Direction direction : Direction.values()) {
            BlockPos relativeBP = pPos.relative(direction);
            Block relative = pLevel.getBlockState(relativeBP).getBlock();

            if (isMAPart(relative)) {
                BlockEntity neighbourBE = pLevel.getBlockEntity(relativeBP);

                if (targetBE instanceof MultiblockBlockEntity target &&
                        neighbourBE instanceof MultiblockBlockEntity neighbour) {
                    commonConsumer.accept(direction, target, neighbour);
                }
            }
        }
    }

    /**
     * This method sets the common (basic) connection between 2 siding blocks.
     * This CC -> ConnectClose means that this method
     * connects and closes sides of the `target` and the `neighbour` blocks
     * and adds those connected {@link Direction}s to the {@link #connections}
     * list to
     *
     * @param pDirection the direction from the `target` block
     *                   towards the `neighbour` block
     * @param target     the `target` block
     * @param neighbour  the `neighbour` block
     */
    private static void commonCC(Direction pDirection, MultiblockBlockEntity target, MultiblockBlockEntity neighbour) {
        if (target.getDSProperty(pDirection) == DirectionState.OPENED &&
                neighbour.getDSProperty(pDirection) == DirectionState.OPENED) {
            connectOn(pDirection, target, neighbour);
            closeOn(pDirection, target, neighbour);

            connections.add(pDirection);
        }
    }

    /**
     * This method removes the common (basic) connection between 2 siding blocks.
     * This OO -> OpenOpen means that this method
     * opens sides of the `target` and the `neighbour` blocks
     * and adds those connected {@link Direction}s to the {@link #connections}
     * list
     *
     * @param pDirection the direction from the `target` block
     *                   towards the `neighbour` block
     * @param target     the `target` block
     * @param neighbour  the `neighbour` block
     */
    private static void commonOO(Direction pDirection, MultiblockBlockEntity target, MultiblockBlockEntity neighbour) {
        openOn(pDirection, target, neighbour);

        connections.add(pDirection);
    }

    /**
     * This method <b>CONNECTS</b> the siding blocks `target` and `neighbour`
     *
     * @param pDirection the direction from the `target` block
     *                   towards the `neighbour` block
     * @param target     the `target` block
     * @param neighbour  the `neighbour` block
     */
    private static void connectOn(Direction pDirection, MultiblockBlockEntity target, MultiblockBlockEntity neighbour) {
        target.setCustomProperty(pDirection, DirectionState.CONNECTED);
        neighbour.setCustomProperty(pDirection.getOpposite(), DirectionState.CONNECTED);
    }

    /**
     * This is the 'extra' method, meaning that this method executes when it is
     * needed (the additional/extra block's properties should be set/removed)
     *
     * @param pLevel        the world level on which the `target` block
     *                      and the other parts of the multiblock should be settled
     * @param pPos          the `target` block position in the world
     * @param extraConsumer the method reference that controls the 'extra'
     *                      operation for the MB instance
     */
    private static void extra(Level pLevel, BlockPos pPos, BiConsumer<Level, BlockPos> extraConsumer) {
        if (connections.size() != 0) {
            extraConsumer.accept(pLevel, pPos);
        }
    }

    /**
     * This method finds the `target` block in the {@link MultiblocksData#LIST}
     * list and sets the {@link #index} of the element (array) that contains it
     *
     * @param pPos the block position of the `target`
     */
    private static void setIndex(BlockPos pPos) {
        boolean positionFound = false;
        int targetIndex = 0;

        for (int i = 0; i < MultiblocksData.LIST.size(); i++) {
            BlockPos[] positions = MultiblocksData.LIST.get(i);

            if (!positionFound) {
                // Assign the necessary index
                targetIndex = i;

                // Position has been found
                for (BlockPos pos : positions) {
                    if (pos == pPos) {
                        positionFound = true;

                        break;
                    }
                }
            }
        }

        index = targetIndex;
    }

    /**
     * This method retrieves the list of {@link #connections}
     * that contains fully opposite {@link Direction}s
     *
     * @return the opposite list of the {@link #connections} list
     */
    private static List<Direction> getOpposite() {
        List<Direction> oppositeDirections = new ArrayList<>();

        for (Direction direction : connections) {
            Direction oppositeDirection = direction.getOpposite();
            oppositeDirections.add(oppositeDirection);
        }

        return oppositeDirections;
    }

    /**
     * This method uses the recursion - which means that this method calls
     * itself to simplify the multiblock structure searching for the connected
     * blocks to the first one
     *
     * @param pLevel     the current world level at which block is located
     * @param pPos       the position of the current block
     * @param directions available directions to go through
     *                   for this specific block
     */
    private static void goDeep(Level pLevel, BlockPos pPos, Direction[] directions) {
        for (Direction direction : directions) {
            BlockPos currentRelative = pPos.relative(direction);
            BlockEntity blockEntity = pLevel.getBlockEntity(currentRelative);

            // Opposite Direction -> The direction that has the connection on the other side of the relative block
            // And is opposite to so
            Direction opposite = direction.getOpposite();

            if (blockEntity instanceof MultiblockBlockEntity multiblockBlock &&
                    multiblockBlock.getDSProperty(opposite) == DirectionState.CONNECTED &&
                    !accumulationList.contains(currentRelative)) {
                accumulationList.add(pPos);

                // Without the opposite side, because it was already evaluated
                Direction[] newDirections =
                        Arrays.stream(directions).filter(d -> d != opposite).toArray(Direction[]::new);

                goDeep(pLevel, currentRelative, newDirections);
            }
        }
    }

    /**
     * This is the 'semi-conditional' method that not only performs different
     * set of actions based on the {@link Action} parameter, but also executes
     * some general stuff
     *
     * @param pLevel  current world level at which operation should be handled
     * @param pPos    block position of the `target` block
     * @param pAction type of the action to perform on the `target` block
     */
    public static void handle(Level pLevel, BlockPos pPos, Action pAction) {
        if (pAction == Action.SET) {
            setAll(pLevel, pPos);
        } else if (pAction == Action.REMOVE) {
            removeAll(pLevel, pPos);
        }

        connections.clear();

        MultiblocksData.updateMD(pLevel);
    }

    /**
     * This method simply checks if the block is the appropriate one
     * (Multiblock Apparatus part)
     *
     * @param block the block to compare
     * @return {@code true} if the block is MA
     * (Mixing Apparatus) -> multiblock part
     */
    public static boolean isMAPart(Block block) {
        return block == ModBlocks.MIXING_APPARATUS.get() || block == ModBlocks.MIXING_APPARATUS_CONTROLLER.get();
    }

    /**
     * This method is a part of the removal operation over the multiblock
     * structure block DC (Direction Connections) as well as their separation
     *
     * @param pLevel the world level at which the block/blocks are
     *               closed/connected to open them
     * @param pPos   position of the `target` block in the world
     */
    private static void openAdditional(Level pLevel, BlockPos pPos) {
        setIndex(pPos);
        separateIntoChunks(pLevel, pPos);
        removeTargetArray();
        setAllDSP(pLevel, pPos);
    }

    /**
     * General method to open all sides in all directions of the `target` block
     *
     * @param pLevel the world level at which the block should be <i>opened</i>
     * @param pPos   the `target` block position that remained
     *               without the appropriate neighbours
     */
    private static void openAll(Level pLevel, BlockPos pPos) {
        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

            if (blockEntity instanceof MultiblockBlockEntity multiblockBlock) {
                multiblockBlock.setCustomProperty(direction, DirectionState.OPENED);
            }
        }
    }

    /**
     * This method basically opens the closed/connected sides
     * for the neighbouring blocks
     * @param pDirection the direction from the `target` block
     *                   towards the `neighbour` block
     * @param target the `target` block
     * @param neighbour the `neighbour` block
     */
    private static void openOn(Direction pDirection, MultiblockBlockEntity target, MultiblockBlockEntity neighbour) {
        target.setCustomProperty(pDirection, DirectionState.OPENED);
        neighbour.setCustomProperty(pDirection.getOpposite(), DirectionState.OPENED);
    }

    /**
     * This method is the set of the common, extra and list of the manipulation
     * actions that contains the specific calls for its,
     * in this case `remove` way
     * @param pLevel the world level at which the `target` was destroyed
     * @param pPos the `target` block position
     */
    private static void removeAll(Level pLevel, BlockPos pPos) {
        common(pLevel, pPos, MultiblockDirectionStateHandler::commonOO);
        extra(pLevel, pPos, MultiblockDirectionStateHandler::openAdditional);

        removeFromList();
    }

    /**
     * This method removes the `target` array ({@link #targetArray})
     * and adds the separated chunks ({@link #positionsArrayList})
     * formed by the destruction of that `target`
     * to the end of the {@link MultiblocksData#LIST}
     */
    private static void removeFromList() {
        MultiblocksData.LIST.remove(targetArray);

        MultiblocksData.LIST.addAll(positionsArrayList);

        positionsArrayList.clear();
    }

    /**
     * This method simply removes the {@link #targetArray} array element
     * from the {@link MultiblocksData#LIST} list
     */
    private static void removeTargetArray() {
        MultiblocksData.LIST.remove(targetArray);
    }

    /**
     * Same plane means that <b>all</b> of the {@link BlockPos} in the provided
     * array have the same value on the X/Y/Z axis, meaning that there should
     * be at least a single ordinate that is equal for each
     * of the {@link BlockPos}
     *
     * @param positionsChunk Already separated part of a single multiblock by
     *                       the target removal event
     * @return {@code true} if all the blocks are located at the same plane
     */
    private static boolean samePlane(BlockPos[] positionsChunk) {
        List<BlockPos> asList = Arrays.asList(positionsChunk);

        return planeAll(asList, BlockPos::getX) ||
                planeAll(asList, BlockPos::getY) ||
                planeAll(asList, BlockPos::getZ);
    }

    /**
     * Suppose, we divide this array into several arrays that are
     * the product of the 'target' removal
     * TODO: I Will Try This For Now
     * @param pLevel
     * @param pPos
     */
    private static void separateIntoChunks(Level pLevel, BlockPos pPos) {
        // Old array from the LIST ot the `current` one that the handler oversees
        // Contains all connected blocks to the 'target'
        BlockPos[] positions = MultiblocksData.LIST.get(index);

        // This is the LIST without the `target`
        List<BlockPos> positionsList = Arrays.stream(positions).filter(p -> p != pPos).toList();

        // Should be efficient, by dynamically removing the redundant positions
        for (Iterator<BlockPos> iterator = positionsList.iterator(); iterator.hasNext(); ) {
            BlockPos pos = iterator.next();

            // Goes through the 'connected' blocks to the block on the `pos` position and accumulates all related parts
            goDeep(pLevel, pos, Direction.values());

            // Adds the single accumulationArray to the List
            BlockPos[] accumulationArray = accumulationList.toArray(BlockPos[]::new);
            positionsArrayList.add(accumulationArray);

            for (BlockPos p : accumulationArray) {
                if (positionsList.contains(p)) {
                    iterator.remove();
                }
            }

            // TODO: Probably should reduce the array by the elements inside of the accumulationList (as an array)
            // array = Arrays.stream(array).filter(p -> !accumulationList.contains(p)).toArray(BlockPos[]::new);

            // Recreate the List
            accumulationList.clear();
        }

        targetArray = positionsList.toArray(BlockPos[]::new);
    }

    /**
     * Sets all the needed properties and adds/updates the structure of the
     * multiblock to the {@link MultiblocksData#LIST} list
     * @param pLevel the world level at which the `target` block is placed
     * @param pPos   the position of the `target` block
     */
    private static void setAll(Level pLevel, BlockPos pPos) {
        common(pLevel, pPos, MultiblockDirectionStateHandler::commonCC);
        addConnectedChunks(pPos);

        extra(pLevel, pPos, MultiblockDirectionStateHandler::closeAdditional);

        addToList();
    }

    /**
     * This method sets all the DSP (Direction State Properties) for all of
     * the {@link #positionsArrayList} list contained chunk's {@link BlockPos}
     * @param pLevel the level at which the multiblock
     *               structure chunks are located
     * @param pPos the `target` block position
     */
    private static void setAllDSP(Level pLevel, BlockPos pPos) {
        for (BlockPos[] positionsChunk : positionsArrayList) {
            for (BlockPos pos : positionsChunk) {
                // Can be simplified if `target` define as 'static'
                List<BlockPos> chunkWithTargetList = new ArrayList<>(Arrays.stream(positionsChunk).toList());
                chunkWithTargetList.add(pPos);
                BlockPos[] chunkWithTargetArray = chunkWithTargetList.toArray(BlockPos[]::new);

                if (positionsChunk.length == 1) {
                    openAll(pLevel, pos);

                    // Adds to the end of the ORIGINAL LIST
                    MultiblocksData.LIST.add(positionsChunk);

                    MultiblocksData.updateMD(pLevel);
                } else if (positionsChunk.length == 2 ||
                        (positionsChunk.length == 3 && samePlane(chunkWithTargetArray)) ||
                        (positionsChunk.length == 4 && samePlane(positionsChunk))) {
                    for (Direction direction : connections) {
                        BlockEntity blockEntity = pLevel.getBlockEntity(pos);

                        if (blockEntity instanceof MultiblockBlockEntity multiblockBlock &&
                                multiblockBlock.getDSProperty(direction) == DirectionState.CLOSED) {
                            multiblockBlock.setCustomProperty(direction, DirectionState.OPENED);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enum that defines the action type to perform on the `target` block
     */
    public enum Action {
        SET,
        REMOVE
    }
}