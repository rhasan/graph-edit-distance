package ged;

public class AlgorithmConfig {
	final public static int A_STAR = 0;
	final public static int A_STAR_BEAM = 1;
	final public static int BipartiteHungarian = 2;
	final public static int BipartiteVolgenantJonker = 3;
	
	private int algo;
	private int beamSize;
	public AlgorithmConfig() {
		beamSize = Integer.MAX_VALUE;
	}
	/**
	 * Creates Bipartite VolgenantJonker configuration.
	 * @return Bipartite VolgenantJonker configuration.
	 */
	public static AlgorithmConfig createBipartiteVolgenantJonkerConfig() {
		AlgorithmConfig ac = new AlgorithmConfig();
		ac.algo = BipartiteVolgenantJonker;
		return ac;
		
	}
	/**
	 * Creates Bipartite Hungarian configuration.
	 * @return Bipartite Hungarian configuration.
	 */
	public static AlgorithmConfig createBipartiteHungarian() {
		AlgorithmConfig ac = new AlgorithmConfig();
		ac.algo = BipartiteHungarian;
		return ac;
		
	}
	/**
	 * Creates A* configuration.
	 * @return A* configuration.
	 */
	public static AlgorithmConfig createAStar() {
		AlgorithmConfig ac = new AlgorithmConfig();
		ac.algo = A_STAR;
		return ac;
		
	}
	/**
	 * Creates A*-beam configuration.
	 * @param s Size of the beam
	 * @return A*-beam configuration.
	 */

	public static AlgorithmConfig createAStarBeam(int s) {
		AlgorithmConfig ac = new AlgorithmConfig();
		ac.algo = A_STAR_BEAM;
		ac.beamSize = s;
		return ac;
	}
	/**
	 * Returns true if the configuration is for A*
	 * @return true or false
	 */
	public boolean isAStar() {
		if(this.algo==A_STAR)
			return true;
		return false;
	}
	/**
	 * Returns true if the configuration is for A*-beam
	 * @return true or false
	 */
	public boolean isAStarBeam() {
		if(this.algo==A_STAR_BEAM) {
			return true;
		}
		return false;
	}
	/**
	 * Returns true if the configuration is for Bipartite Hungarian
	 * @return true or false
	 */
	
	public boolean isBipartiteHungarian() {
		if(this.algo==BipartiteHungarian)
			return true;
		return false;
	}
	/**
	 * Returns true if the configuration is for Bipartite VolgenantJonker
	 * @return true or false
	 */
	public boolean isBipartiteVolgenantJonker() {
		if(this.algo==BipartiteVolgenantJonker) 
			return true;
		return false;
	}
	
	/**
	 * Gets the size of beam for A*-beam
	 * @return size of beam for A*-beam
	 */
	public int getBeamSize() {
		return beamSize;
	}
	/**
	 * Sets the size of beam for A*-beam
	 * @param beamSize size of beam for A*-beam
	 */
	public void setBeamSize(int beamSize) {
		this.beamSize = beamSize;
	}
	
	
	
}
