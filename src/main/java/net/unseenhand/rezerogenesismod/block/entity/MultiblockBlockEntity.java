package net.unseenhand.rezerogenesismod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.unseenhand.rezerogenesismod.client.gui.menu.MixingApparatusMenu;
import net.unseenhand.rezerogenesismod.item.ModItems;
import net.unseenhand.rezerogenesismod.network.ModMessages;
import net.unseenhand.rezerogenesismod.network.protocol.FluidSyncS2CPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class MultiblockBlockEntity extends BlockEntity implements MenuProvider {
    public static final String INVENTORY_KEY = "inventory";
    public static final String UUID_KEY = "uuid";
    protected final ContainerData data;
    protected final FluidTank fluidHandler = new FluidTank(12000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                ModMessages.sendToClients(new FluidSyncS2CPacket(this.getFluid(), worldPosition));
            }
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.WATER;
        }
    };
    protected final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.getItem() == Items.COAL;
                case 1 -> stack.getItem() == ModItems.COBBLESTONE_MIXTURE.get();
                case 2 -> false;
                case 3 -> stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
                default -> super.isItemValid(slot, stack);
            };
        }
    };
    protected int maxProgress = 78;
    protected int progress = 0;
    private MultiblockDirectionStateProperty directionStateProperty;
    private UUID ID;
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public MultiblockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        directionStateProperty = new MultiblockDirectionStateProperty(DirectionState.OPENED); // default value
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> maxProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 2; // should not be the magical number
            }
        };
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerID, @NotNull Inventory pInventory, @NotNull Player pPlayer) {
        ModMessages.sendToClients(new FluidSyncS2CPacket(this.getFluidStack(), worldPosition));
        return new MixingApparatusMenu(containerID, pInventory, this, this.data);
    }

    public void drops(Level level) {
        // Item Drops Handling
        int slots = itemHandler.getSlots();
        SimpleContainer inventory = new SimpleContainer(slots);
        for (int i = 0; i < slots; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inventory);

        // Fluid Drops Handling
        // TEMPORARY!!!
        if (fluidHandler.getFluidAmount() > 0) {
            FluidStack fluidStack = fluidHandler.getFluid();
            ItemStack fluidItemStack = FluidUtil.getFilledBucket(fluidStack);
            if (!fluidItemStack.isEmpty()) {
                Block.popResource(level, worldPosition, fluidItemStack);
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    /**
     * This method is the get method to retrieve
     * the mapped value (DS property) by its key
     *
     * @param pDirection the direction to get the mapped {@link DirectionState}
     *                   in the {@link #directionStateProperty}
     * @return the {@code OPENED/CLOSED/CONNECTED} {@link DirectionState} value
     * based on the {@link Direction} key
     */
    public DirectionState getDSProperty(Direction pDirection) {
        return directionStateProperty.get(pDirection);
    }

    public FluidStack getFluidStack() {
        return fluidHandler.getFluid();
    }

    public void setFluidStack(FluidStack stack) {
        this.fluidHandler.setFluid(stack);
    }

    public UUID getID() {
        return ID;
    }

    public void setID(UUID ID) {
        this.ID = ID;
    }

    public boolean hasConnections(int number) {
        int counter = 0;
        for (Direction direction : Direction.values()) {
            if (this.getDSProperty(direction) == DirectionState.CONNECTED) {
                counter++;
                if (counter == number) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> fluidHandler);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        ID = nbt.getUUID(UUID_KEY);
        itemHandler.deserializeNBT(nbt.getCompound(INVENTORY_KEY));
        directionStateProperty.deserializeNBT(nbt.getCompound(MultiblockDirectionStateProperty.DIRECTION_DATA_KEY));
        fluidHandler.readFromNBT(nbt);
    }

    protected void resetProgress() {
        this.progress = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putUUID(UUID_KEY, ID);
        nbt.put(INVENTORY_KEY, itemHandler.serializeNBT());
        nbt.put(MultiblockDirectionStateProperty.DIRECTION_DATA_KEY, directionStateProperty.serializeNBT());
        nbt = fluidHandler.writeToNBT(nbt);

        super.saveAdditional(nbt);
    }

    public void setCustomProperty(Direction dir, DirectionState value) {
        directionStateProperty = directionStateProperty.with(dir, value);
    }
}