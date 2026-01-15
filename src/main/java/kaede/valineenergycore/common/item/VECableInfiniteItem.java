package kaede.valineenergycore.common.item;

import kaede.valineenergycore.api.energy.VEMemoryManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import javax.annotation.Nullable;
import java.util.List;

/**
 * VE無限ケーブルのアイテム（ツールチップ付き）
 */
public class VECableInfiniteItem extends BlockItem {

    public VECableInfiniteItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 基本説明
        tooltip.add(Component.translatable("tooltip.valineenergycore.ve_cable_infinite")
                .withStyle(ChatFormatting.GRAY));

        // SHIFTキーで詳細表示
        if (Screen.hasShiftDown()) {
            // 容量表示
            tooltip.add(Component.translatable("tooltip.valineenergycore.ve_cable_infinite.capacity",
                            VEMemoryManager.getMaxVECapacity().toString())
                    .withStyle(ChatFormatting.AQUA));

            // 転送レート表示
            tooltip.add(Component.translatable("tooltip.valineenergycore.ve_cable_infinite.transfer_rate")
                    .withStyle(ChatFormatting.GREEN));

            // 追加情報
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.valineenergycore.ve_cable_infinite.info")
                    .withStyle(ChatFormatting.YELLOW));

            tooltip.add(Component.translatable("tooltip.valineenergycore.ve_cable_infinite.formula")
                    .withStyle(ChatFormatting.DARK_GRAY));

            // メモリ情報
            VEMemoryManager.MemoryInfo memInfo = VEMemoryManager.getMemoryInfo();
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("info.valineenergycore.memory.current",
                            memInfo.getMaxMemoryMB())
                    .withStyle(ChatFormatting.GOLD));

        } else {
            tooltip.add(Component.translatable("tooltip.valineenergycore.shift_for_details")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
