package net.unseenhand.rezerogenesismod.client.gui.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;
import net.unseenhand.rezerogenesismod.block.ModBlocks;
import net.unseenhand.rezerogenesismod.block.entity.MixingApparatus;
import org.jetbrains.annotations.Nullable;

public class MixingApparatusMenu extends AbstractContainerMenu {
    public MixingApparatus blockEntity;
    private ContainerData data;
    /**
     * The expected minimal size for the container that holds items
     */
    private static final int EXPECTED_CONTAINER_MIN_SIZE = 4;
    private FluidStack fluidStack;
    private Level level;

    private MixingApparatusMenu(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return null;
    }

    public MixingApparatusMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(
                containerId,
                inventory,
                inventory.player.level.getBlockEntity(data.readBlockPos()),
                new SimpleContainerData(2));
    }

    public MixingApparatusMenu(int pContainerId, Inventory pInventory, BlockEntity pBlockEntity, ContainerData pData) {
        this(ModMenuTypes.MIXING_APPARATUS_MENU.get(), pContainerId);
        checkContainerSize(pInventory, EXPECTED_CONTAINER_MIN_SIZE);
        blockEntity = (MixingApparatus) pBlockEntity;
        level = pInventory.player.level;
        data = pData;
        fluidStack = blockEntity.getFluidStack();

        addPlayerInventory(pInventory);
        addPlayerHotbar(pInventory);

        // The values below should be adaptive
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            addSlot(new SlotItemHandler(iItemHandler, 0, 12, 56));
            addSlot(new SlotItemHandler(iItemHandler, 1, 80, 8));
            addSlot(new SlotItemHandler(iItemHandler, 2, 80, 110));
            addSlot(new SlotItemHandler(iItemHandler, 3, 140, 8));
        });

        addDataSlots(pData);
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        int hotbarLeft = 8 + 176 + 18 * 3;
        for (int row = 0; ; ) {
            for (int col = 0; col < 9; ++col) {
                int slotIndex = col;
                this.addSlot(new Slot(playerInventory, slotIndex, hotbarLeft + row, 8 + col * 18));
            }
            break;
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int slotIndex = 8;
        for (int row = 0; row < 3; ++row) {
            int currentSlotIndex = 0;
            for (int col = 0; col < 9; ++col) {
                currentSlotIndex = slotIndex + col + row + 1;

                this.addSlot(
                        new Slot(playerInventory,
                                currentSlotIndex,
                                176 + row * 18,
                                8 + col * 18));
            }
            slotIndex = currentSlotIndex - (row + 1);
        }
    }

    public MixingApparatus getBlockEntity() {
        return blockEntity;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    public int getScaledProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1); // Max progress
        int progressArrowSize = 90; // This is the height of the progress arrow in pixels

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }
    // TODO: Crashes the game client
    /*
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot sourceSlot = slots.get(slotIndex);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copySourceStack = sourceStack.copy();
        // ISC - Inventory Slot Count
        int playerISC = InventoryMenu.INV_SLOT_START + InventoryMenu.INV_SLOT_END;
        int playerAndContainerISC = playerISC + EXPECTED_CONTAINER_MIN_SIZE;

        // Check if the slot clicked is one of the vanilla container slots
        if (slotIndex < playerISC) {
            // This is the vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, playerISC, playerAndContainerISC, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < playerAndContainerISC) {
            // This is TE slot so merge the stack into the player's inventory
            if (!moveItemStackTo(sourceStack, 0, playerISC, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slot index:" + slotIndex);
            return ItemStack.EMPTY;
        }

        // If stack size equals to 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player, sourceStack);
        return copySourceStack;
    }
    */

    public void setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
                ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player,
                ModBlocks.MIXING_APPARATUS.get());
    }
}