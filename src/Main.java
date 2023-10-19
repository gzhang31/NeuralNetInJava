import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Random;
public class Main {
	
	static JFrame window;
	static ImageIcon body = new ImageIcon("body.png"), blank = new ImageIcon("blank.png"), food = new ImageIcon("food.png");
	static JLabel[][] labels;
	static byte[][] map;
	static Queue<Pair> snake = new LinkedList<Pair>();
	static boolean makeWindow = false;
	static final int NUMROW = 10, NUMCOL = 10, ICONSIZE = 60, U = 0, R = 1, D = 2, L = 3, FOOD = 2, BODY = 1, BLANK = 0;
	static Random rng = new Random();
	static int headX, headY, facing, nextTurn, score, foodX, foodY;
	static int[][] dir = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
	
	static void init() {
		map = new byte[NUMROW][NUMCOL];
		headX = 4; headY = 4; facing = D; nextTurn = D; score = 1;
		if(makeWindow) {
			for(int i = 0; i < NUMROW; i ++) {
				for(int j = 0; j < NUMCOL; j ++) {
					labels[i][j].setIcon(blank);
				}
			}
			labels[headX][headY].setIcon(body);
		}
		map[headX][headY] = BODY;
		snake.clear();
		snake.add(new Pair(headX, headY));
		spawnFood();
	}
	
