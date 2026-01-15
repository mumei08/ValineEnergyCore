package kaede.valineenergycore.common.item;

import kaede.valineenergycore.common.content.network.VECableTier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

public class VECableBlockItem extends BlockItem {

    private final VECableTier tier;

    public VECableBlockItem(Block block, Properties properties, VECableTier tier) {
        super(block, properties);
        this.tier = tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 基本説明
        tooltip.add(Component.translatable("tooltip.valineenergycore.ve_cable")
                .withStyle(ChatFormatting.GRAY));

        // SHIFTキーで詳細表示
        if (Screen.hasShiftDown()) {
            // Tier表示
            tooltip.add(Component.translatable("tooltip.valineenergycore.ve_cable.tier",
                            Component.translatable("info.valineenergycore.tier." + tier.name().toLowerCase()))
                    .withStyle(ChatFormatting.YELLOW));

            // 容量表示
            tooltip.add(Component.translatable("tooltip.valineenergycore.ve_cable.capacity",
                            tier.getCapacity().toString())
                    .withStyle(ChatFormatting.AQUA));

            // 転送レート表示
            tooltip.add(Component.translatable("tooltip.valineenergycore.ve_cable.transfer_rate",
                            tier.getTransferRate().toString())
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.valineenergycore.shift_for_details")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}