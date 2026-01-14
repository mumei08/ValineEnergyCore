package kaede.valineenergycore.common.content.network;

import kaede.valineenergycore.api.energy.IVEContainer;
import net.minecraft.core.BlockPos;

/**
 * Acceptor (エネルギー受容側) のデータ
 */

public class AcceptorData {
    private final BlockPos position;
    private final IVEContainer container;

    public AcceptorData(BlockPos position, IVEContainer container) {
        this.position = position;
        this.container = container;
    }

    public BlockPos getPosition() {
        return position;
    }

    public IVEContainer getContainer() {
        return container;
    }
}
