package net.yseven.findyourway.item;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.yseven.findyourway.FindYourWay;

public class StructureCompass extends Item {
    private String targetType;
    private DimensionType targetDimension;

    public StructureCompass(String targetType, DimensionType dimension,  Settings settings) {
        super(settings);
        this.targetType = targetType;
        this.targetDimension = dimension;
        this.addPropertyGetter(new Identifier("angle"), new ItemPropertyGetter() {

            @Environment(EnvType.CLIENT)
            private double angle;
            @Environment(EnvType.CLIENT)
            private double step;
            @Environment(EnvType.CLIENT)
            private long lastTick;

            @Environment(EnvType.CLIENT)
            public float call(ItemStack stack, @Nullable World world, @Nullable LivingEntity user) {
                if(!stack.hasTag()) {stack.setTag(new CompoundTag());}
                if (user == null && !stack.isInFrame() || stack.getTag().isEmpty()) {
                    return 0.0F;
                } else {
                    boolean bl = user != null;
                    Entity entity = bl ? user : stack.getFrame();
                    if (world == null) {
                        world = ((Entity) entity).world;
                    }

                    double g;
                    if (world.dimension.getType() == StructureCompass.this.targetDimension) {
                        double d = bl ? (double) ((Entity) entity).yaw : this.getYaw((ItemFrameEntity) entity);
                        d = MathHelper.floorMod(d / 360.0D, 1.0D);
                        double e = this.getAngleToTarget(world, (Entity) entity, StructureCompass.this.targetType, stack)
                                / 6.2831854820251465D;
                        g = 0.5D - (d - 0.25D - e);
                    } else {
                        g = Math.random();
                    }

                    if (bl) {
                        g = this.getPointingAngle(world, g);
                    }

                    return MathHelper.floorMod((float) g, 1.0F);
                }
            }

            @Environment(EnvType.CLIENT)
            private double getPointingAngle(World world, double entityYaw) {
                if (world.getTime() != this.lastTick) {
                    this.lastTick = world.getTime();
                    double d = entityYaw - this.angle;
                    d = MathHelper.floorMod(d + 0.5D, 1.0D) - 0.5D;
                    this.step += d * 0.1D;
                    this.step *= 0.8D;
                    this.angle = MathHelper.floorMod(this.angle + this.step, 1.0D);
                }

                return this.angle;
            }

            @Environment(EnvType.CLIENT)
            private double getYaw(ItemFrameEntity entity) {
                return (double) MathHelper.wrapDegrees(180 + entity.getHorizontalFacing().getHorizontal() * 90);
            }

            @Environment(EnvType.CLIENT)
            private double getAngleToTarget(World world, Entity entity, String target, ItemStack stack) {
                if(!stack.getTag().contains("posx")){
                    return 0;
                } else {
                    return Math.atan2((double) stack.getTag().getInt("posX") - entity.getZ(), (double) stack.getTag().getInt("posX") - entity.getX());
                }
            }
        });
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        if(world.isClient) {
            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());

            passedData.writeBlockPos(new BlockPos(playerEntity.getPos()));
            passedData.writeString(this.targetType);
            passedData.writeInt(playerEntity.inventory.selectedSlot);
    
            ClientSidePacketRegistry.INSTANCE.sendToServer(FindYourWay.PROVIDE_FEATURE_POSITION_ID, passedData);
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, playerEntity.getStackInHand(hand));
    }
}
