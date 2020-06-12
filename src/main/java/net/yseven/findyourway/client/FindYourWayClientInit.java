package net.yseven.findyourway.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.yseven.findyourway.FindYourWay;

public class FindYourWayClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSidePacketRegistry.INSTANCE.register(FindYourWay.RECEIVE_FEATURE_POSITION_ID,
            (packetContext, attachedData) -> {
                BlockPos pos = attachedData.readBlockPos();
                int invSlot = attachedData.readInt();
                packetContext.getTaskQueue().execute(() -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("posX", pos.getX());
                    tag.putInt("posZ", pos.getZ());
                    packetContext.getPlayer().inventory.getInvStack(invSlot).setTag(tag);
                });
            });
    }
}