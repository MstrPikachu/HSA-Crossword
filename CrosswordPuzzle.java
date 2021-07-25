import java.awt.*; // import the java.awt library
import hsa.*; // import the hsa library
import javax.swing.JOptionPane; // import JOptionPane for error messages
import java.io.*; // import java.io to read and write to files
/* Top Comments
Peter Lin
2018-12-16
Mrs. Krasteva
This program makes a timed and untimed crossword puzzle for the user to solve. It also includes a high score menu so the user can view other's scores.
The first screen is the introduction. The user will see an animation.
The second screen is the main menu. The main menu allows the user to choose what they want to do.
If they want to read the instructions, the instructions screen explains the program to the user.
If they want to select a level, the level select screen will ask if they want a timed or untimed level. Then they will go to the game screen, where the user inputs their guesses for words.
When they finish the game, it will go to the win screen so they can enter their name, and then back to the main menu.
The high scores screen displays the leaderboard.
The clear high scores doesn't have a screen; it just clears the high scores.
The goodbye screen thanks the user for using the program.

Global Variable Dictionary
Name            Type        Description
c               Console     This variable represent's the program's console. It is the window that reads user input and prints text.
choice          char        This variable is the user's choice to read the instructions, exit, view the high scores, or play the crossword.
scores          int[]       This variable stores top 10 times of the fastest crossword solves.
names           String[]    This variable stores the top 10 names of the players of the fastest crossword solves.
hints           String[] [] This variable stores the list of crossword hints for every crossword. Hints for across and down are distinguished because hints for down start with a period.
crossword       char[] [] []This variable stores the 2D character array representation of every crossword.
guess           char[] [] []This variable stores the 2D character array representation for the user's cumulative guesses for the respective crossword.
			    It has extra information stored (spaces and special characters) to accurately be compared against the real crossword to check if it
			    has been solved.
xCoord         int[] []     This variable stores the x-coordinate (1-indexed coordinate) of the special character representing the number of the word (eg. 3 across).
			    It is 1-indexed so the default value of 0 is distinguished as illegal.
yCoord         int[] []     This variable stores the y-coordinate (1-indexed coordinate) of the special character representing the number of the word (eg. 3 across).
			    It is 1-indexed so the default value of 0 is distinguished as illegal.
crosswordNumber int         This is the current crossword number. There are currently 3 different crosswords as of 2019-01-17. This variable distinguishes between
			    the 3 different crosswords.
timed           boolean     This flag stores whether the current game is timed or untimed.
completed       boolean     This flag stores whether the crossword is completed or not after a check.
time            int         This variable stores the elapsed time (in 100ths of a second) after starting a timed level.
drawing         boolean     This flag stores whether a thread should be drawing the timer or incrementing the time variable periodically (every 10 milliseconds).
cheat           boolean     This flag stores whether cheat mode is on or off.

Citations                   Description
1. Synchronization          This helps keep the timer and other drawing from interfering each other from different threads.
			    Source: https://docs.oracle.com/javase/tutorial/essential/concurrency/locksync.html
2. "this" keyword           The "this" keyword allows reference to the object calling a method (an instance of the CrosswordPuzzle class). It is useful when initializing a new Thread because
			    the Thread constructor takes a class as a parameter.
			    Source: https://docs.oracle.com/javase/tutorial/java/javaOO/thiskey.html

*/

public class CrosswordPuzzle implements Runnable // creating the CrosswordPuzzle class
{
    // Global Variable Declaration
    Console c;
    char choice = ' ';
    int[] scores = new int [10];
    String[] names = new String [10];
    String[] winTitles = new String [10];
    String[] titles = {"ICS", "Physics", "Math"};
    String[] [] hints;
    char[] [] [] crossword;
    char[] [] [] guess;
    int[] [] xCoord;
    int[] [] yCoord;
    int crosswordNumber;
    boolean timed = false;
    boolean completed = false;
    int time = 0;
    boolean drawing = false;
    boolean cheat = false;

