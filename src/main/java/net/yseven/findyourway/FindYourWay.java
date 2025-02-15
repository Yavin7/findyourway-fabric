package net.yseven.findyourway;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.yseven.findyourway.item.StructureCompass;

public class FindYourWay implements ModInitializer {

	public static final Identifier PROVIDE_FEATURE_POSITION_ID = new Identifier("findyourway", "providefeaturepos");
	public static final Identifier RECEIVE_FEATURE_POSITION_ID = new Identifier("findyourway", "receivefeaturepos");

	public static final StructureCompass ENDER_COMPASS = new StructureCompass("Stronghold", DimensionType.OVERWORLD,
			new Item.Settings().group(ItemGroup.TOOLS).maxCount(1));
	public static final StructureCompass FORTRESS_COMPASS = new StructureCompass("Fortress", DimensionType.THE_NETHER,
			new Item.Settings().group(ItemGroup.TOOLS).maxCount(1));	
	public static final StructureCompass MANSION_COMPASS = new StructureCompass("Mansion", DimensionType.OVERWORLD,
			new Item.Settings().group(ItemGroup.TOOLS).maxCount(1));
	public static final StructureCompass MONUMENT_COMPASS = new StructureCompass("Monument", DimensionType.OVERWORLD,
			new Item.Settings().group(ItemGroup.TOOLS).maxCount(1));
	public static final StructureCompass VILLAGE_COMPASS = new StructureCompass("Village", DimensionType.OVERWORLD,
			new Item.Settings().group(ItemGroup.TOOLS).maxCount(1));

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("findyourway", "ender_compass"), ENDER_COMPASS);
		Registry.register(Registry.ITEM, new Identifier("findyourway", "fortress_compass"), FORTRESS_COMPASS);
		Registry.register(Registry.ITEM, new Identifier("findyourway", "mansion_compass"), MANSION_COMPASS);
		Registry.register(Registry.ITEM, new Identifier("findyourway", "monument_compass"), MONUMENT_COMPASS);
		Registry.register(Registry.ITEM, new Identifier("findyourway", "village_compass"), VILLAGE_COMPASS);
	
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
					packetContext.getPlayer().inventory.getInvStack(invSlot).setTag(tag);

					PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());

					passedData.writeBlockPos(featureLocation);
					passedData.writeInt(invSlot);

					ServerSidePacketRegistry.INSTANCE.sendToPlayer(packetContext.getPlayer(), RECEIVE_FEATURE_POSITION_ID, passedData);
				}
			});
		});
	}
}
