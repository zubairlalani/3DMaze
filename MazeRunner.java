import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;
import java.util.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Timer;
import java.util.TimerTask;
import java.io.PrintWriter;
import java.io.File;
public class MazeRunner extends JPanel implements KeyListener,MouseListener
{
	JFrame frame;

	Wall[][] walls =null;	//keeps track of characters in text file
	int direction = 0;		//direction that the character is facing
	Explorer explorer = new Explorer(0, 1);

	//holds the trapezoid polygons for 3D
	ArrayList<Polygon> leftWalls = new ArrayList<>();
	ArrayList<Polygon> rightWalls = new ArrayList<>();
	ArrayList<Polygon> floor = new ArrayList<>();
	ArrayList<Polygon> ceiling = new ArrayList<>();

	//For Spray Paint/BreadCrumb Portion
	ArrayList<Point> painted = new ArrayList<>();
	boolean paint1 = false;
	boolean paint2 = false;
	boolean paint3 = false;
	int paintMax = 5;
	int paintCount = 0;
	int random = (int)(Math.random()*8)+0;

	//For Flashlight/dimming lights portion
    ArrayList<Point> flashlightPoints = new ArrayList<>();
	boolean done =false;
	int gradientChange= 0;
	int vision = 20;
	boolean flashlightCoordinate = false;

	int halllength =0;		//length from explorer to wall
	int temp;
	int maxVision = 5;
	boolean moved = false;
	boolean toggleMap = false;

	//FOR KEEPING PERSONAL RECORD/SAVE DATA PORTION
	int moveCounter =0;
	int secondsPassed = 0;
	int hours =0;
	int minutes =0;
	int recordHours;
	int recordMinutes;
	int recordSeconds;

	//Used for stopwatch
	Timer timer = new Timer();
	TimerTask task = new TimerTask(){
		public void run(){
			secondsPassed++;

			if(secondsPassed>59){
				minutes++;
				secondsPassed=0;
			}
			if(minutes>69){
				hours++;
				minutes=0;
			}
			repaint();
			//System.out.println("SECONDS PASSED: "+secondsPassed);
		}
	};

	public MazeRunner()
	{
		setBoard();
		frame=new JFrame();
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000,800);
		frame.setVisible(true);
		frame.addKeyListener(this);

		//Points where a flashlight can appear to help restore vision
		flashlightPoints.add(new Point(2, 3));
		flashlightPoints.add(new Point(5, 1));
		flashlightPoints.add(new Point(7, 5));
		flashlightPoints.add(new Point(11, 3));
		flashlightPoints.add(new Point(9, 2));
		flashlightPoints.add(new Point(13, 1));
		flashlightPoints.add(new Point(14, 7));
		flashlightPoints.add(new Point(16, 8));

		System.out.println("FLASH LIGHT : "+flashlightPoints.get(random));

		//retrieves saved data for personal record
		File saveFile = new File("SaveData.txt");
		try
		{
			BufferedReader save = new BufferedReader(new FileReader(saveFile));
			String text;
			int lineNumber =1;
			while( (text=save.readLine())!= null)
			{
				String[] lengths = text.split(" ");				//prints out text file
				if(lineNumber==1){
					recordHours = Integer.parseInt(lengths[1]);
				}
				else if(lineNumber==2){
					recordMinutes = Integer.parseInt(lengths[1]);
				}
				else if(lineNumber==3){
					recordSeconds = Integer.parseInt(lengths[1]);
				}
				lineNumber++;
			}

		}catch(Exception e){

		}

