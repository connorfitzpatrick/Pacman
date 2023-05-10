package beta;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import sf.Sound;
import sf.SoundFactory;


@SuppressWarnings("serial")
public class Board extends JPanel implements ActionListener {

    Dimension d;
    Font smallfont = new Font("Helvetica", Font.BOLD, 28);

    FontMetrics fmsmall, fmlarge;
    Image ii;
    Color dotcolor = new Color(192, 192, 0);
    Color mazecolor;

    boolean ingame = false;
    boolean dying = false;

    final int blocksize = 48;
    final int nrofblocks = 15;
    final int scrsize = nrofblocks * blocksize;
    final int pacanimdelay = 2;
    final int pacmananimcount = 4;
    final int maxghosts = 12;
    final int pacmanspeed = 6;

    int pacanimcount = pacanimdelay;
    int pacanimdir = 1;
    int pacmananimpos = 0;
    int nrofghosts = 6;
    int pacsleft, score;
    int highScore;
    int deathcounter;
    int levelsPlayed;
    int[] dx, dy;
    int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;
    
    String highScorePlayer;
    long powerUpStart;
    boolean poweredUp;
    
    // Images
    Image ghost, scaredGhost;
    Image fruit;
    Image cherry, strawberry, orange, apple, ship;
    Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
    Image pacman3up, pacman3down, pacman3left, pacman3right;
    Image pacman4up, pacman4down, pacman4left, pacman4right;
    
    // Sounds
    final static String DIR = "src/res/";
    final static String pacmanVoice = DIR + "pacman_voice.wav";
    final static String powerVoice = DIR + "pacman_powerup.wav";
    final static String pacmanMunch1 = DIR + "pacman_munch_1.wav";
    final static String pacmanMunch2 = DIR + "pacman_munch_2.wav";
    final static String eatGhost = DIR + "pacman_eatghost.wav";
    final static String eatPowerup = DIR + "pacman_eatfruit.wav";
    final static String deathNoise = DIR + "pacman_death.wav";
    String lastPlayed;
    
    Sound pacmanSiren;
    Sound powerupSiren;

    int pacmanx, pacmany, pacmandx, pacmandy;
    int reqdx, reqdy, viewdx, viewdy;
    
    Levels levelList;
    int[] levels;
    int currentLevelNumber;
    int fruitLocation;
    int pellotLeft;
    int totalPellots;
    boolean flashing;
    
    Sound munchSound1 = SoundFactory.getInstance(pacmanMunch1);
	Sound munchSound2 = SoundFactory.getInstance(pacmanMunch2);
    
    short leveldata[];
    
    final int validspeeds[] = { 1, 2, 3, 4, 6, 8 };
    final int maxspeed = 6;

    int currentspeed = 3;
    short[] screendata;
    Timer timer;


    public Board() {

        GetImages();

        addKeyListener(new TAdapter());

        screendata = new short[nrofblocks * nrofblocks];
        setFocusable(true);

        d = new Dimension(800, 800);

        setBackground(Color.black);
        setDoubleBuffered(true);
        
        // get sounds and mute them
        pacmanSiren = SoundFactory.getInstance(pacmanVoice);
        pacmanSiren.setVolume(-80);
        SoundFactory.play(pacmanSiren, Integer.MAX_VALUE);
        
        powerupSiren = SoundFactory.getInstance(powerVoice);
        powerupSiren.setVolume(-80);
        SoundFactory.play(powerupSiren, Integer.MAX_VALUE);
        
        
        ghostx = new int[maxghosts];
        ghostdx = new int[maxghosts];
        ghosty = new int[maxghosts];
        ghostdy = new int[maxghosts];
        ghostspeed = new int[maxghosts];
        dx = new int[4];
        dy = new int[4];
         
        timer = new Timer(40, this);
        timer.start();
    }

    public void addNotify() {
        super.addNotify();
        GameInit();
    }


    public void DoAnim() {
        pacanimcount--;
        if (pacanimcount <= 0) {
            pacanimcount = pacanimdelay;
            pacmananimpos = pacmananimpos + pacanimdir;
            if (pacmananimpos == (pacmananimcount - 1) || pacmananimpos == 0)
                pacanimdir = -pacanimdir;
        }
    }


