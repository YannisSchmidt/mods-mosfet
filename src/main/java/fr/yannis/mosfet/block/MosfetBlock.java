package fr.yannis.mosfet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;

/**
 * Socle commun des deux MOSFET.
 *
 * <p>Geometrie (le bloc est orientable a l'horizontale, {@code FACING} pointe vers
 * la sortie) :</p>
 * <ul>
 *   <li><b>Face avant ({@code FACING})</b> : le <b>drain</b> (sortie).</li>
 *   <li><b>Face arriere</b> : la <b>source</b> (entree de signal a faire passer).</li>
 *   <li><b>Faces laterales</b> : la <b>grille</b> (gate), la borne de commande.</li>
 * </ul>
 */
public abstract class MosfetBlock extends HorizontalDirectionalBlock {

	protected MosfetBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		// Le drain (sortie) pointe devant le joueur, comme la fleche d'un repetiteur.
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}

	// ------------------------------------------------------------------
	// Bornes
	// ------------------------------------------------------------------

	protected static Direction getDrainDirection(BlockState state) {
		return state.getValue(FACING);
	}

	protected static Direction getSourceDirection(BlockState state) {
		return state.getValue(FACING).getOpposite();
	}

	/** Signal present sur la source (entree, face arriere). */
	protected static int getSourcePower(Level level, BlockPos pos, BlockState state) {
		return getIncomingPower(level, pos, getSourceDirection(state));
	}

	/** Signal present sur la grille (gate, les deux faces laterales). */
	protected static int getGatePower(Level level, BlockPos pos, BlockState state) {
		Direction facing = state.getValue(FACING);
		return Math.max(
				getIncomingPower(level, pos, facing.getClockWise()),
				getIncomingPower(level, pos, facing.getCounterClockWise())
		);
	}

	/**
	 * Puissance arrivant sur la face {@code side} du bloc.
	 *
	 * <p>Convention vanilla (cf. {@code SignalGetter#getSignal}) : la puissance
	 * fournie par le voisin situe dans la direction {@code side} s'obtient avec
	 * {@code level.getSignal(pos.relative(side), side)}.</p>
	 */
	private static int getIncomingPower(Level level, BlockPos pos, Direction side) {
		return level.getSignal(pos.relative(side), side);
	}

	// ------------------------------------------------------------------
	// Comportement redstone
	// ------------------------------------------------------------------

	/** Signal effectivement delivre par le drain. */
	protected abstract int getDrainPower(Level level, BlockPos pos, BlockState state);

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		// Un bloc emet vers le voisin situe en "direction.getOpposite()" :
		// le MOSFET ne repond donc que pour la face du drain.
		if (direction != getDrainDirection(state).getOpposite()) {
			return 0;
		}

		return level instanceof Level signalGetter ? getDrainPower(signalGetter, pos, state) : 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		// Comme les diodes vanilla : le signal fort suit le signal faible, ce qui
		// permet d'alimenter un bloc plein pose devant le drain.
		return state.getSignal(level, pos, direction);
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	// ------------------------------------------------------------------
	// Propagation des mises a jour
	// ------------------------------------------------------------------

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston) {
		// Mise a jour differee d'un tick : evite les boucles de retour infinies
		// (un MOSFET reboucle sur sa propre grille) tout en restant quasi instantane.
		if (!level.isClientSide()) {
			level.scheduleTick(pos, this, 1);
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		this.updateDrainNeighbours(level, pos, state);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (!level.isClientSide()) {
			this.updateDrainNeighbours(level, pos, state);
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		if (!movedByPiston) {
			this.updateDrainNeighbours(level, pos, state);
		}
	}

	/** Previent le bloc branche sur le drain, puis ses voisins. */
	protected void updateDrainNeighbours(Level level, BlockPos pos, BlockState state) {
		Direction facing = state.getValue(FACING);
		BlockPos drainPos = pos.relative(facing);

		// null en dehors du mode "redstone experiments", exactement comme le fait vanilla.
		Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, facing, null);
		level.neighborChanged(drainPos, this, orientation);
		level.updateNeighborsAtExceptFromFacing(drainPos, this, facing.getOpposite(), orientation);
	}
}
