import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;


public class Othello extends JPanel {
	static final int width = 700;
	static final int height = 700;
	static JFrame frame;
	ClassLoader cl;

	//状態管理
	String status;

	//タイトル画面
	JButton solo, multi;

	//一人プレイ選択画面
	JRadioButton cpuLv1, cpuLv2, cpuLv3, colorBlack, colorWhite;
	ButtonGroup cpuGroup, colorGroup;
	JButton start;

	//二人プレイ選択画面
	JRadioButton colorBlackMulti, colorWhiteMulti;
	ButtonGroup colorGroupMulti;
	JButton startMulti;
	ServerSocket sSocket;
	Socket socket;
	PrintWriter pw;
	BufferedReader br;


	//ゲーム画面
	static final Color othelloBoard = new Color(10, 160, 50);
	static final Color othelloBlack = new Color(20, 20, 20);
	static final Color othelloWhite = new Color(245, 245, 245);
	Board board;
	char myColor;
	CPU cpu; //一人プレイ用
	char enemyColor; //二人プレイ用
	JButton update;
	Timer timer;
	boolean myTurn = false;

	//ゲーム終了画面
	JButton back;


	public static void main(String[] args) {
		frame = new JFrame("Othello");
		frame.getContentPane().add(new Othello());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
		frame.setVisible(true);
	}


