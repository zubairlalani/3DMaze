public class Explorer extends Location{


	public Explorer(int x, int y){
		super(x, y);
	}

	public void move(int x, int y){
		setX(getX() +x);
		setY(getY()+y);
	}

}