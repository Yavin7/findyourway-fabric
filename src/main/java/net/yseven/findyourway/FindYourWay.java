package net.yseven.findyourway;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.yseven.findyourway.item.StructureCompass;

public class FindYourWay implements ModInitializer {

	public static final Identifier PROVIDE_FEATURE_POSITION_ID = new Identifier("findyourway", "providefeaturepos");
	public static final Identifier RECEIVE_FEATURE_POSITION_ID = new Identifier("findyourway", "receivefeaturepos");

	public static final StructureCompass ENDER_COMPASS = new StructureCompass("Stronghold",
			new Item.Settings().group(ItemGroup.TOOLS).maxCount(1));

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("findyourway", "ender_compass"), ENDER_COMPASS);

		ServerSidePacketRegistry.INSTANCE.register(PROVIDE_FEATURE_POSITION_ID, (packetContext, attachedData) -> {
			BlockPos pos = attachedData.readBlockPos();
			String featureType = attachedData.readString();
			int invSlot = attachedData.readInt();
			packetContext.getTaskQueue().execute(() -> {

				ServerWorld world = (ServerWorld)packetContext.getPlayer().world;
				BlockPos featureLocation = world.getChunkManager().getChunkGenerator().locateStructure(world,
						featureType, pos, 1000, false);

				if(featureLocation != null) {
					CompoundTag tag = new CompoundTag();
                    tag.putInt("posX", featureLocation.getX());
					tag.putInt("posZ", featureLocation.getZ());
					ENDER_COMPASS.setTargetLocation(tag);
					packetContext.getPlayer().inventory.getInvStack(invSlot).getItem().shouldSyncTagToClient();
					
					PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());

					passedData.writeBlockPos(featureLocation);
					passedData.writeInt(invSlot);

					ServerSidePacketRegistry.INSTANCE.sendToPlayer(packetContext.getPlayer(), RECEIVE_FEATURE_POSITION_ID, passedData);
				}
			});
		});
	}
}