    /* Method run is called when a new Thread is started.
    When a Thread is first started, the run method will start incrementing this.time. Instead of drawing the timer inside the loop, it is drawn outside the loop in another Thread to prevent the
    timer from being too slow.
    Before the timer starts counting up, a flag is set so the next Thread to start will draw to the board. Then a new Thread is started and it draws to the board. However, the timer is not
    constantly being erased. It erases part by part to minimize flashing.

    Local Variable Dictionary
    Name        Type        Description
    decimal     int         This variable stores this.time in 100ths of a second on the timer. It starts at -1 by default so the timer is updated by default.
    seconds     int         This variable stores this.time in seconds on the timer. It also starts at -1 by default.
    minutes     int         This variable stores this.time in minutes on the timer. It also starts at -1 by default.
    hours       int         This variable stores this.time in hours on the timer. It also starts at -1 by default.

    Loops
    There are 2 loops.
    The second loop is simple. Its job is to increase this.time by 1 every 10 milliseconds (or a 100th of a second).
    Start Condition: this.time is reset to 0.
    Stop Condition: When this.completed becomes true. This means that it stops counting when the crossword is completed.
    Increment: Every 10 milliseconds this.time is incrememented by 1.
    The first loop is a little more complex. Its job is to draw the timer on the board.
    Start Condition: decimal, seconds, minutes, and hours are set to -1. -1 is chosen because it is smaller than 0 (the start condition for this.time).
    End Condition: When this.completed becomes true. The timer will stop being drawn when the crossword is completed.
    Increment: The increment is too complicated to fit in the for loop syntax. This will be explained with the conditional statements.

    Conditional Statements
    There are two main conditional statements that define the bulk of this method. As already explained, if statement 1 runs only when the drawing flag has been set. This ensures there is
    another Thread incrementing this.time for the timer.
    The else statement runs when the drawing flag is false. This indicates the the current Thread needs to start incrementing this.time in a loop, and it will start a new Thread to start
    drawing the timer to the screen.

    If 1.1 runs when the hours on the timer need to be updated. It updates the hours variable, erases the old hours, then draws the new one back.
    If 1.2 runs when the minutes on the timer need to be updated. It updates the minutes variable, erases the old minutes, then draws the new one back.
    If 1.3 runs when the seconds on the timer need to be updated. It updates the seconds variable, erases the old seconds, then draws the new one back.
    If 1.4 rusn when the decimals on the timer need to be updated. It updates the decimal variable, erases the old decimals, then draws the new one back.

    Segment Blocks
    The synchronized statement is used to prevent memory inconsistency. See Citation 1 in the Top Comments for more details.
    The try-catch blocks are used because Thread.sleep() can throw InterruptedException.
    */
    public void run ()
    {
	if (drawing) // if 1
	{
	    synchronized (c)
	    {
		c.setFont (new Font ("Arial", Font.PLAIN, 20));
		c.drawString (":", 890, 50); // timers have colons
		c.drawString (":", 857, 50);
		c.drawString (":", 824, 50);
	    }
	    for (int decimal = -1, seconds = -1, minutes = -1, hours = -1 ; !completed ;) // Loop 1
	    {
		synchronized (c)
		{
		    c.setFont (new Font ("Arial", Font.PLAIN, 20));
		    if (hours != time / 36000) // if 1.1
		    {
			hours = time / 360000;
			c.setColor (Color.white);
			c.fillRect (800, 32, 25, 20); // erase
			c.setColor (Color.black);
			c.drawString (((hours % 24) < 10 ? "0": // ternary statement adds a leading 0 if necessary
			"") + hours % 24, 800, 50);
		    }
		    if (minutes != time / 6000) // if 1.2
		    {
			minutes = time / 6000;
			c.setColor (Color.white);
			c.fillRect (833, 32, 25, 20);
			c.setColor (Color.black);
			c.drawString (((minutes % 60) < 10 ? "0": // ternary statement adds a leading 0 if necessary
			"") + minutes % 60, 833, 50);
		    }
		    if (seconds != time / 100) // if 1.3
		    {
			seconds = time / 100;
			c.setColor (Color.white);
			c.fillRect (866, 32, 25, 20);
			c.setColor (Color.black);
			c.drawString (((seconds % 60) < 10 ? "0": // ternary statement adds a leading 0 if necessary
			"") + seconds % 60, 866, 50);
		    }
		    if (decimal != time) // if 1.4
		    {
			decimal = time;
			c.setColor (Color.white);
			c.fillRect (900, 32, 25, 20);
			c.setColor (Color.black);
			c.drawString ((time % 100 < 10 ? "0": // ternary statement adds a leading 0 if necessary
			"") + decimal % 100, 900, 50);
		    }
		}
	    }
	}
	else // else 2
	{
	    drawing = true;
	    (new Thread (this)).start ();
	    for (time = 0 ; !completed ; time++) // loop 2
	    {
		try
		{
		    Thread.sleep (10);
		}
		catch (InterruptedException e)
		{
		}
	    }
	    drawing = false;
	}
    }


    /* Method title prints the title, draws graphics, and clears the screen

    Local Variable Dictionary
    Name        Type        Description
    title       String      The title to be written on the screen.
    */
    private void title (String title)
    {
	c.clear ();
	c.print ("", 61 - title.length () / 2);
	synchronized (c)
	{
	    c.println (title + "\n");
	    c.setColor (Color.yellow); // draws yellow stars
	    c.fillStar (11, 1, 28, 28);
	    c.fillStar (931, 1, 28, 28);
	    c.fillStar (11, 691, 28, 28);
	    c.fillStar (931, 691, 28, 28);
	    c.setColor (Color.black); // Changes text color back to black

	    if (timed) // if 1
	    {
		c.setFont (new Font ("Arial", Font.PLAIN, 20));
		c.setColor (Color.white); // erase
		c.fillRect (800, 32, 25, 20);
		c.fillRect (833, 32, 25, 20);
		c.fillRect (866, 32, 25, 20);
		c.fillRect (900, 32, 25, 20);
		c.setColor (Color.black); // draws the timer
		c.drawString (((time / 360000 % 24) < 10 ? "0": // the following ternary statements add a leading 0 if necessary
		"") + (time / 360000) % 24, 800, 50);
		c.drawString (((time / 6000 % 60) < 10 ? "0":
		"") + (time / 6000) % 60, 833, 50);
		c.drawString (((time / 100 % 60) < 10 ? "0":
		"") + (time / 100) % 60, 866, 50);
		c.drawString ((time % 100 < 10 ? "0":
		"") + time % 100, 900, 50);
		c.drawString (":", 890, 50); // timers have colons
		c.drawString (":", 857, 50);
		c.drawString (":", 824, 50);
	    }
	}
    }


    // default title should be "Crossword Puzzle"
    private void title ()
    {
	title ("Crossword Puzzle");
    }


