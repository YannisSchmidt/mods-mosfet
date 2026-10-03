package fr.yannis.mosfet.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * MOSFET canal N : il conduit quand la grille est <b>superieure ou egale</b> a la
 * source. Le drain prend alors exactement la valeur de la source (passage
 * analogique du signal), sinon il ne delivre rien.
 */
public class NmosMosfetBlock extends MosfetBlock {

	public static final MapCodec<NmosMosfetBlock> CODEC = simpleCodec(NmosMosfetBlock::new);

	public NmosMosfetBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<NmosMosfetBlock> codec() {
		return CODEC;
	}

	@Override
	protected int getDrainPower(Level level, BlockPos pos, BlockState state) {
		int source = getSourcePower(level, pos, state);
		int gate = getGatePower(level, pos, state);

		return gate >= source ? source : 0;
	}
}
