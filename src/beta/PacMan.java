package beta;

import javax.swing.JFrame;


@SuppressWarnings("serial")
public class PacMan extends JFrame
{

  public PacMan()
  {
    add(new Board());
    setTitle("Connor's Beta Pacman");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(780, 820);
    setLocationRelativeTo(null);
    setVisible(true);
  }

  public static void main(String[] args) {
      new PacMan();
  }
}