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
 * VEエネルギーキューブのアイテム（ツールチップ付き）
 */

public class VEEnergyCubeItem extends BlockItem {

    public VEEnergyCubeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 基本説明
        tooltip.add(Component.translatable("tooltip.valineenergycore.ve_energy_cube")
                .withStyle(ChatFormatting.GRAY));

        // NBTからエネルギー量を取得（保存されている場合）
        if (stack.hasTag() && stack.getTag().contains("BlockEntityTag")) {
            var beTag = stack.getTag().getCompound("BlockEntityTag");
            if (beTag.contains("Buffer")) {
                var bufferTag = beTag.getCompound("Buffer");
                // TODO: エネルギー量を表示
            }
        }

        // SHIFTキーで詳細表示
        if (Screen.hasShiftDown()) {
            // 容量表示
            tooltip.add(Component.translatable("tooltip.valineenergycore.ve_energy_cube.capacity",
                            VEMemoryManager.getMaxVECapacity().toString())
                    .withStyle(ChatFormatting.AQUA));

            // 使用方法
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.valineenergycore.ve_energy_cube.info")
                    .withStyle(ChatFormatting.YELLOW));

            // メモリ情報
            VEMemoryManager.MemoryInfo memInfo = VEMemoryManager.getMemoryInfo();
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("info.valineenergycore.memory.current",
                            memInfo.getMaxMemoryMB())
                    .withStyle(ChatFormatting.GOLD));

            tooltip.add(Component.translatable("info.valineenergycore.memory.max_capacity",
                            VEMemoryManager.getMaxVECapacity().toString())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));

        } else {
            tooltip.add(Component.translatable("tooltip.valineenergycore.shift_for_details")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}