    /* Method pauseProgram prints a given message and waits for the user to respond. The key pressed will be returned from the method.

    Local Variable Dictionary
    Name        Type        Description
    message     String      This is the message to be printed.
    */
    private char pauseProgram (String message)
    {
	c.println ();
	synchronized (c)
	{
	    c.print (message);
	}
	return c.getChar ();
    }


    /* Method isGreater checks if the name-score pair given is larger or smaller than another name-score pair in this.names and this.scores at the index given
    Local Variable Dictionary;
    Name        Type        Description
    index       int         This is the index of the name-score pair.
    name        String      The name to compare.
    score       int         The score to compare.
    */
    private boolean isGreater (int index, String name, int score)
    { // returns true if the score is greater than another for given indices i and j, and if they are the same, returns based on the name of the scoreholder
	return scores [index] == score ? names [index].compareToIgnoreCase (name) >= 0: // if the scores are the same, compare the names based on alphabetical order
	scores [index] > score;
    }


    /* Method checkCrossword checks whether the user's crossword is correctly completed or not.
    Local Variable Dictionary
    Name        Type        Description
    i           int         In loop 1, this variable is the x-coordinate of the crossword element being checked. It goes from 0 to the crossword width.
    j           int         In loop 1.1, this variable is the y-coordinate of the crossword element being checked. It goes from 0 to the crossword height.

    Conditional Statements
    If 1.1.1 checks if the current crossword element is not equal to the answer. If it is not, then it returns false.

    Loops
    Loop 1 goes from the left of the crossword to the right of the crossword.
    Start Condition: i = 0
    End Condition: When the loop finished going from left to right.
    Increment: i is increased by 1.
    Loop 2 goes from the top of the crossword to the bottom of the crossword.
    Start Condition: j = 0
    End Condition: When the loop finished going from top to bottom.
    Increment: j is increased by 1.
    */
    private boolean checkCrossword ()
    {
	for (int i = 0 ; i < crossword [crosswordNumber].length ; i++) // loop 1
	{
	    for (int j = 0 ; j < crossword [crosswordNumber] [i].length ; j++) // loop 1.1
	    {
		if (crossword [crosswordNumber] [i] [j] != guess [crosswordNumber] [i] [j]) // if 1.1.1
		{
		    return false;
		}
	    }
	}
	return true; // if the method makes it here then none of the elements are wrong -> the crossword is correct
    }


    /* Method display draws the current crossword to the screen.
    Local Variable Dictionary
    Name        Type        Description
    i           int         This variable stores the x-coordinate of the crossword in loop 1, and it is the element number in loop 2.
    j           int         This variable stores the y-coordinate of the crossword in loop 1.1.
    across      int         This variable stores how far down from the title "Across" the hint should be drawn.
    down        int         This variable stores how far down from the title "Down" the hint should be drawn.

    Citations               Description
    1. Bitwise OR           This is taken advantage of during the creation of the crossword storage format. A character is assigned to each word in the crossword. It represents whether it is
			    down or across, and what number it is (eg. 5 across). If it is down, then the character will be the ASCII of 48 (the ASCII of the number 0) + number. If it is across,
			    then the character will be the ASCII of 32 + number. Unfortunately it is 1-indexed because the ASCII of 32 is the space character, which was already used as a blank
			    space. What Bitwise OR does is that it adds 16 (10 000) if the 5th bit isn't set. If it is set, then the character will be from 48 - 64. Once that is done, subtracting
			    '0' or 48 will convert it back to the 0-indexed number (eg. 5 across). Add 1 to make it 1-indexed for the user to read.
			    Source: https://en.wikipedia.org/wiki/Bitwise_operation

    Loops
    The method must loop through the whole crossword to check which words must be printed to the screen.
    Loop 1 goes from the left of the crossword to the right of the crossword.
    Start Condition: i = 0
    End Condition: When the loop finishes going from left to right.
    Increment: Increase i by 1
    Loop 2 goes from the top to the bottom of the crossword.
    Start Condition: j = 0
    End Condition: When the loop finishes going from top to bottom.
    Increment: Increase j by 1.
    Loop 3 goes through all the hints.
    Start Condition: i = 0
    End Condition: When all the hints have been printed.
    Increment: Increase i by 1.

    Conditional Statements
    The conditional statements 1.1.x print part of the crossword on the screen. Different variables need to represent different parts of the screen.
    If 1.1.1 draws a crossword box if the crossword element is from A-Z (a letter). It must draw a box so the crossword can have shape.
    If 1.1.2 draws a letter (in a box because the box has already been drawn) that the user guessed if the guessed crossword element is from A-Z (a letter).
    If 1.1.3 draws a number beside a section of the crossword denoting which number the word is (every word in a crossword is assigned a number) if the crossword element is a special character
    (see Citation 1 in these method comments.
    If 2.1 prints the hint under the "Down" title if the first character of the hint is a period (meaning that the hint is for a "down" word).
    Else 2.2 prints the hint under the "Across" title, because if the hint is not down, the it must be across.

    Segment Blocks
    The synchronized statement is used to prevent memory inconsistency. See Citation 1 in the Top Comments for more details.
    */
    public void display ()
    {
	title (titles [crosswordNumber]);
	synchronized (c)
	{
	    for (int i = 0 ; i < crossword [crosswordNumber].length ; i++) // loop 1
	    {
		for (int j = 0 ; j < crossword [crosswordNumber] [i].length ; j++) // loop 1.1
		{
		    if (crossword [crosswordNumber] [i] [j] >= 'A' && crossword [crosswordNumber] [i] [j] <= 'Z') // if 1.1.1
		    {
			c.drawRect (475 + (i - crossword [crosswordNumber].length / 2) * 25, 120 + j * 20, 25, 20); // draw a box for the crossword
		    }
		    if (guess [crosswordNumber] [i] [j] >= 'A' && guess [crosswordNumber] [i] [j] <= 'Z') // if 1.1.2
		    {
			c.setFont (new Font ("Arial", Font.PLAIN, 20));
			c.drawString ("" + (char) (guess [crosswordNumber] [i] [j]), 480 + (i - guess [crosswordNumber].length / 2) * 25, 138 + j * 20); // draw a letter in the box
		    }
		    else if (crossword [crosswordNumber] [i] [j] > 32 && crossword [crosswordNumber] [i] [j] <= 64) // else if 1.1.3
		    {
			c.setFont (new Font ("Arial", Font.PLAIN, 12)); // draw the word number
			c.drawString ("" + ((crossword [crosswordNumber] [i] [j] - 1 | 16) - '0' + 1), 483 + (i - crossword [crosswordNumber].length / 2) * 25, 138 + j * 20);
		    }
		}
	    }
	    c.setFont (new Font ("Arial", Font.PLAIN, 15));
	    c.drawString ("Across", 100, 490); // draws title "Across" and "Down"
	    c.drawString ("Down", 500, 500);
	    for (int i = 0, across = 0, down = 0 ; i < hints [crosswordNumber].length ; i++) // loop 2
	    {
		if (hints [crosswordNumber] [i].charAt (0) == '.') // if 2.1
		{ // the hint is down
		    c.drawString (i + 1 + ". " + hints [crosswordNumber] [i].substring (1), 500, 490 + ++down * 20);
		}
		else // else 2.2
		{ // the hint is across
		    c.drawString (i + 1 + ". " + hints [crosswordNumber] [i], 100, 500 + ++across * 20);
		}
	    }
	}
    }


