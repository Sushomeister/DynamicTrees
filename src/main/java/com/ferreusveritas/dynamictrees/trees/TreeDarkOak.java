package com.ferreusveritas.dynamictrees.trees;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.VanillaTreeData;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.Biome;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockAccess;
import com.ferreusveritas.dynamictrees.api.backport.SpeciesRegistry;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.cells.Cells;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.cells.CellDarkOakLeaf;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeDarkOak extends DynamicTree {
	
	public class SpeciesDarkOak extends Species {

		SpeciesDarkOak(DynamicTree treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Dark Oak Trees are tall, slowly growing, thick trees
			setBasicGrowingParameters(0.35f, 18.0f, 6, 8, 0.8f);
			
			setSoilLongevity(14);//Grows for a long long time
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.25f);
			envFactor(Type.MUSHROOM, 1.25f);

		}

		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, BiomeGenBase.roofedForest);
		};
		
		@Override
		public ArrayList<ItemStack> getDrops(IBlockAccess blockAccess, BlockPos pos, int chance, ArrayList<ItemStack> drops) {
			Random rand = blockAccess instanceof World ? ((World)blockAccess).rand : new Random();
			if ((rand.nextInt(chance) == 0)) {
				drops.add(new ItemStack(Items.apple, 1, 0));
			}
			return drops;
		}
		
		@Override
		public int getLowestBranchHeight(World world, BlockPos pos) {
			return (int)(super.getLowestBranchHeight(world, pos) * biomeSuitability(world, pos));
		}
		
		@Override
		public float getEnergy(World world, BlockPos pos) {
			return super.getEnergy(world, pos) * biomeSuitability(world, pos);
		}
		
		@Override
		public float getGrowthRate(World world, BlockPos pos) {
			return super.getGrowthRate(world, pos) * biomeSuitability(world, pos);
		}

		@Override
		protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {
			
			if(signal.numTurns >= 1) {//Disallow up/down turns after having turned out of the trunk once.
				probMap[EnumFacing.UP.getIndex()] = 0;
				probMap[EnumFacing.DOWN.getIndex()] = 0;
			}
			
			//Amplify cardinal directions to encourage spread(beware! this algorithm is wacked-out poo brain and should be redone)
			float energyRatio = signal.delta.getY() / getEnergy(world, pos);
			float spreadPush = energyRatio * energyRatio * energyRatio * 4;
			spreadPush = spreadPush < 1.0f ? 1.0f : spreadPush;
			
			for(EnumFacing dir: EnumFacing.HORIZONTALS) {
				probMap[dir.ordinal()] *= spreadPush;
			}
			
			return probMap;
		}
	}
	
	Species species;
	
	public TreeDarkOak() {
		super(VanillaTreeData.EnumType.DARKOAK);
		
		setCellSolver(Cells.darkOakSolver);
		setSmotherLeavesMax(3);//thin canopy
	}
	
	@Override
	public void createSpecies() {
		species = new SpeciesDarkOak(this);
	}
	
	@Override
	public void registerSpecies(SpeciesRegistry speciesRegistry) {
		speciesRegistry.register(species);
	}
	
	@Override
	public Species getCommonSpecies() {
		return species;
	}
	
	protected static final ICell darkOakLeafCells[] = {
			Cells.nullCell,
			new CellDarkOakLeaf(1),
			new CellDarkOakLeaf(2),
			new CellDarkOakLeaf(3),
			new CellDarkOakLeaf(4)
		}; 
	
	@Override
	public ICell getCellForLeaves(int hydro) {
		return darkOakLeafCells[hydro];
	}
	
	@Override
	public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random) {
		if(super.rot(world, pos, neighborCount, radius, random)) {
			if(radius > 2 && TreeHelper.isRootyDirt(world, pos.down()) && world.getLightFor(EnumSkyBlock.Sky, pos) < 6) {
				world.setBlockState(pos, ModBlocks.blockStates.redMushroom);//Change branch to a red mushroom
				world.setBlockState(pos.down(), ModBlocks.blockStates.podzol);//Change rooty dirt to Podzol
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public void createLeafCluster(){
		
		setLeafCluster(new SimpleVoxmap(7, 5, 7, new byte[] {
				
			//Layer 0(Bottom)
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 2, 2, 2, 0, 0,
			0, 2, 0, 0, 0, 2, 0,
			0, 2, 0, 0, 0, 2, 0,
			0, 2, 0, 0, 0, 2, 0,
			0, 0, 2, 2, 2, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			
			//Layer 1
			0, 0, 1, 1, 1, 0, 0,
			0, 1, 2, 2, 2, 1, 0,
			1, 2, 3, 4, 3, 2, 1,
			1, 2, 4, 0, 4, 2, 1,
			1, 2, 3, 4, 3, 2, 1,
			0, 1, 2, 2, 2, 1, 0,
			0, 0, 1, 1, 1, 0, 0,
			
			//Layer 2
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 1, 2, 2, 2, 1, 0,
			0, 1, 2, 4, 2, 1, 0,
			0, 1, 2, 2, 2, 1, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			
			//Layer 3
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 0, 1, 2, 1, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			
			//Layer 4 (Top)
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 1, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0
			
		}).setCenter(new BlockPos(3, 1, 3)));
	}
}