	static boolean move() {
		headX += dir[facing][0]; headY += dir[facing][1];
		
		try {

			if(map[headX][headY] != FOOD) {
				Pair tail = snake.poll();
				map[tail.x][tail.y] = BLANK;
				labels[tail.x][tail.y].setIcon(blank);
			}else {
				score ++;
				spawnFood();
			}
			
			if(map[headX][headY] == BODY) {
				return false;
			}
			
			map[headX][headY] = BODY;
			if(makeWindow) {
				labels[headX][headY].setIcon(body);
			}
			snake.add(new Pair(headX, headY));
			
		}catch(ArrayIndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}
	
	static void turn(int direction) {
		if(facing % 2 != direction % 2) {
			facing = direction;
		}
	}
	
	static void end() {
		wait(2000);
		System.exit(0);
	}
	
	static void spawnFood() {
		int x = 0;
		int y = 0;
		
		do {
			x = (int) (rng.nextFloat() * NUMROW);
			y = (int) (rng.nextFloat() * NUMCOL);
		}while(map[x][y] != BLANK);
		map[x][y] = FOOD;
		if(makeWindow) {
			labels[x][y].setIcon(food);
		}
		foodX = x;
		foodY = y;
	}
	
	static void wait(int mili) {
		try {
			Thread.sleep(mili);
		}catch(Exception e) {}
	}
	
	static int play() {
		init();
		
		wait(250);
		while(move()) {
			wait(250);
			turn(nextTurn);
		}
		return score;
	}
	
	static int rayTrace(int x, int y, int dirX, int dirY, int search) {
		int dist = 1;
		while(true) {
			try {
				if(map[x + dirX * dist][y + dirY * dist] == search) {
					return dist;
				}
			}catch(ArrayIndexOutOfBoundsException e) {
				if(search == BODY) {
					return dist;
				}else {
					return Integer.MAX_VALUE;
				}
			}
			dist ++;
		}
	}
	
	static float aiPlay(Network nn) {
		init();
		
		int[][] rTDir = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
		//sees[] 0-7 is body 8-15 is food 16-19 is direction currently facing 20-23 is distance to wall
		float[] sees = new float[6];
		int movesWithoutScore = 0;
		int currScore = score;
		float aliveBonus = 0;
		while(move() && movesWithoutScore < 100) {
			if(currScore > score) {
				movesWithoutScore = 0;
			}else {
				movesWithoutScore ++;
			}
			for(int i = 0; i < 4; i ++) {
				sees[i] = (float) 1 / rayTrace(headX, headY, rTDir[i][0], rTDir[i][1], BODY);
			}
			sees[4] = (float) (foodX - headX) / NUMCOL;
			sees[5] = (float) (foodY - headY) / NUMROW;
//			sees[6] = (float) headX / NUMCOL;
//			sees[7] = (float) headY / NUMROW;
//			sees[8] = (float) (NUMCOL - headX) / NUMCOL;
//			sees[9] = (float) (NUMROW - headY) / NUMROW;
			float[] ans = nn.run(sees);
			float max = ans[0];
			int direction = 0;
			
			for(int i = 0; i < ans.length; i ++) {
				if(ans[i] > max) {
					direction = i;
				}
			}
			turn(direction);
			if(aliveBonus < 0.5f * (score + 1)) {
				aliveBonus += 0.05f;
			}
		}
//		if(window != null) {
//			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
//		}
		return score + aliveBonus;
	}
	
	public static void main(String[] args) {
		window = new JFrame();
		window.setSize(NUMCOL * ICONSIZE, NUMROW * ICONSIZE);
		window.setVisible(true);
		window.setFocusable(true);
		window.setLayout(new GridLayout(NUMROW, NUMCOL));
		window.addKeyListener(new UserInput());
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ArrayList<Score> scores = new ArrayList<Score>();
		labels = new JLabel[NUMROW][NUMCOL];
		
		for(int i = 0; i < NUMROW; i ++) {
			for(int j = 0; j < NUMCOL; j ++) {
				labels[i][j] = new JLabel();
				labels[i][j].setSize(ICONSIZE, ICONSIZE);
				labels[i][j].setBorder(new LineBorder(Color.BLACK));
				window.add(labels[i][j]);
				labels[i][j].setIcon(blank);
			}
		}
		int genSize = 50;
		
		for(int i = 0; i < genSize; i ++) {
			scores.add(new Score(new Network(6, 4, 3, 6), 0));
		}
		
		int generation = 1;
		float highestScore = 0;
		while(true) {
			long start = System.currentTimeMillis();
			if(generation % 1 == 0) {
				makeWindow = true;
			}else {
				makeWindow = false;
			}
			long seed = rng.nextLong();
			float totalScore = 0;
			for(Score s : scores) {
				rng.setSeed(seed);
				float temp = aiPlay(s.nn);
				highestScore = Math.max(temp, highestScore);
				s.score = temp;
				totalScore += temp;
			}
			
			Collections.sort(scores);
			
			int removedCount = 0;
			for(int i = genSize - 1; i >= 0; i --) {
				if(Math.random() > (-1.0 / (genSize << 1) * (i << 1) + 1) && removedCount < genSize >> 1) {
					scores.remove(i);
					removedCount ++;
				}
			}
			for(int i = 0; i < removedCount; i ++) {
				scores.add(new Score(Utilities.crossover(scores.get(i).nn, scores.get(i + 1).nn, 0.5).mutate((float) (generation % 3) / 100), 0));
			}
			long end = System.currentTimeMillis();
			System.out.printf("Generation %d \nBest score: \t%.2f\nRecord: \t%.2f\nAverage: \t%.2f\n", generation, scores.get(0).score, highestScore, totalScore / genSize);
			System.out.println("Time take: " + (double) (end - start) / 1000 + " sec.");
			generation ++;
		}
	}
	
	static class Score implements Comparable<Score>{
		Network nn;
		float score;
		
		Score(Network nn, int score){
			this.nn = nn;
			this.score = score;
		}
		
		public void print() {
			System.out.print(score + ", ");
		}
		
		@Override
		public int compareTo(Score o) {
			if(o.score > this.score) {
				return 1;
			}else if (o.score < this.score) {
				return -1;
			}else {
				return 0;
			}
		}
	}
	
	static class Pair{
		int x, y;
		Pair(int x, int y){
			this.x = x;
			this.y = y;
		}
	}
}