    /* Method splashScreen shows a bouncy animation of two words connecting in the middle
    Local Variable Dictionary
    Name            Type        Description
    splashScreen    Thread      This is a new Thread made to run an animation, and then wait for it to stop.

    Segment Blocks
    The try-catch blocks are used because Thread.join() can throw InterruptedException.
    */
    public void splashScreen ()  // this method uses a threaded animation and outputs the introduction of this program for the user to read
    {
	title (); // clear the screen
	c.print ("", 27);
	c.println ("This program will use a crossword puzzle to improve your vocabulary!");
	Thread splashScreen = new Thread (new SplashScreen (c));
	splashScreen.start (); // start the animation
	try
	{
	    splashScreen.join (); // wait for the animation to stop before continuing
	}
	catch (InterruptedException e)
	{
	}
    }


    /* Method mainMenu allows the user to choose what option they want: read the instructions, select a level, view the high scores, clear the high scores, or exit the program.
    Loops
    The do-while loop keeps repeating as long the choice is invalid. It is a do-while rather than while because the choice must be asked at least once.

    Conditional Statements
    The if statement checks whether the choice is invalid. If it is, then an error message will be produced.

    About the expression repeating inside the if and the while statement: the compiler should have optimized it using Common Subexpression Elimination.
    Source: https://stackoverflow.com/questions/8949388/will-the-compiler-optimize-repeated-math-computations
    */
    public void mainMenu ()
    {
	do
	{
	    title ();
	    c.print("", 49);
	    c.println ("Please choose an option:");
	    c.print("", 
	    c.println ("1. Instructions");
	    c.println ("2. Level Select");
	    c.println ("3. High Scores");
	    c.println ("4. Clear High Scores");
	    c.println ("5. Exit");
	    choice = pauseProgram ("Press 1, 2, 3, or 5: ");
	    if (choice < '1' || choice > '5') // if the choice is invalid
	    {
		JOptionPane.showMessageDialog (null, "Sorry, you must enter: 1, 2, 3, 4, or 5. Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
	while (choice < '1' || choice > '5'); // repeat while the choice is invalid
    }


    // Method instructions will print instructions that explains the main menu options to the user.
    public void instructions ()
    {
	title ();
	c.println ("The main menu of this program has 5 options. The first one is the instructions option, which goes here.");
	c.println ("The second main menu option is the level selection option.\nIt will lead to a screen asking to choose between a timed and an untimed level.");
	c.println ("The difference is that only timed results will be added to the leaderboard.\nYou will also see a timer counting how long it took you to solve the crossword.");
	c.println ("The third option is to view the high scores.\nThe high scores leaderboard simply lists the top 10 fastest crossword solves.");
	c.println ("The fourth option is to clear the leaderboard. After clearing the leaderboard, the high scores menu will be emtpy.");
	c.println ("The last option is to exit. This will quit the program.");
	c.println ("\nHow to play the game:\n");
	c.println ("To solve the crossword, you first have 3 choices. You can fill the word going across, down, or you can check your crossword.");
	c.println ("If you choose to go either across or down, you will have to enter the row or column number of your word.\nThis can be found be looking to the start of the word.");
	c.println ("There will be a number beside the boxes indicating the number of the row or column.");
	c.println ("Finally, you enter the word that you wish to put into the crossword.\nBe sure that your word is not longer than the allowed space.");
	pauseProgram ("Press any key to return to the main menu...");
    }


    /* Method levelSelect lets the user choose whether they want to play timed or untimed.

    Loops
    The do-while loop asks for user input as long as the input is invalid. It is a do-while loop rather than a while loop because there must be input at least once.

    Conditional Statements
    If statement 1 checks to see if the input is invalid, and it if is, an error message is produced.
    If statement 2 checks to see if the level is timed, and if it is, then start a new Thread. This will start the timer.

    About the repeated expression in the while and if 1, go to the method comments for mainMenu() for more details.
    */
    public void levelSelect ()
    {
	crosswordNumber = (int) (crossword.length * Math.random ()); // randomly choose a crossword number
	do
	{
	    title ();
	    c.println ("Please choose a level:");
	    c.println ("1. Timed");
	    c.println ("2. Untimed");
	    choice = pauseProgram ("");
	    if (choice < '1' || choice > '2') // if 1
	    {
		JOptionPane.showMessageDialog (null, "Sorry, you must enter: 1, 2, 3, or 4. Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
	while (choice < '1' || choice > '2');
	timed = choice == '1';
	if (timed) // if 2
	{
	    (new Thread (this)).start ();
	}
    }


    /* Method winScreen congratulates the user on finishing the crossword. It also sorts their score and name in the leaderboard.
    Local Variable Dictionary
    Name        Type        Description
    i           int         In loop 1.1, i goes from 9 to 0 to sort the high scores. It checks whether the current score is lower than the input score and continues until it is sorted.
			    In loop 3, i goes from the left to the right of the guess crossword to reset it to the original.
    j           int         In loop 3.1, j goes from the top to the bottom of the guess crossword to reset it to the original.
    name        String      This is the user's name that will be sorted into the leaderboard.

    Loops
    Loop 1 is sorting the leaderboard. It works like this: as long as the input score is lower than the one above it, then swap their places. A temp variable is not needed for the swap because
    the input score does not change.
    Start Condition: i = 9
    End Condition: Stop when the input is no longer lower than the score above it, or when the whole array has been looped through (whichever comes first).
    Increment: Decrease i by 1 (because it goes from 9 to 0)

    Conditional Statements
    If 1 checks if the game was timed, and if it is, then sorts the score (only timed games go on the leaderboard).
    Else 2 allows the user to return to the main menu without prompting for the user's name.
    If 3.1.1 checks if the current crossword element is a letter, and if it is, resets it to the space character.
    */
    public void winScreen ()
    {
	title ();
	c.print ("Congratulations! You finished the crossword!"); // Not using println so the timer isn't erased
	c.setCursor (5, 1); // setCursor is needed to move the cursor down
	if (timed) // if 1
	{
	    c.print ("Enter your name: ");
	    String name = c.readLine ();
	    for (int i = 9 ; i >= 0 && isGreater (i, name, time) ; i--) // loop 1.1
	    { // sort the leaderboard
		if (i < 9) // if 1.1.1
		{ // do not run if i is 9, to prevent index out of bounds
		    scores [i + 1] = scores [i]; // move the current score down a spot
		    names [i + 1] = names [i]; // move the name as well
		    winTitles [i + 1] = winTitles [i]; // move the title as well
		}
		scores [i] = time; // copy the current score to the current element
		names [i] = name; // copy the name as well
		winTitles [i] = titles [crosswordNumber];
	    }
	}
	else // else 2
	{
	    pauseProgram ("Press any key to return to the main menu...");
	}
	completed = timed = false;
	for (int i = 0 ; i < crossword [crosswordNumber].length ; i++) // loop 3
	{ // resets the crossword by setting all letters to their original (space character)
	    for (int j = 0 ; j < crossword [crosswordNumber] [i].length ; j++) // loop 3.1
	    {
		if (crossword [crosswordNumber] [i] [j] >= 'A') // if 3.1.1
		{
		    guess [crosswordNumber] [i] [j] = ' ';
		}
	    }
	}
    }


    /* method askData will prompt the user for an input. They will input down, across, check, or cheat.

    There is a processing section for when the input is down and across. They are similar enough that only down has been documented.

    Local Variable Dictionary
    Name            Type        Description
    input           String      This is the input that the user enters in the program.
    word            String      This is the input word that the user enters in the program.
    i               int         This is the loop counter. It goes from a side of the crossword to the other, checking all the letters in between.

    Citations                   Description
    1. Regular Expressions      Using regular expressions (or regex) makes it easy to check whether a word doesn't contains letters. This is useful for checking input. It is also is more readable
				than a loop (especially with this many).
				Sources: https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
					 https://docs.oracle.com/javase/7/docs/api/java/lang/String.html

    Loops
    Loop 1.1 is the main input loop when asking for a word. It stops when the user inputs a valid input for all inputs.
    Loop 1.1.1.2.1 prints out the answer to the crossword. It only goes when cheat mode is on.
    Start Condition: i starts at the coordinate of the word
    End Condition: When the element at index i is no longer a letter
    Increment: Increase i by 1.
    Loop 1.1.1.3 is for guessing a word. It keep repeating as long as the word is longer than there is space on the board.
    Loop 1.1.1.3.2 checks to see if the word is too long. It starts at the beginning of the word, and scans through the answer crossword to see how long the answer is. If it is too long, then
    the user will be forced to choose another word.
    Start Condition: i = 0
    End Condition: When the loop reaches the end of the word.
    Increment: Increase i by 1.
    Loop 1.1.1.3.3 copies the guess into the guess crossword.
    Start Condition: i = 0
    End Condition: When the loop reaches the end of the word.
    Increment: Increase i by 1.

    Conditional Statements
    If 1 checks to see if the input is "down" and will run the code for getting more input. There is another else if checking to see if the input is "across" and is very similar.
    If 1.1.1.1 checks to see if the column/row number is valid or not. If it isn't, then an error message will be produced.
    If 1.1.1.2 checks to see if cheat mode is on. If it is, then the answer for a word will be printed.
    If 1.1.1.3.1 checks to see if the word doesn't only contain letters. If so, then an error message will be produced.
    Else if 3 checks to see if the input is "check" and will check the crossword if it is.
    Else 4 produces an error message because the input is invalid.

    Block Segments
    Try-catch 1.1.1 is used to ensure that the program doesn't crash if the user inputs an invalid number for the column/row. Instead, NumberFormatException is caught and an error message is
    produced.
    */
    public void askData ()
    {
	title ();
	display ();
	c.setCursor (4, 1); // some coordinates
	synchronized (c)
	{
	    c.print ("Choose down or across, or choose to check your crossword: ");
	}
	String input = c.readLine ().toLowerCase ();
	if (input.equals ("down")) // if 1
	{
	    int column = -1;
	    while (true) // loop 1.1
	    {
		c.setCursor (5, 1);
		synchronized (c)
		{
		    c.print ("Enter a column number: ");
		}
		try // try 1.1.1
		{
		    column = Integer.parseInt (c.readLine ()) + '0'; // adds '0' because the indices for xCoord are in ASCII
		    if (column >= xCoord [crosswordNumber].length || xCoord [crosswordNumber] [column] == 0) // if 1.1.1.1
		    {
			JOptionPane.showMessageDialog (null, "Sorry, that is not a valid column number. Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
			c.setCursor (5, 1);
			c.println ();
			continue;
		    }
		    if (cheat) // if 1.1.1.2
		    {
			c.setCursor (6, 29);
			for (int i = yCoord [crosswordNumber] [column] ; crossword [crosswordNumber] [xCoord [crosswordNumber] [column] - 1] [i] >= 'A' ; i++) // loop 1.1.1.2.1
			{
			    synchronized (c)
			    {
				c.print (crossword [crosswordNumber] [xCoord [crosswordNumber] [column] - 1] [i]);
			    } // print answer letter by letter
			}
		    }
		    outer:
		    do // loop 1.1.1.3
		    {
			c.setCursor (6, 1);
			synchronized (c)
			{
			    c.print ("Enter your guess as a word: ");
			}
			String word = c.readLine ().toUpperCase ();
			if (!word.matches ("[A-Z]+")) // if 1.1.1.3.1
			{
			    JOptionPane.showMessageDialog (null, "Sorry, your word must only have letters. Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
			    c.setCursor (6, 1);
			    c.println ();
			    continue outer;
			}
			for (int i = 0 ; i < word.length () ; i++) // loop 1.1.1.3.2
			{
			    if (crossword [crosswordNumber] [xCoord [crosswordNumber] [column] - 1] [yCoord [crosswordNumber] [column] + i] < 'A') // if 1.1.1.3.2.1
			    {
				JOptionPane.showMessageDialog (null, "Sorry, your word is too long. Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
				c.setCursor (6, 1);
				c.println ();
				continue outer;
			    }
			}
			for (int i = 0 ; i < word.length () ; i++) // loop 1.1.1.3.3
			{
			    guess [crosswordNumber] [xCoord [crosswordNumber] [column] - 1] [yCoord [crosswordNumber] [column] + i] = word.charAt (i);
			}
			break;
		    }
		    while (true);
		}
		catch (NumberFormatException e)
		{
		    JOptionPane.showMessageDialog (null, "Sorry, you must enter a positive number. Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
		    c.setCursor (5, 1);
		    c.println ();
		}
		break;
	    }
	}
	else if (input.equals ("across"))
	{
	    int row = -1;
	    while (true)
	    {
		c.setCursor (5, 1);
		synchronized (c)
		{
		    c.print ("Enter a row number: ");
		}
		try
		{
		    row = Integer.parseInt (c.readLine ()) + ' ';
		    if (row >= xCoord [crosswordNumber].length || xCoord [crosswordNumber] [row] == 0)
		    {
			JOptionPane.showMessageDialog (null, "Sorry, that is not a valid row number. Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
			c.setCursor (5, 1);
			c.println ();
			continue;
		    }
		    if (cheat)
		    {
			c.setCursor (6, 29);
			for (int i = xCoord [crosswordNumber] [row] ; crossword [crosswordNumber] [i] [yCoord [crosswordNumber] [row] - 1] >= 'A' ; i++)
			{
			    c.print (crossword [crosswordNumber] [i] [yCoord [crosswordNumber] [row] - 1]);
			}
			c.println ();
		    }
		    outer:
		    do
		    {
			c.setCursor (6, 1);
			synchronized (c)
			{
			    c.print ("Enter your guess as a word: ");
			}
			String word = c.readLine ().toUpperCase ();
			if (!word.matches ("[A-Z]+")) // if 1.1.1.3.1
			{
			    JOptionPane.showMessageDialog (null, "Sorry, your word must only have letters. Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
			    c.setCursor (6, 1);
			    c.println ();
			    continue outer;
			}
			for (int i = 0 ; i < word.length () ; i++)
			{
			    if (crossword [crosswordNumber] [xCoord [crosswordNumber] [row] + i] [yCoord [crosswordNumber] [row] - 1] < 'A')
			    {
				JOptionPane.showMessageDialog (null, "Sorry, your word is too long. Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
				c.setCursor (6, 1);
				c.println ();
				continue outer;
			    }
			}
			for (int i = 0 ; i < word.length () ; i++)
			{
			    guess [crosswordNumber] [xCoord [crosswordNumber] [row] + i] [yCoord [crosswordNumber] [row] - 1] = word.charAt (i);
			}
			break;
		    }
		    while (true);
		}
		catch (NumberFormatException e)
		{
		    JOptionPane.showMessageDialog (null, "Sorry, you must enter a positive number. Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
		    c.setCursor (5, 1);
		    c.println ();
		}
		break;
	    }
	}
	else if (input.equals ("check")) // else if 3
	{
	    completed = checkCrossword ();
	    if (!completed) // if 3.1
	    {
		c.setCursor (4, 1);
		synchronized (c)
		{
		    c.println ("Your crossword is wrong. Try again!");
		}
		pauseProgram ("Press any key to continue...");
	    }
	}
	else // else 4
	{
	    JOptionPane.showMessageDialog (null, "Sorry, you must enter: \"down,\" \"across,\" or \"check.\" Please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
	}
    }


    /* Method highScores displays the top 10 high scores, players, and what level they played.
    Local Variable Dictionary
    Name            Type        Description
    i               int         This is the loop counter that goes through all the high scores.

    Loops
    The for loop goes through all the high scores and prints them out, or stop when the maximum score (Integer.MAX_VALUE is considered invalid) is reached.
    Start Condition: i = 0
    End Condition: When all the highscores have been printed
    Increment: Increase i by 1.

    Conditional Statements
    If 1 checks if the first score is invalid, and then says there are no high scores if it is.
    Else 2 prints the scores otherwise.
    */
    public void highScores ()
    {
	title ();
	if (scores [0] == Integer.MAX_VALUE) // if 1
	{
	    c.println ("There are currently no high scores.");
	}
	else // else 2
	{
	    for (int i = 0 ; i < 10 && scores [i] < Integer.MAX_VALUE ; i++)
	    {
		c.setCursor (i + 4, 30);
		c.print (names [i]);
		c.setCursor (i + 4, 60);
		c.print (winTitles [i]);
		c.setCursor (i + 4, 90);
		c.print (((scores [i] / 360000) % 24 < 10 ? "0":
		"") + (scores [i]) / 360000 % 24);     // prints the hours
		c.print (((scores [i] / 6000) % 60 < 10 ? ":0":
		":") + (scores [i]) / 6000 % 60);    // prints the minutes
		c.print (((scores [i] / 100) % 60 < 10 ? ":0":
		":") + (scores [i]) / 100 % 60);   // prints the seconds
		c.print ((scores [i] % 100 < 10 ? ".0":
		".") + scores [i] % 100);  // prints the decimals
	    }
	}
	pauseProgram ("Press any key to to return to the main menu...");
    }


    /* Method clearHighScores clears the high scores by setting them to the maximum value. The maximum value is considered an invalid high score. The maximum was chosen to be invalid so when
    more scores are sorted they go above the maximum.
    Local Variable Dictionary
    Name            Type        Description
    i               int         This is the loop counter that goes through all the high scores.

    Loops
    The for loop goes through all the high scores and sets them as invalid.
    Start Condition: i = 0
    End Condition: When all the highscores have been set
    Increment: Increase i by 1.
    */
    public void clearHighScores ()
    {
	for (int i = 0 ; i < 10 ; i++)
	{
	    scores [i] = Integer.MAX_VALUE;
	}
    }


    /* Method goodbye thanks the user for using the program, closes the window, and saves high scores.
    Local Variable Dictionary
    Name        Type        Description
    pw          PrintWriter This variable writes to the high scores file.
    i           int         This loop counter goes through all the high scores.

    Loops
    The for loop goes through the high scores and saves the names, scores, and winTitles in the highscores file.
    Start Condition: i = 0
    End Condition: When all the high scores have been stored
    Increment: Increase i by 1

    Bock Segments
    Try-catch is used here because the PrintWriter constructor can throw IOException
    */
    public void goodbye ()
    {
	title ();
	c.println ("Thank you for using the Crossword Puzzle program!\n");
	c.println ("By: Peter Lin");
	pauseProgram ("Press any key to continue...");
	try
	{
	    PrintWriter pw = new PrintWriter (new FileWriter ("highscores.dat"));
	    for (int i = 0 ; i < 10 ; i++)
	    {
		pw.println (names [i]);
		pw.println (scores [i]);
		pw.println (winTitles [i]);
	    }
	    pw.close ();
	}
	catch (IOException e)
	{
	    JOptionPane.showMessageDialog (null, "An error has been detected with your directory.", "Critical Error", JOptionPane.ERROR_MESSAGE);
	}
	c.close ();
    }


    /* CrosswordPuzzle constructor initializes variables from file IO and makes a new console to draw onto.
    Local Variable Dictionary
    Name                Type            Description
    br                  BufferedReader  This variable reads from the data files and stores it in the program.
    i                   int             This variable generally loops as many times as the first line of input, because the first line of input is how many crosswords there are.
    j                   int             This variable loops from the top of the crossword to the bottom.
    k                   int             This variable loops from the left of the crossword to the right.
    width               int             This variable stores the length of a specific crossword.
    height              int             This variable stores the height of a specific crossword.
    numberOfCrosswords  int             This variable stores the number of crosswords.
    numberOfHints       int             This variable stores the number of hints for a specific crossword.
    
    Loops
    Loop 1 reads all the highscores from the highscores.dat file.
    Start Condition: i = 0
    End Condition: When all the highscores are read.
    Increment: Increase i by 1.
    Loop 2 reads in a 2D crossword numberOfCrossword times. It also reads the width and height of the crosswords and initializes arrays accordingly.
    Start Condition: i = 0
    End Condition: When all the crosswords are read.
    Increment: Increase i by 1.
    Loop 2.1 reads in a line height times.
    Start Condition: j = 0
    End Condition: When all the lines are read.
    Increment: Increase j by 1.
    Loop 2.1.1 copies every character in the lines above.
    Start Condition: k = 0
    End Condition: When all the characters are copied.
    Increment: Increase k by 1.
    Loop 3 reads in all the hints for a crossword numberOfCrossword times.
    Start Condition: i = 0
    End Condition: When all the hints are read.
    Increment: Increase i by 1.
    Loop 3.1 reads in numberOfHints hints for a crossword.
    Start Condition: j = 0
    End Condition: When all the hints for a crossword are read.
    Increment: Increase j by 1.
    
    Conditional Statements
    The if statements checks to for a crossword's word number, and then stores the 1-indexed x and y-coordinates for it.
    
    Block Segments
    Try-catch is used because the BufferedReader constructor can throw IOException.
    */
    public CrosswordPuzzle ()
    {
	c = new Console (36, 120, "Crossword Puzzle"); // creates a new object of the hsa.Console class
	try
	{
	    BufferedReader br = new BufferedReader (new FileReader ("highscores.dat"));
	    for (int i = 0 ; i < 10 ; i++) // loop 1
	    {
		names [i] = br.readLine ();
		scores [i] = Integer.parseInt (br.readLine ());
		winTitles [i] = br.readLine ();
	    }
	    br = new BufferedReader (new FileReader ("crossword.dat"));
	    int numberOfCrosswords = Integer.parseInt (br.readLine ());
	    crossword = new char [numberOfCrosswords] [] [];
	    guess = new char [numberOfCrosswords] [] [];
	    xCoord = new int [numberOfCrosswords] [65];
	    yCoord = new int [numberOfCrosswords] [65];
	    for (int i = 0 ; i < numberOfCrosswords ; i++) // loop 2
	    {
		int width = Integer.parseInt (br.readLine ());
		int height = Integer.parseInt (br.readLine ());
		crossword [i] = new char [width] [height];
		guess [i] = new char [width] [height];
		for (int j = 0 ; j < height ; j++) // loop 2.1
		{
		    String line = br.readLine ();
		    for (int k = 0 ; k < width ; k++) // loop 2.1.1
		    {
			crossword [i] [k] [j] = line.charAt (k);
			if (line.charAt (k) >= 32 && line.charAt (k) <= 64)
			{
			    xCoord [i] [line.charAt (k)] = k + 1; // 1-indexing so the default element value of 0 becomes an index out of bounds
			    yCoord [i] [line.charAt (k)] = j + 1;
			    guess [i] [k] [j] = line.charAt (k);
			}
		    }
		}
	    }
	    br = new BufferedReader (new FileReader ("hints.dat"));
	    numberOfCrosswords = Integer.parseInt (br.readLine ());
	    hints = new String [numberOfCrosswords] [];
	    for (int i = 0 ; i < numberOfCrosswords ; i++) // loop 3
	    {
		int numberOfHints = Integer.parseInt (br.readLine ());
		hints [i] = new String [numberOfHints];
		for (int j = 0 ; j < numberOfHints ; j++) // loop 3.1
		{
		    hints [i] [j] = br.readLine ();
		}
	    }
	}
	catch (IOException e)
	{
	    JOptionPane.showMessageDialog (null, "An error has been detected in your directory.", "Critical Error", JOptionPane.ERROR_MESSAGE);
	}
    }

    /* main method calls methods and controls the program
    Local Variable Dictionary
    Name        Type            Description
    cp          CrosswordPuzzle This stores all the information the program needs to run a Crossword Puzzle
    
    Loops
    Loop 1 repeats as long as the user's choice is not "exit" or '5'.
    Loop 1.1 repeats as long as the crossword is not completed.
    */
    public static void main (String[] args)
    {
	CrosswordPuzzle cp = new CrosswordPuzzle (); // creating a new instance of the CrosswordPuzzle class and a new object of the CrosswordPuzzle class
	cp.splashScreen ();
	while (cp.choice != '5') // loop 1
	{
	    cp.mainMenu ();
	    switch (cp.choice)
	    {
		case '1':
		    cp.instructions ();
		    break;
		case '2':
		    cp.levelSelect ();
		    while (!cp.completed) // loop 1.1
		    {
			cp.display ();
			cp.askData ();
		    }
		    cp.winScreen ();
		    break;
		case '3':
		    cp.highScores ();
		    break;
		case '4':
		    cp.clearHighScores ();
		    break;
	    }
	}
	cp.goodbye ();
    } // end of main method
} // end of CrosswordPuzzle class


