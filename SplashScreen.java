import java.awt.*; // import the java.awt library
import hsa.Console; // import the hsa.Console library
/* Peter Lin
2019-01-17
Mrs. Krasteva
This class is a threaded animation. It is used in as an introduction to the crossword program.

Global Variable Dictionary
Name    Type        Description
c       Console     This is the canvas on which the animation is drawn on.
*/

public class SplashScreen implements Runnable // creating the SplashScreen class
{
    Console c;
    
    /* Method run is called when this Thread is started.
    Local Variable Dictionary
    Name    Type        Description
    x       double      This variable stores the x-coordinate of the word "Cross" as it moves across the screen.
    y       double      This variable stores the y-coordinate of the word "Cross" as it bounces up and down.
    vy      double      This variable stores the y-component of the velocity of the word "Cross" as it bounces up and down.
    
    Loops
    The for loop provides the coordinates for the words and a set of rules to update them. The x-velocity never changes, but the y-velocity always increases
    (due to gravity).
    Start Condition: x = -150, y = 200, vy = 0 (when you drop an object it starts with a velocity of 0)
    End Condition: When the words reach the centre of the screen.
    Increment: Increase x by 1, decrease y by vy (because the way Java coordinates are)
    
    Conditional
    The if statement checks to see if the words hit the "ground" by looking at its position and velocity. If it has hit the ground, the reverse the direction
    and make it bounce lower than last time.
    The else statement simply decrements vy due to gravity.
    
    Block Segments
    Try-catch is used because Thread.sleep() can throw InterruptedException.
    */
    public void run ()
    {
	c.setFont (new Font ("Comic Sans MS", Font.PLAIN, 50));
	for (double x = -150, y = 200, vy = 0 ; x < 351 ; x++, y -= vy)
	{
	    c.setColor (Color.white);
	    c.fillRect ((int) x - 1, (int) (y - 60 + vy), 130, 65);
	    c.fillRect (830 - (int) x + 1, (int) (y - 60 + vy), 115, 65);
	    c.setColor (new Color ((int) (127 * Math.sin (x / 4) + 128), (int) (127 * Math.sin (x / 4 + Math.PI * 2 / 3) + 128), (int) (127 * Math.sin (x / 4 + Math.PI * 4 / 3) + 128)));
	    c.drawString ("Cross", (int) x, (int) y);
	    c.drawString ("word", 830 - (int) x, (int) y);
	    try
	    {
		Thread.sleep (25);
	    }
	    catch (InterruptedException e)
	    {
	    }
	    if (y >= 400 && vy < 0)
	    {
		vy *= -0.887; // reverse direction when the word hits the ground, and bounce lower than last time
	    }
	    else
	    {
		vy -= 0.5; // increase velocity due to gravity
	    }
	}
	try
	{
	    Thread.sleep (2000);
	}
	catch (InterruptedException e)
	{
	}
    }


    public SplashScreen (Console con)
    {
	c = con;
    }
} // end of Splashscreen class