		//this.addMouseListener(this); //in case you need mouse clicking
	}
	public void start(){
		timer.scheduleAtFixedRate(task, 0, 1000);
	}

	public void paintComponent(Graphics g)
	{

		super.paintComponent(g);
		//setBoard();
		setWalls();

		Graphics2D g2 = (Graphics2D)g;
		g.setColor(Color.BLACK);
		g.fillRect(0,0,1000,800);


		GradientPaint blueToBlack = new GradientPaint(0, 0, Color.BLUE, 0, 250-gradientChange, Color.BLACK);
		GradientPaint blueToBlack2 = new GradientPaint(0, 500+gradientChange, Color.BLACK, 0, 750, Color.BLUE);
		for(int x=0;x<Math.min(maxVision, ceiling.size());x++)		//whenever maxVision is smaller only draws polygons up till the maxVision
		{
			if(paint1){
				g2.setPaint(Color.GREEN);
			}
			else
				g2.setPaint(blueToBlack);
			g2.fillPolygon(ceiling.get(x));
			g2.setColor(Color.BLACK);
			g2.drawPolygon(ceiling.get(x));


			g2.setColor(Color.BLACK);
			g2.drawPolygon(floor.get(x));

			if(flashlightCoordinate){
				g2.setPaint(Color.YELLOW);
				flashlightCoordinate = false;
				g2.drawString("CONGRATULATIONS! YOU FOUND A FLASH LIGHT!", 400, 555);
			}
			else if(paint1){
				g2.setPaint(Color.GREEN);
				paint1=false;
			}
			else
				g2.setPaint(blueToBlack2);
			g2.fillPolygon(floor.get(x));

		}

		GradientPaint magentaToBlack = new GradientPaint(1000, 0, Color.MAGENTA, 700+gradientChange, 0, Color.BLACK);

		for(int x=0;x<Math.min(maxVision, rightWalls.size());x++)
		{
			g2.setColor(Color.BLACK);
			g2.drawPolygon(rightWalls.get(x));
			if(paint2){
				g2.setPaint(Color.GREEN);
				paint2=false;
			}
			else
				g2.setPaint(magentaToBlack);
			g2.fillPolygon(rightWalls.get(x));
		}

		GradientPaint magentaToBlack2 = new GradientPaint(0, 0, Color.MAGENTA, 300-gradientChange, 0, Color.BLACK);
		for(int x=0;x<Math.min(maxVision, leftWalls.size());x++)
		{
			if(paint3){
				g2.setPaint(Color.GREEN);
				paint3 = false;
			}
			else
				g2.setPaint(magentaToBlack2);
			g2.fillPolygon(leftWalls.get(x));
			g2.setColor(Color.BLACK);
			g2.drawPolygon(leftWalls.get(x));
        }


        //Display 2-D Maze Code
		if(gradientChange<240){
			if(halllength == 1){
				System.out.println("HELLLO");
				g2.setColor(Color.MAGENTA.darker());
				g2.drawRect(50,50,900,700);
				g2.fillRect(50,50,900,700);
			}
			vision=0;


		}
		if(gradientChange<200){
			if(halllength == 2){
						System.out.println("HELLLO");
						g2.setColor(Color.MAGENTA.darker().darker());
						g2.drawRect(100,100,800,600);
						g2.fillRect(100,100,800,600);
			}
			vision=5;
		}
		if(gradientChange<150){
			if(halllength == 3){
						System.out.println("HELLLO");
						g2.setColor(Color.MAGENTA.darker().darker().darker().darker());
						g2.drawRect(150,150,700,500);
						g2.fillRect(150,150,700,500);
			}
			vision=15;
		}
		if(gradientChange<80){
			if(halllength == 4){
						System.out.println("HELLLO");
						g2.setColor(Color.MAGENTA.darker().darker().darker().darker().darker().darker());
						g2.drawRect(200,200,600,400);
						g2.fillRect(200,200,600,400);
			}
			vision=20;
		}


		//g2.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		//
		g.setColor(Color.ORANGE);
		g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		g.drawString("MOVE COUNTER: "+moveCounter, 300, 370);//print direction
		g.drawString("Vision: "+vision+"/20", 300, 400);
		g.drawString("Spray Paint Bottles: "+paintCount+"/5", 300, 450);
		if(vision==0){
			g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
			g.drawString("OMG YOU'RE BLIND! FIND A FLASHLIGHT!", 300, 470);
		}

		g.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		g.drawString("Controls:", 530, 480);
		g.drawString("Press Space Bar to Cheat with a minimap", 535, 495);
		g.drawString("Press R to cheat and restore vision without a flashlight", 535, 510);
		g.drawString("Press J to spray paint a wall", 535, 525);
		g.drawString("Press enter to reset Personal record to zero.", 535, 540);




		g.setFont(new Font("Times New Roman", Font.PLAIN, 32));
		if(secondsPassed<10 && minutes<10)
			g.drawString(0+hours+":"+0+minutes+":"+0+secondsPassed, 300, 300);
		else if(secondsPassed>=10 && minutes<10)
			g.drawString(0+hours+":"+0+minutes+":"+secondsPassed, 300, 300);
		else
			g.drawString(0+hours+":"+minutes+":"+secondsPassed, 300, 300);


		//g2.drawString(dir+"", 600, 500);


		if(done){
			System.out.println("DONEONOENOENOIENOEN");
			g.setColor(Color.ORANGE);
			timer.cancel();
			g2.drawString("Game Over!", 300, 500);
			if((hours*3600+(minutes*60)+secondsPassed)< (recordHours*3600+(recordMinutes*60)+recordSeconds) || (recordHours*3600+(recordMinutes*60)+recordSeconds) ==0 ){
				g.setFont(new Font("Times New Roman", Font.PLAIN, 32));
				recordHours=hours;
				recordMinutes = minutes;
				recordSeconds = secondsPassed;
				try{
					PrintWriter writer = new PrintWriter("SaveData.txt", "UTF-8");
					writer.println("Hours: "+hours);
					writer.println("Minutes: "+minutes);
					writer.println("Seconds: "+secondsPassed);
					writer.close();
				}catch(Exception e){

				}
			}
		}

		g.setFont(new Font("Times New Roman", Font.PLAIN, 32));
		if(secondsPassed<10 && minutes<10)
			g.drawString("PERSONAL RECORD: "+0+recordHours+":"+0+recordMinutes+":"+0+recordSeconds, 300, 250);
		else if(secondsPassed>=10 && minutes<10)
			g.drawString("PERSONAL RECORD: "+0+recordHours+":"+0+recordMinutes+":"+recordSeconds, 300, 250);
		else
			g.drawString("PERSONAL RECORD: "+0+recordHours+":"+recordMinutes+":"+recordSeconds, 300, 250);

		if(toggleMap){
			g.setColor(Color.RED);

				for(int x=0; x<walls.length; x++){
					for(int y=0; y<walls[0].length; y++){
						if(walls[x][y].getOccupier() == 0){
							g.fillRect(walls[x][y].getY()*10, walls[x][y].getX()*10, 10, 10 );
						}
					}

					//g.setColor(Color.WHITE);
					g.drawRect(explorer.getX()*10,explorer.getY()*10,10,10);
				}

		}

	}

	public void setBoard()
	{

		File name = new File("mazeone.txt");
		int r=0;
		try
		{
			BufferedReader input = new BufferedReader(new FileReader(name));
			String text;
			ArrayList<String> temp = new ArrayList<>();
			int mazeLength =0;
			while( (text=input.readLine())!= null)
			{
				r++;
				String[] lengths = text.split("");				//prints out text file
				mazeLength=lengths.length;
				temp.add(text);

				System.out.println(text);
			}
			walls = new Wall[r][mazeLength];					//Array of all the characters in the textfile
			for(int x=0; x<temp.size(); x++){
				for(int y=0; y<temp.get(x).length(); y++){
					if(temp.get(x).charAt(y) == '#'){
						walls[x][y] = new Wall(x,y,0);
					}
					else if(temp.get(x).charAt(y) == ' '){
						walls[x][y] = new Wall(x,y,1);
					}
					else if(temp.get(x).charAt(y) == 'e'){
						walls[x][y] = new Wall(x,y,2);
					}
					else if(temp.get(x).charAt(y) == 'E'){
						walls[x][y] = new Wall(x,y,3);
					}

				}
			}

		}
		catch (IOException io)
		{
			System.err.println("File error");
		}

	}

	public void setWalls()
	{

		ceiling.clear();
		floor.clear();
		leftWalls.clear();
		rightWalls.clear();

		int row = explorer.getX();
		int column = explorer.getY();
		System.out.println("X: " + row +"Y: "+column);

		halllength = 0;
		int adi = 0;
		int l =0;
		int adjustColumn = 0, adjustRow = 0;

		//teleport();

		try{
			switch(direction)
			{
				case 0:
				adi = explorer.getX();
				while(walls[column][row].getOccupier()!=0)
				{
					for(int x=0; x<painted.size(); x++){
						if(row == painted.get(x).getX() && column == painted.get(x).getY()){
							paint1=true;
							paint2=true;
							paint3=true;
							System.out.println("PAINT IS NOW TRUE");
						}
					}
					if(row == flashlightPoints.get(random).getX() && column == flashlightPoints.get(random).getY()){
						flashlightCoordinate =true;
					}
					row++;
					halllength++;
				}
				break;
				case 1:
				adi = explorer.getY();
				while(walls[column][row].getOccupier()!=0)
				{
					for(int x=0; x<painted.size(); x++){
						if(row == painted.get(x).getX() && column == painted.get(x).getY()){
							paint1=true;
							paint2=true;
							paint3=true;
							System.out.println("PAINT IS NOW TRUE");
						}
					}
					if(row == flashlightPoints.get(random).getX() && column == flashlightPoints.get(random).getY()){
						flashlightCoordinate =true;
					}
					column++;
					halllength++;
				}
				break;
				case 2:
				while(walls[column][row].getOccupier()!=0)
				{
					for(int x=0; x<painted.size(); x++){
						if(row == painted.get(x).getX() && column == painted.get(x).getY()){
							paint1=true;
							paint2=true;
							paint3=true;
							System.out.println("PAINT IS NOW TRUE");
						}
					}
					if(row == flashlightPoints.get(random).getX() && column == flashlightPoints.get(random).getY()){
						flashlightCoordinate =true;
					}
					row--;
					halllength++;
				}
				break;
				case 3:
				while(walls[column][row].getOccupier()!=0)
				{
					for(int x=0; x<painted.size(); x++){
						if(row == painted.get(x).getX() && column == painted.get(x).getY()){
							paint1=true;
							paint2=true;
							paint3=true;
							System.out.println("PAINT IS NOW TRUE");
						}
					}
					if(row == flashlightPoints.get(random).getX() && column == flashlightPoints.get(random).getY()){
						flashlightCoordinate =true;
					}
					column--;
					halllength++;
				}
				break;
				}
		    }catch(Exception e){

			}

		column = explorer.getY();
		row = explorer.getX();



		System.out.println("HALLLENGTH: "+halllength);


		for(int x=0;x<halllength;x++)
		{
			int[] floorx = {50*x, 1000-50*x, 950-50*x, 50+50*x};
			int[] floory = {800-50*x, 800-50*x, 750-50*x,750-50*x};
			int[] ceilingx = {50*x, 1000-50*x, 950-50*x, 50+50*x};
			int[] ceilingy = {50*x, 50*x, 50+50*x, 50+50*x};

			floor.add(new Polygon(floorx, floory, 4));
			ceiling.add(new Polygon(ceilingx, ceilingy, 4));

		}

		for(int i=0;i<halllength;i++) {
			int[] empty = {0,0,0,0};
			 l = i;
				int[] leftwallsx={50*(l),50+50*l,50+50*l,50*l};
				int[] leftwallsy={50*(l),50+50*(l),750-50*(l),800-50*(l)};
				int[] rightwallsx={950-50*(l),1000-50*l,1000-50*l,950-50*l};
				int[] rightwallsy={750-50*(l),800-50*(l),50*(l),50+50*(l)};

				if(canAddWall(direction,i,true))
					leftWalls.add(new Polygon(leftwallsx,leftwallsy,4));
				else
					leftWalls.add(new Polygon(empty,empty,4));

				if(canAddWall(direction,i,false))
					rightWalls.add(new Polygon(rightwallsx,rightwallsy,4));
				else
					rightWalls.add(new Polygon(empty,empty,4));

		}

	}

	// checks if coordinate distance away is a valid place to add walls.
	public boolean canAddWall(int dir, int i, boolean addOrSubtract) {
		int adjustRow;
		int adjustColumn;
		int adr = 0;
		int adc = 0;
		if(dir == 0) {
			adjustRow = 0;
			adjustColumn = -1;
			adr = 1;
			adc = 0;
		} else if(dir == 1) {
			adjustColumn = 0;
			adjustRow = 1;
			adr = 0;
			adc = 1;
		} else if(dir == 2) {
			adjustRow = 0;
			adjustColumn = 1;
			adr = -1;
			adc = 0;
		} else {
			adjustColumn = 0;
			adjustRow = -1;
			adr = 0;
			adc = -1;
		}
		if(addOrSubtract) {
			return (explorer.getY() + adjustColumn + (i * adc) >= 0 && explorer.getY() + adjustColumn + (i * adc) < walls.length) &&
					(explorer.getX() + adjustRow + (i * adr) >= 0 && explorer.getX() + adjustRow + (i * adr) < walls[0].length) &&
					(walls[explorer.getY() + adjustColumn + (i * adc)][explorer.getX() + adjustRow + (i * adr)].getOccupier() == 0);
		}
		else
			return (explorer.getY() - adjustColumn + (i * adc) >= 0 && explorer.getY() - adjustColumn + (i * adc) < walls.length) &&
					(explorer.getX() - adjustRow + (i * adr) >= 0 && explorer.getX() - adjustRow + (i * adr)< walls[0].length) &&
					(walls[explorer.getY() - adjustColumn + (i * adc)][explorer.getX() - adjustRow + (i * adr)].getOccupier() == 0);

	}

	public int positiveDirection(int d)
	{
		d++;
		if(d>3)
			d=0;
		return d;
	}

	public int negativeDirection(int d)
	{
		d--;
		if(d<0)
			d=3;
		return d;
	}


	public void keyPressed(KeyEvent e)
	{
		if(gradientChange<240){
			if(moveCounter%10 == 0 && moveCounter!=0){
				gradientChange+=40;
				System.out.println("gradientChange" + gradientChange);
			}
		}
		int mx=0;
		int my=0;
		boolean movable = true;
		if(!done){
			if(e.getKeyCode()==37) //left
			{
				direction=negativeDirection(direction);
			}
			if(e.getKeyCode() == 39) //right
			{
				direction=positiveDirection(direction);
			}
		}
		halllength=0;
		switch(direction){

			case 0:
				halllength=0;
				mx=1;
				my=0;
				break;
			case 1:
				halllength=0;
				my=1;
				mx=0;
				break;
			case 2:
				halllength=0;
				mx=-1;
				my=0;
				if(explorer.getX()+mx<0)
					movable = false;
				break;
			case 3:
				halllength=0;
				my=-1;
				mx=0;
				break;
		}
		if(done || !movable){
			mx=0;
			my=0;
		}

		if(walls[explorer.getY()+my][explorer.getX()+mx].getOccupier() != 0) //up
		{
			if(!done){
				if(e.getKeyCode()==38)
				{
					explorer.move(mx, my);
					if(explorer.getX() == flashlightPoints.get(random).getX() && explorer.getY() == flashlightPoints.get(random).getY()){
						gradientChange = 0;
						random = (int)(Math.random()*8)+0;
						System.out.println("FLASH LIGHT : "+flashlightPoints.get(random));
					}
					moveCounter++;
					try{
						leftWalls.remove(0);
						rightWalls.remove(0);
						ceiling.remove(0);
						floor.remove(0);
						moved =true;
					}
					catch(Exception exception){

					}
				}
			}
		}

		if(walls[explorer.getY()][explorer.getX()].getOccupier()==2)
                done = true;


		if(e.getKeyCode() == 32){
			if(toggleMap)
				toggleMap=false;
			else
				toggleMap = true;
		}
		if(paintCount<paintMax){
			if(e.getKeyCode() == 74){
				painted.add(new Point(explorer.getX(), explorer.getY()));
				paintCount++;
				System.out.println("ENTER");
			}
		}

		if(e.getKeyCode() == 82){
			gradientChange = 0;
			vision = 20;

		}
		if(e.getKeyCode()==10){
			recordHours=0;
			recordSeconds=0;
			recordMinutes=0;

			try{
				PrintWriter writer = new PrintWriter("SaveData.txt", "UTF-8");
				writer.println("Hours: "+recordHours);
				writer.println("Minutes: "+recordMinutes);
				writer.println("Seconds: "+recordSeconds);
				writer.close();
			}catch(Exception error){

			}
		}
		repaint();


	}

	public void keyReleased(KeyEvent e)
	{
	}
	public void keyTyped(KeyEvent e)
	{
	}
	public void mouseClicked(MouseEvent e)
	{
	}
	public void mousePressed(MouseEvent e)
	{
	}
	public void mouseReleased(MouseEvent e)
	{
	}
	public void mouseEntered(MouseEvent e)
	{
	}
	public void mouseExited(MouseEvent e)
	{
	}

	public static void main(String args[])
	{
		MazeRunner app=new MazeRunner();
		app.start();
	}
}