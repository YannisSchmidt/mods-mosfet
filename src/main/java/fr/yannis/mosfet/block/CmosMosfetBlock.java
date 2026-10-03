package fr.yannis.mosfet.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * MOSFET canal P (le complementaire du canal N) : il conduit quand la grille est
 * <b>strictement inferieure</b> a la source. Le drain prend alors exactement la
 * valeur de la source, sinon il ne delivre rien.
 */
public class CmosMosfetBlock extends MosfetBlock {

	public static final MapCodec<CmosMosfetBlock> CODEC = simpleCodec(CmosMosfetBlock::new);

	public CmosMosfetBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<CmosMosfetBlock> codec() {
		return CODEC;
	}

	@Override
	protected int getDrainPower(Level level, BlockPos pos, BlockState state) {
		int source = getSourcePower(level, pos, state);
		int gate = getGatePower(level, pos, state);

		return gate < source ? source : 0;
	}
}
