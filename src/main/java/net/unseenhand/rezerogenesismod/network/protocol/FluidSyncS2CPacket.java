package net.unseenhand.rezerogenesismod.network.protocol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;
import net.unseenhand.rezerogenesismod.block.entity.MixingApparatus;
import net.unseenhand.rezerogenesismod.client.gui.menu.MixingApparatusMenu;

import java.util.function.Supplier;

public class FluidSyncS2CPacket {
    private final FluidStack stack;
    private final BlockPos pos;

    public FluidSyncS2CPacket(FluidStack stack, BlockPos pos) {
        this.stack = stack;
        this.pos = pos;
    }

    public FluidSyncS2CPacket(FriendlyByteBuf buf) {
        this.stack = buf.readFluidStack();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFluidStack(stack);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel clientLevel = minecraft.level;

            if (clientLevel != null) {
                if (clientLevel.getBlockEntity(pos) instanceof MixingApparatus blockEntity) {
                    blockEntity.setFluidStack(stack);

                    if (minecraft.player.containerMenu instanceof MixingApparatusMenu menu &&
                            menu.getBlockEntity().getBlockPos().equals(pos)) {
                        menu.setFluidStack(stack);
                    }
                }
            }
        });

        return true;
    }
}