    public void PlayGame(Graphics2D g2d) {
        if (dying) {
            Death();
        } else {
            MovePacMan();
            DrawPacMan(g2d);
            moveGhosts(g2d);
            CheckMaze();
            if (poweredUp == true) {
            	pacmanSiren.setVolume(-80);
            	powerupSiren.setVolume(0);
            }
            else {
            	pacmanSiren.setVolume(0);
            	powerupSiren.setVolume(-80);
            }
        }
    }


    public void ShowIntroScreen(Graphics2D g2d) {

        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(100, scrsize / 2 - 60, scrsize - 200, 100);
        g2d.setColor(Color.white);
        g2d.drawRect(100, scrsize / 2 - 60, scrsize - 200, 100);

        String s = "PRESS S TO START OR Q TO QUIT.";
        Font small = new Font("Helvetica", Font.BOLD, 28);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2, scrsize / 2);
    }


    public void DrawScore(Graphics2D g) {
        int i;
        String s;

        g.setFont(smallfont);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, scrsize / 2 + 192, scrsize + 32);
        for (i = 0; i < pacsleft; i++) {
            g.drawImage(pacman3left, i * 56 + 16, scrsize + 2, this);
        }
    }
    
    public void DrawHighScore(Graphics2D g) {
    	String s;
    	
    	g.setFont(smallfont);
        g.setColor(new Color(96, 128, 255));
        s = "High Score: " + highScore;
        g.drawString(s, scrsize / 2 - 160, scrsize + 32);
        s = "Earned By: " + highScorePlayer;
        g.drawString(s, scrsize / 2 - 160, scrsize + 64);
    }

    public void DrawFruit(Graphics2D g2d) {
    	g2d.drawImage(fruit, scrsize + 10, 25, this);
    }

    public void CheckMaze() {
        short i = 0;
        boolean finished = true;

        while (i < nrofblocks * nrofblocks && finished) {
            if ((screendata[i] & 48) != 0)
                finished = false;
            i++;
        }

        if (finished) {
        	// Give extra points for finishing level
            score += 50;
            
            if (nrofghosts < maxghosts)
            	nrofghosts = 6 + currentLevelNumber;
                nrofghosts++;
            if (currentspeed < maxspeed)
                currentspeed++;
            currentLevelNumber++;
            LevelInit();
        }
    }

    public void Death() {
		Sound sound = SoundFactory.getInstance(deathNoise);
		sound.play();
        pacsleft--;
        if (pacsleft == 0) {
        	ingame = false;
        	if (score > highScore) {
        		highScorePlayer = JOptionPane.showInputDialog("Enter a 3 letter name");
        		if (highScorePlayer.length() > 2) {
        			highScorePlayer = highScorePlayer.substring(0, 3);
        		}
                ReadNamesScores.updateNamesScore(highScorePlayer, score);
        	}
        }
        LevelContinue();
    }


    public void moveGhosts(Graphics2D g2d) {
        short i;
        int pos;
        int count;

        for (i = 0; i < nrofghosts; i++) {
        	
        	// the two if statements below handle wraparound for ghosts
        	if (ghostx[i] < 0 - 48) {
            	ghostx[i] = 672;
            }
            if (ghostx[i] > 672) {
            	ghostx[i] = 0;
            }
            
            if (ghostx[i] % blocksize == 0 && ghosty[i] % blocksize == 0) {
                pos =
 ghostx[i] / blocksize + nrofblocks * (int)(ghosty[i] / blocksize);

                count = 0;
                if ((screendata[pos] & 1) == 0 && ghostdx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }
                if ((screendata[pos] & 2) == 0 && ghostdy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }
                if ((screendata[pos] & 4) == 0 && ghostdx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }
                if ((screendata[pos] & 8) == 0 && ghostdy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }
 
                if (count == 0) {
                    if ((screendata[pos] & 15) == 15) {
                        ghostdx[i] = 0;
                        ghostdy[i] = 0;
                    } else {
                        ghostdx[i] = -ghostdx[i];
                        ghostdy[i] = -ghostdy[i];
                    }
                } else {
                    count = (int)(Math.random() * count);
                    if (count > 3)
                        count = 3;
                    ghostdx[i] = dx[count];
                    ghostdy[i] = dy[count];
                }

            }
            
            ghostx[i] = ghostx[i] + (ghostdx[i] * ghostspeed[i]);
            ghosty[i] = ghosty[i] + (ghostdy[i] * ghostspeed[i]);
            //DrawGhost(g2d, ghostx[i] + 1, ghosty[i] + 1);

            // if collision with ghost...
            if (pacmanx > (ghostx[i] - 12) && pacmanx < (ghostx[i] + 12) &&
                pacmany > (ghosty[i] - 12) && pacmany < (ghosty[i] + 12) &&
                ingame) {
            	if (poweredUp == true) {
            		// eat ghost
            		Sound sound = SoundFactory.getInstance(eatGhost);
            		SoundFactory.play(sound);
            		
            		RemoveGhost(i);
            		
            	}
            	else {
            		dying = true;
            	}

            }
            DrawGhost(g2d, ghostx[i] + 1, ghosty[i] + 1);
        }
    }
    
    public void RemoveGhost(int i) {
    	
    	int tempGhostx = ghostx[nrofghosts - 1];
        int tempGhostdx = ghostdx[nrofghosts - 1];
        int tempghosty = ghosty[nrofghosts -1];
        int tempghostdy = ghostdy[nrofghosts -1];
        int tempghostspeed = ghostspeed[nrofghosts -1];
        
        ghostx[nrofghosts -1] = ghostx[i];
        ghostdx[nrofghosts -1] = ghostdx[i];
        ghosty[nrofghosts -1] = ghosty[i];
        ghostdy[nrofghosts -1] = ghostdy[i];
        ghostspeed[nrofghosts -1] = ghostspeed[i];
        
        
        ghostx[i] = tempGhostx;
        ghostdx[i] = tempGhostdx;
        ghosty[i] = tempghosty;
        ghostdy[i] = tempghostdy;
        ghostspeed[i] = tempghostspeed;
        
        nrofghosts--;
    }


    public void DrawGhost(Graphics2D g2d, int x, int y) {
    	if (poweredUp == false) {
    		g2d.drawImage(ghost, x, y, this);
    	}
    	else {
    		g2d.drawImage(scaredGhost, x, y, this);
    	}
    }


    public void MovePacMan() {
        int pos;
        short ch;
        
        // Handle wraparound. Pacman must reappear onscreen
        if (pacmanx < 0 - 48) {
        	pacmanx = 672;
        }
        if (pacmanx > 672) {
        	pacmanx = 0;
        }
        
        // if pacman changes direction
        if (reqdx == -pacmandx && reqdy == -pacmandy) {
        	pacmandx = reqdx;
            pacmandy = reqdy;
            viewdx = pacmandx;
            viewdy = pacmandy;
        }
        
        if (pacmanx % blocksize == 0 && pacmany % blocksize == 0) {
            pos =
 pacmanx / blocksize + nrofblocks * (int)(pacmany / blocksize);
            ch = screendata[pos];

            if ((ch & 16) != 0) {
                screendata[pos] = (short)(ch & 15);
                if (poweredUp == true) {
                	score += 3;
                }
                else {
                	score++;
                }
                if (pos == fruitLocation) {
                	score += 5;
                	Sound sound = SoundFactory.getInstance(eatPowerup);
                	SoundFactory.play(sound);
                }
                // if power up collected
                if ((pos == leveldata[225]) || (pos == leveldata[226]) || (pos == leveldata[227]) || (pos == leveldata[228])) {
                	Sound sound = SoundFactory.getInstance(eatPowerup);
                	SoundFactory.play(sound);
                	
                	powerUpStart = System.currentTimeMillis();
                	poweredUp = true;
                	flashing = false;
                }
                else {
                	
                	if (lastPlayed == "munch2") {
                    	SoundFactory.play(munchSound1);                    	
                		lastPlayed = "munch1";
                	}
                	else {
                    	SoundFactory.play(munchSound2);
                		lastPlayed = "munch2";
                	}
                }
            }
            
            
            if (reqdx != 0 || reqdy != 0) {
            	// prevents pacman from turning into wall to standing still
                if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0) ||
                      (reqdx == 1 && reqdy == 0 && (ch & 4) != 0) ||
                      (reqdx == 0 && reqdy == -1 && (ch & 2) != 0) ||
                      (reqdx == 0 && reqdy == 1 && (ch & 8) != 0))) {
                    pacmandx = reqdx;
                    pacmandy = reqdy;
                    viewdx = pacmandx;
                    viewdy = pacmandy;
                }
            }

            // Check for standstill
            if ((pacmandx == -1 && pacmandy == 0 && (ch & 1) != 0) ||
                (pacmandx == 1 && pacmandy == 0 && (ch & 4) != 0) ||
                (pacmandx == 0 && pacmandy == -1 && (ch & 2) != 0) ||
                (pacmandx == 0 && pacmandy == 1 && (ch & 8) != 0)) {
                pacmandx = 0;
                pacmandy = 0;
            }
            
        }
        
        
        // check power up time
        if (System.currentTimeMillis() - powerUpStart > 8000) {
        	poweredUp = false;
        	flashing = false;
        }
        
        if (System.currentTimeMillis() - powerUpStart > 6000) {
        	flashing = true;
        }
        if (System.currentTimeMillis() - powerUpStart > 6400) {
        	flashing = false;
        }
        if (System.currentTimeMillis() - powerUpStart > 6800) {
        	flashing = true;
        }
        if (System.currentTimeMillis() - powerUpStart > 7200) {
        	flashing = false;
        }
        if (System.currentTimeMillis() - powerUpStart > 7600) {
        	flashing = true;
        }
        if (System.currentTimeMillis() - powerUpStart > 7800) {
        	flashing = false;
        }
        if (System.currentTimeMillis() - powerUpStart > 7900) {
        	flashing = true;
        }
        
        
        pacmanx = pacmanx + pacmanspeed * pacmandx;
        pacmany = pacmany + pacmanspeed * pacmandy;
    }


    public void DrawPacMan(Graphics2D g2d) {
        if (viewdx == -1)
            DrawPacManLeft(g2d);
        else if (viewdx == 1)
            DrawPacManRight(g2d);
        else if (viewdy == -1)
            DrawPacManUp(g2d);
        else
            DrawPacManDown(g2d);
    }

    public void DrawPacManUp(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(pacman2up, pacmanx + 1, pacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(pacman3up, pacmanx + 1, pacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(pacman4up, pacmanx + 1, pacmany + 1, this);
            break;
        default:
            g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
            break;
        }
    }


    public void DrawPacManDown(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(pacman2down, pacmanx + 1, pacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(pacman3down, pacmanx + 1, pacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(pacman4down, pacmanx + 1, pacmany + 1, this);
            break;
        default:
            g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
            break;
        }
    }


    public void DrawPacManLeft(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(pacman2left, pacmanx + 1, pacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(pacman3left, pacmanx + 1, pacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(pacman4left, pacmanx + 1, pacmany + 1, this);
            break;
        default:
            g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
            break;
        }
    }


    public void DrawPacManRight(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(pacman2right, pacmanx + 1, pacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(pacman3right, pacmanx + 1, pacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(pacman4right, pacmanx + 1, pacmany + 1, this);
            break;
        default:
            g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
            break;
        }
    }


    public void DrawMaze(Graphics2D g2d) {
        short i = 0;
        int x, y;

        for (y = 0; y < scrsize; y += blocksize) {
            for (x = 0; x < scrsize; x += blocksize) {
                g2d.setColor(mazecolor);
                g2d.setStroke(new BasicStroke(4));

                if ((screendata[i] & 1) != 0) // draws left
                {
                    g2d.drawLine(x, y, x, y + blocksize - 1);
                }
                if ((screendata[i] & 2) != 0) // draws top
                {
                    g2d.drawLine(x, y, x + blocksize - 1, y);
                }
                if ((screendata[i] & 4) != 0) // draws right
                {
                    g2d.drawLine(x + blocksize - 1, y, x + blocksize - 1,
                                 y + blocksize - 1);
                }
                if ((screendata[i] & 8) != 0) // draws bottom
                {
                    g2d.drawLine(x, y + blocksize - 1, x + blocksize - 1,
                                 y + blocksize - 1);
                }
                if ((screendata[i] & 16) != 0) // draws point/pellets
                {
                	if (poweredUp == true) {
                		if (flashing) {
                			g2d.setColor(new Color(255, 255, 255));
                		}
                		else {
                			g2d.setColor(new Color(255, 87, 51));
                		}
                	}
                	else {
                    	g2d.setColor(dotcolor);
                	}
                	                	
                	
                	if ((i == leveldata[225]) || (i == leveldata[226]) || (i == leveldata[227]) || (i == leveldata[228])) {
                		g2d.fillRect(x + 19, y + 19, 10, 10);
                	}
                	else {
                		// only draw fruit if in game
                		if (i == fruitLocation && ingame == true) {
                			g2d.drawImage(fruit, x, y, null);
                    	}
                		else {
                			g2d.fillRect(x + 22, y + 22, 4, 4);
                		}
                	}
                }
                i++;
            }
        }
    }
    
    // calculate a random location on the map to generate a fruit
    public int calculateFruitLocation() {
    	int count = 0;
    	for (int i = 0; i < leveldata.length; i++) {
    		if (leveldata[i] == 16) {
    			count++;
    		}
    	}
    	Random random = new Random();
    	int fruitIndex = random.nextInt(count) + 1;

    	int c = 0;
    	for (int j = 0; j < leveldata.length; j++) {
    		if (leveldata[j] == 16) {
    			c++;
    		}
    		if (c == fruitIndex) {
    			return j;
    		}
    	}
    	return 0;
    	
    }

    public void GameInit() {
        pacsleft = 3;
        score = 0;
        String[] scoreInfo = ReadNamesScores.readNameScore();
        highScore = Integer.parseInt(scoreInfo[1]);
        highScorePlayer = scoreInfo[0];
        levelList = new Levels();
        levels = levelList.getLevelList();
        currentLevelNumber = 0;
        lastPlayed = "munch2";
        flashing = false;
        LevelInit();
        nrofghosts = 6;
        currentspeed = 3;
        poweredUp = false;

    }


    public void LevelInit() {
        lastPlayed = "munch2";
    	flashing = false;
        poweredUp = false;
		leveldata = levelList.getNthLevelFromList(currentLevelNumber);
    	
    	if (currentLevelNumber % 5 == 0) {
    		mazecolor = new Color(25, 25, 166);
    		fruit = cherry;
    	}
    	else if (currentLevelNumber % 5 == 1) {
    		mazecolor = new Color(5, 100, 5);
    		fruit = orange;
    	}
    	else if (currentLevelNumber % 5 == 2) {
    		mazecolor = new Color(5, 5, 100);
    		fruit = strawberry;
    	}
    	else if (currentLevelNumber % 5 == 3) {
    		mazecolor = new Color(64, 224, 208);
    		fruit = apple;
    	}
    	else if (currentLevelNumber % 5 == 4) {
    		mazecolor = new Color(100, 5, 5);
    		fruit = ship;
    	}
    	
    	fruitLocation = calculateFruitLocation();
    	
    	int i;
        for (i = 0; i < nrofblocks * nrofblocks; i++)
            screendata[i] = leveldata[i];

        LevelContinue();
    }


    public void LevelContinue() {
        short i;
        int dx = 1;
        int random;

        for (i = 0; i < nrofghosts; i++) {
            ghosty[i] = 4 * blocksize;
            ghostx[i] = 4 * blocksize;
            ghostdy[i] = 0;
            ghostdx[i] = dx;
            dx = -dx;
            random = (int)(Math.random() * (currentspeed + 1));
            if (random > currentspeed)
                random = currentspeed;
            ghostspeed[i] = validspeeds[random];
        }

        pacmanx = 7 * blocksize;
        pacmany = 11 * blocksize;
        pacmandx = 0;
        pacmandy = 0;
        reqdx = 0;
        reqdy = 0;
        viewdx = -1;
        viewdy = 0;
        dying = false;
    }

    public void GetImages()
    {
      cherry = new ImageIcon(Board.class.getResource("/res/cherry.png")).getImage();
      strawberry = new ImageIcon(Board.class.getResource("/res/strawberry.png")).getImage();
      orange = new ImageIcon(Board.class.getResource("/res/orange.png")).getImage();
      apple = new ImageIcon(Board.class.getResource("/res/apple.png")).getImage();
      ship = new ImageIcon(Board.class.getResource("/res/ship.png")).getImage();
      ghost = new ImageIcon(Board.class.getResource("/res/ghost.png")).getImage();
      scaredGhost = new ImageIcon(Board.class.getResource("/res/scared_ghost.png")).getImage();
      pacman1 = new ImageIcon(Board.class.getResource("/res/pacman.png")).getImage();
      pacman2up = new ImageIcon(Board.class.getResource("/res/up1.png")).getImage();
      pacman3up = new ImageIcon(Board.class.getResource("/res/up2.png")).getImage();
      pacman4up = new ImageIcon(Board.class.getResource("/res/up3.png")).getImage();
      pacman2down = new ImageIcon(Board.class.getResource("/res/down1.png")).getImage();
      pacman3down = new ImageIcon(Board.class.getResource("/res/down2.png")).getImage(); 
      pacman4down = new ImageIcon(Board.class.getResource("/res/down3.png")).getImage();
      pacman2left = new ImageIcon(Board.class.getResource("/res/left1.png")).getImage();
      pacman3left = new ImageIcon(Board.class.getResource("/res/left2.png")).getImage();
      pacman4left = new ImageIcon(Board.class.getResource("/res/left3.png")).getImage();
      pacman2right = new ImageIcon(Board.class.getResource("/res/right1.png")).getImage();
      pacman3right = new ImageIcon(Board.class.getResource("/res/right2.png")).getImage();
      pacman4right = new ImageIcon(Board.class.getResource("/res/right3.png")).getImage();

    }

    public void paint(Graphics g)
    {
      super.paint(g);

      Graphics2D g2d = (Graphics2D) g;

      g2d.setColor(Color.black);
      g2d.fillRect(0, 0, d.width, d.height);

      DrawMaze(g2d);
      DrawScore(g2d);
      DrawFruit(g2d);
      DrawHighScore(g2d);
      DoAnim();
      if (ingame) {
        PlayGame(g2d);
      }
      else {
    	pacmanSiren.setVolume(-80);
      	powerupSiren.setVolume(-80);
        ShowIntroScreen(g2d);
      }

      g.drawImage(ii, 5, 5, this);
      Toolkit.getDefaultToolkit().sync();
      g.dispose();
    }

    class TAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e) {

          int key = e.getKeyCode();

          if (ingame)
          {
            if (key == KeyEvent.VK_LEFT)
            {
              reqdx=-1;
              reqdy=0;
            }
            else if (key == KeyEvent.VK_RIGHT)
            {
              reqdx=1;
              reqdy=0;
            }
            else if (key == KeyEvent.VK_UP)
            {
              reqdx=0;
              reqdy=-1;
            }
            else if (key == KeyEvent.VK_DOWN)
            {
              reqdx=0;
              reqdy=1;
            }
            else if (key == KeyEvent.VK_ESCAPE && timer.isRunning())
            {
              ingame=false;
            }
            else if (key == 'q' || key == 'Q' ) 
            {
              ingame = false;
              pacmanSiren.setVolume(-80);
              powerupSiren.setVolume(-80);
            }
            else if (key == KeyEvent.VK_PAUSE) {
                if (timer.isRunning())
                    timer.stop();
                else timer.start();
            }
          }
          else
          {
        	pacmanSiren.setVolume(-80);
            powerupSiren.setVolume(-80);
            if (key == 's' || key == 'S')
            {
              ingame=true;
              GameInit();
            }
            if (key == 'q' || key == 'Q') 
            {
              System.exit(0);
            }
          }
      }

          public void keyReleased(KeyEvent e) {
              int key = e.getKeyCode();

              if (key == Event.LEFT || key == Event.RIGHT || 
                 key == Event.UP ||  key == Event.DOWN)
              {
                reqdx=0;
                reqdy=0;
              }
          }
      }

    public void actionPerformed(ActionEvent e) {
        repaint();  
    }
}