	Othello()  { //状態、ボタン、タイマーの初期化
		cl = this.getClass().getClassLoader();

		//状態管理
		status = "title";

		//タイトル画面
		setLayout(null);
		ImageIcon ic1 = new ImageIcon(cl.getResource("solo.png"));
		solo = new JButton("一人で遊ぶ",ic1);
		solo.setBounds(250,400,200,50);
		add(solo);
		solo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				status = "select";
				repaint();
			}
		});
		ImageIcon ic2 = new ImageIcon(cl.getResource("multi.png"));
		multi = new JButton("二人で遊ぶ",ic2);
		multi.setBounds(250,500,200,50);
		add(multi);
		multi.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				try {
					sSocket = new ServerSocket(1234);
					socket = sSocket.accept();
					OutputStream out = socket.getOutputStream();
					pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
					InputStream in = socket.getInputStream();
					br = new BufferedReader(new InputStreamReader(in));
				} catch(IOException e2) {
					e2.printStackTrace();
				}
				status = "selectMulti";
				repaint();
			}
		});

		//一人プレイ選択画面
		cpuLv1 = new JRadioButton("Lv.1", true);
		cpuLv2 = new JRadioButton("Lv.2");
		cpuLv3 = new JRadioButton("Lv.3");
		cpuLv1.setForeground(Color.white);
		cpuLv2.setForeground(Color.white);
		cpuLv3.setForeground(Color.white);
		cpuLv1.setOpaque(false);
		cpuLv2.setOpaque(false);
		cpuLv3.setOpaque(false);
		cpuLv1.setFont(new Font(Font.SERIF, Font.BOLD, 20));
		cpuLv2.setFont(new Font(Font.SERIF, Font.BOLD, 20));
		cpuLv3.setFont(new Font(Font.SERIF, Font.BOLD, 20));
		cpuLv1.setBounds(245,200,63,30);
		cpuLv2.setBounds(315,200,63,30);
		cpuLv3.setBounds(385,200,63,30);
		add(cpuLv1);
		add(cpuLv2);
		add(cpuLv3);
		cpuGroup = new ButtonGroup();
		cpuGroup.add(cpuLv1);
		cpuGroup.add(cpuLv2);
		cpuGroup.add(cpuLv3);
		colorBlack = new JRadioButton("先手(黒)", true);
		colorWhite = new JRadioButton("後手(白)");
		colorBlack.setForeground(Color.white);
		colorWhite.setForeground(Color.white);
		colorBlack.setOpaque(false);
		colorWhite.setOpaque(false);
		colorBlack.setFont(new Font(Font.SERIF, Font.BOLD, 20));
		colorWhite.setFont(new Font(Font.SERIF, Font.BOLD, 20));
		colorBlack.setBounds(235,380,105,30);
		colorWhite.setBounds(355,380,105,30);
		add(colorBlack);
		add(colorWhite);
		colorGroup = new ButtonGroup();
		colorGroup.add(colorBlack);
		colorGroup.add(colorWhite);
		start = new JButton("Start");
		start.setBounds(290,500,100,40);
		add(start);
		start.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				board = new Board();
				int level;
				boolean isBlack;
				if (cpuLv1.isSelected())
					level = 1;
				else if (cpuLv2.isSelected())
					level = 2;
				else
					level = 3;
				isBlack = colorBlack.isSelected();
				if (isBlack) {
					myColor = 'b';
					cpu = new CPU(level, 'w');
					myTurn = true;
				} else {
					myColor = 'w';
					cpu = new CPU(level, 'b');
					myTurn = false;
					timer.start();
				}
				status = "game";
				repaint();
			}
		});

		//二人プレイ選択画面
		colorBlackMulti = new JRadioButton("先手(黒)", true);
		colorWhiteMulti = new JRadioButton("後手(白)");
		colorBlackMulti.setForeground(Color.white);
		colorWhiteMulti.setForeground(Color.white);
		colorBlackMulti.setOpaque(false);
		colorWhiteMulti.setOpaque(false);
		colorBlackMulti.setFont(new Font(Font.SERIF, Font.BOLD, 20));
		colorWhiteMulti.setFont(new Font(Font.SERIF, Font.BOLD, 20));
		colorBlackMulti.setBounds(235,310,105,30);
		colorWhiteMulti.setBounds(355,310,105,30);
		add(colorBlackMulti);
		add(colorWhiteMulti);
		colorGroup = new ButtonGroup();
		colorGroup.add(colorBlackMulti);
		colorGroup.add(colorWhiteMulti);
		startMulti = new JButton("Start");
		startMulti.setBounds(290,420,100,40);
		add(startMulti);
		startMulti.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				board = new Board();
				boolean isBlack = colorBlackMulti.isSelected();
				String request;
				if (isBlack) {
					myColor = 'b';
					enemyColor = 'w';
					request = "w";
					myTurn = true;
				} else {
					myColor = 'w';
					enemyColor = 'b';
					request = "b";
					myTurn = false;
					timer.start();
				}
				pw.println(request);
				pw.flush();
				status = "gameMulti";
				repaint();
			}
		});

		//ゲーム画面
		update = new JButton();
		add(update);
		UpdateListener ul = new UpdateListener();
		update.addActionListener(ul);
		timer = new Timer(1000, ul);

		//ゲーム終了画面
		back = new JButton("タイトルに戻る");
		back.setBounds(250,400,200,50);
		add(back);
		back.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				status = "title";
				repaint();
			}
		});

		addMouseListener(new MouseMotionCheck());
		addMouseMotionListener(new MouseMotionCheck());
	}


	class UpdateListener implements ActionListener {
		public void actionPerformed (ActionEvent e) {
			timer.stop();
			if (status == "game") {
				//自分が置けるようになる、またはどちらも置けなくなるまで繰り返す
				int passCount = 0;
				while (true) {
					//相手が置けるかどうか判定する
					if (board.existCanPlace(cpu.cpuColor)) {
						passCount = 0;
						cpu.select();
					} else
						passCount++;
					//自分が置けるかどうか判定する
					if (board.existCanPlace(myColor)) {
						passCount = 0;
						break;
					} else
						passCount++;
					//パスが2回以上連続したらゲーム終了
					if (passCount >= 2)
						break;
				}
				if (passCount >= 2) {
					if (myColor == 'w') {
						if (board.countWhite() > board.countBlack())
							status = "win";
						else if (board.countWhite() < board.countBlack())
							status = "lose";
						else
							status = "draw";
					} else {
						if (board.countBlack() > board.countWhite())
							status = "win";
						else if (board.countBlack() < board.countWhite())
							status = "lose";
						else
							status = "draw";
					}
				}
			} else if (status == "gameMulti") {
				while (true) {
					try {
						//相手の出力を受け取る
						String response = br.readLine();
						if (response.equals("pass")) {
							if (!board.existCanPlace(myColor)) { //相手が"pass"+自分が置けない -> "end"を送って終了
								String request = "end";
								pw.println(request);
								pw.flush();
								if (myColor == 'w') {
									if (board.countWhite() > board.countBlack())
										status = "win";
									else if (board.countWhite() < board.countBlack())
										status = "lose";
									else
										status = "draw";
								} else {
									if (board.countBlack() > board.countWhite())
										status = "win";
									else if (board.countBlack() < board.countWhite())
										status = "lose";
									else
										status = "draw";
								}
								sSocket.close();
								socket.close();
								pw.close();
								br.close();
							}
							break;
						} else if (response.equals("end")) { //相手が"end" -> 終了
							if (myColor == 'w') {
								if (board.countWhite() > board.countBlack())
									status = "win";
								else if (board.countWhite() < board.countBlack())
									status = "lose";
								else
									status = "draw";
							} else {
								if (board.countBlack() > board.countWhite())
									status = "win";
								else if (board.countBlack() < board.countWhite())
									status = "lose";
								else
									status = "draw";
							}
							sSocket.close();
							socket.close();
							pw.close();
							br.close();
							break;
						} else { //相手が置いた -> 自分の盤面にも反映させる
							String[] res = response.split(",", 0);
							int x2 = Integer.parseInt(res[0]);
							int y2 = Integer.parseInt(res[1]);
							board.placePiece(x2, y2, enemyColor);
						}
						//自分が置けるかどうか判定する
						if (board.existCanPlace(myColor)) //置ける場合はループから抜ける
							break;
						else { //置けない場合は"pass"を送る
							String request = "pass";
							pw.println(request);
							pw.flush();
						}
					} catch(IOException e2) {
						e2.printStackTrace();
					}
				}
			}
			repaint();
			myTurn = true;
		}
	}


	public void paintComponent(Graphics g) { //画面出力を管理する
		super.paintComponent(g);
		if (status == "title") {
			Graphics2D g2 = (Graphics2D) g;
			try {
				BufferedImage image = ImageIO.read(cl.getResource("Othello_title.jpg"));
				g2.drawImage(image, null, 0, 0);
				BufferedImage text = ImageIO.read(cl.getResource("Othello_text.png"));
				g2.drawImage(text, null, 150, 150);
			} catch (IOException e) {
				e.printStackTrace();
			}
			solo.setVisible(true);
			multi.setVisible(true);
			cpuLv1.setVisible(false);
			cpuLv2.setVisible(false);
			cpuLv3.setVisible(false);
			colorBlack.setVisible(false);
			colorWhite.setVisible(false);
			start.setVisible(false);
			colorBlackMulti.setVisible(false);
			colorWhiteMulti.setVisible(false);
			startMulti.setVisible(false);
			update.setVisible(false);
			back.setVisible(false);
		} else if(status == "select") {
			Graphics2D g2 = (Graphics2D) g;
			try {
				BufferedImage image = ImageIO.read(cl.getResource("othello_select.png"));
				g2.drawImage(image, null, 0, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			g.setColor(Color.white);
			g.setFont(new Font(Font.SERIF, Font.BOLD, 30));
			g.drawString("CPUの強さ", 270, 170);
			g.drawString("手番", 310, 350);
			solo.setVisible(false);
			multi.setVisible(false);
			cpuLv1.setVisible(true);
			cpuLv2.setVisible(true);
			cpuLv3.setVisible(true);
			colorBlack.setVisible(true);
			colorWhite.setVisible(true);
			start.setVisible(true);
		} else if(status == "selectMulti") {
			Graphics2D g2 = (Graphics2D) g;
			try {
				BufferedImage image = ImageIO.read(cl.getResource("othello_select.png"));
				g2.drawImage(image, null, 0, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			g.setColor(Color.white);
			g.setFont(new Font(Font.SERIF, Font.BOLD, 30));
			g.drawString("手番", 310, 280);
			solo.setVisible(false);
			multi.setVisible(false);
			colorBlackMulti.setVisible(true);
			colorWhiteMulti.setVisible(true);
			startMulti.setVisible(true);
		} else if (status == "game" || status == "gameMulti" || status == "win" || status == "lose" || status == "draw") {
			if (status == "game") {
				cpuLv1.setVisible(false);
				cpuLv2.setVisible(false);
				cpuLv3.setVisible(false);
				colorBlack.setVisible(false);
				colorWhite.setVisible(false);
				start.setVisible(false);
			} else if (status == "gameMulti") {
				colorBlackMulti.setVisible(false);
				colorWhiteMulti.setVisible(false);
				startMulti.setVisible(false);
			}
			setBackground(othelloBoard);
			g.setColor(Color.black);
			//横線
			for (int y = 10; y <= 650; y+=80)
				g.drawLine(20, y, 660, y);
			//縦線
			for (int x = 20; x <= 660; x+=80)
				g.drawLine(x, 10, x, 650);
			//マス
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					char c = board.get(i, j);
					if (c == '-')
						continue;
					int x = 20+80*j+10;
					int y = 10+80*i+10;
					if (c == 'b')
						g.setColor(othelloBlack);
					else
						g.setColor(othelloWhite);
					g.fillOval(x, y, 60, 60);
				}
			}
			if ((status == "game" || status == "gameMulti") && !myTurn) {
				try {
					BufferedImage img = ImageIO.read(cl.getResource("loading.png"));
					g.drawImage(img, 140, 170, this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (status == "win" || status == "lose" || status == "draw") {
				try {
					if (status == "win") {
						BufferedImage img = ImageIO.read(cl.getResource("win.png"));
						g.drawImage(img, 90, 100, this);
					} else if (status == "lose") {
						BufferedImage img = ImageIO.read(cl.getResource("lose.png"));
						g.drawImage(img, 20, 100, this);
					} else {
						BufferedImage img = ImageIO.read(cl.getResource("draw.png"));
						g.drawImage(img, 90, 50, this);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				back.setVisible(true);
			}
		}
	}


	class MouseMotionCheck extends MouseInputAdapter {
		public void mouseReleased (MouseEvent e) {
			if (!myTurn)
				return;
			if (status == "game" || status == "gameMulti") {
				//自分が置く
				int x = e.getY()-10;
				int y = e.getX()-20;
				if (x >= 0)
					x /= 80;
				if (y >= 0)
					y /= 80;
				if (x < 0 || x >= 8 || y < 0 || y >= 8 || !board.canPlace(x, y, myColor))
					return;
				board.placePiece(x, y, myColor);
				repaint();
				if (status == "gameMulti") {
					String request = String.valueOf(x)+","+String.valueOf(y);
					pw.println(request);
					pw.flush();
				}
				myTurn = false;
				timer.start();
			}
		}
	}


	class CPU {
		final private int[][] weightLv2 = {
				{120, -20, 20, 5, 5, 20, -20, 120},
				{-20, -40, -5, -5, -5, -5, -40, -20},
				{20, -5, 15, 3, 3, 15, -5, 20},
				{5, -5, 3, 3, 3, 3, -5, 5},
				{5, -5, 3, 3, 3, 3, -5, 5},
				{20, -5, 15, 3, 3, 15, -5, 20},
				{-20, -40, -5, -5, -5, -5, -40, -20},
				{120, -20, 20, 5, 5, 20, -20, 120}
		};
		final private int[][] weightLv3 = {
				{30, -12, 0, -1, -1, 0, -12, 30},
				{-12, -15, -3, -3, -3, -3, -15, -12},
				{0, -3, 0, -1, -1, 0, -3, 0},
				{-1, -3, -1, -1, -1, -1, -3, -1},
				{-1, -3, -1, -1, -1, -1, -3, -1},
				{0, -3, 0, -1, -1, 0, -3, 0},
				{-12, -15, -3, -3, -3, -3, -15, -12},
				{30, -12, 0, -1, -1, 0, -12, 30}
		};
		private int level;
		private char cpuColor;

		private class Pair {
			int x, y;
			Pair(int a, int b) {
				x = a;
				y = b;
			}
		}

		CPU (int l, char c) {
			level = l;
			cpuColor = c;
		}
		void select() {
			if (level == 1) {
				ArrayList<Pair> list = new ArrayList<Pair>();
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						if (board.canPlace(i, j, cpuColor)) {
							Pair p = new Pair(i, j);
							list.add(p);
						}
					}
				}
				Random rand = new Random();
				int idx = rand.nextInt(list.size());
				board.placePiece(list.get(idx).x, list.get(idx).y, cpuColor);
			} else if (level == 2) {
				ArrayList<Pair> list = new ArrayList<Pair>();
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						if (board.canPlace(i, j, cpuColor)) {
							Pair p = new Pair(i, j);
							list.add(p);
						}
					}
				}
				int idx = 0, mx = -10000;
				for (int i = 0; i < list.size(); i++) {
					int x = list.get(i).x;
					int y = list.get(i).y;
					if (weightLv2[x][y] > mx) {
						mx = weightLv2[x][y];
						idx = i;
					}
				}
				board.placePiece(list.get(idx).x, list.get(idx).y, cpuColor);
			} else if (level == 3) {
				ArrayList<Pair> list = new ArrayList<Pair>();
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						if (board.canPlace(i, j, cpuColor)) {
							Pair p = new Pair(i, j);
							list.add(p);
						}
					}
				}
				int idx = 0, mx = -10000;
				for (int i = 0; i < list.size(); i++) {
					int x = list.get(i).x;
					int y = list.get(i).y;
					if (weightLv3[x][y] > mx) {
						mx = weightLv3[x][y];
						idx = i;
					}
				}
				board.placePiece(list.get(idx).x, list.get(idx).y, cpuColor);
			}
		}
	}
}


