public class Wall extends Location{
	private int occupier;
	public Wall(int x, int y, int occupier){
		super(x, y);
		this.occupier = occupier;

	}

	public int getOccupier()
	{
		return occupier;
	}


}