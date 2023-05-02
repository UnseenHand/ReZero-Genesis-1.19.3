package net.unseenhand.rezerogenesismod;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;
import java.util.function.BiConsumer;

public class ModPacketHandler {
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ReZeroGenesisMod.MOD_ID, "messages"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

//    private void testMethod() {
//        // Local variable for the ID -> id++
//        String message = "Here I go";
//        INSTANCE.registerMessage(
//                ID++,
//                "Here I go",
//                (m, p) -> System.out.println("d"),
//                INSTANCE.messageBuilder().encoder()
//        ;
//        INSTANCE.sendToServer("Here I go");
//    }
}