class Board {
	private char[][] grid;

	//コンストラクタ起動時にメモリの確保と初期化をする
	Board() {
		grid = new char[8][8];
		init();
	}
	//盤面の初期化
	void init() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (i == 3 && j == 3 || i == 4 && j == 4)
					grid[i][j] = 'w';
				else if (i == 3 && j == 4 || i == 4 && j == 3)
					grid[i][j] = 'b';
				else
					grid[i][j] = '-';
			}
		}
	}
	//置けるマスが存在するかどうか判定する
	boolean existCanPlace(char color) {
		boolean flag = false;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (canPlace(i, j, color))
					flag = true;
			}
		}
		return flag;
	}
	//指定したマスに置けるかどうか判定する
	boolean canPlace(int x, int y, char color) {
		if (grid[x][y] != '-')
			return false;
		boolean flag = false;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0)
					continue;
				int i = x+dx, j = y+dy;
				if (i >= 0 && i < 8 && j >= 0 && j < 8 && grid[i][j] != '-' && grid[i][j] != color) {
					i += dx;
					j += dy;
					while (i >= 0 && i < 8 && j >= 0 && j < 8 && grid[i][j] != '-') {
						if (grid[i][j] == color)
							flag = true;
						i += dx;
						j += dy;
					}
				}
			}
		}
		return flag;
	}
	//指定したマスに置く
	void placePiece(int x, int y, char color) {
		grid[x][y] = color;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0)
					continue;
				int i = x+dx, j = y+dy;
				if (i >= 0 && i < 8 && j >= 0 && j < 8 && grid[i][j] != '-' && grid[i][j] != color) {
					i += dx;
					j += dy;
					while (i >= 0 && i < 8 && j >= 0 && j < 8 && grid[i][j] != '-') {
						if (grid[i][j] == color) {
							while (i != x || j != y) {
								grid[i][j] = color;
								i -= dx;
								j -= dy;
							}
							break;
						}
						i += dx;
						j += dy;
					}
				}
			}
		}
	}
	//指定したマスの状態を返す
	char get(int x, int y) {
		return grid[x][y];
	}
	//黒の個数を数える
	int countBlack() {
		int num = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (grid[i][j] == 'b')
					num++;
			}
		}
		return num;
	}
	//白の個数を数える
	int countWhite() {
		int num = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (grid[i][j] == 'w')
					num++;
			}
		}
		return num;
	}
}