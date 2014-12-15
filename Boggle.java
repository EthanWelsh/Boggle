import java.io.*;
import java.util.*;

/********************************************************\
 *                                                      *
 *                         BOGGLE                       *
 *                                                      *
 \********************************************************/
public class Boggle
{
	public static void main(String[] args) throws Exception
	{
		StopWatch watch = new StopWatch();
		watch.start();

		if(args.length != 2)
		{
			System.out.println("usage: boggle board.txt dict.txt");
			return;
		}

		String[][] swamp = loadBoard(args[0]);
		Solution s = new Solution(swamp, args[1]);
		watch.stop();
		System.out.println("Solution found in " + watch.getTime() + " milliseconds, or " + watch.getTimeSecs() + " seconds.");
	}

	private static String[][] loadBoard(String infileName) throws Exception
	{
		Scanner infile = new Scanner(new File(infileName));
		int d = infile.nextInt();
		String[][] board = new String[d][d];
		for (int r = 0; r < d; r++)
			for (int c = 0; c < d; c++)
				if (infile.hasNext()) board[r][c] = infile.next().toLowerCase();
		infile.close();
		return board;
	}
}

/********************************************************\
 *                                                      *
 *                       SOLUTION                       *
 *                                                      *
 \********************************************************/
class Solution
{
	Point dropInPoint;
	String[][] board;
	Stack myPath;

	// N, NE, E, SE, S, SW, W, NW:
	Point[] compass = {new Point(-1, 0), new Point(-1, 1), new Point(0, 1), new Point(1, 1), new Point(1, 0), new Point(1, -1), new Point(0, -1), new Point(-1, -1)};
	Map<String, Boolean> dictHash = new HashMap<String, Boolean>(2179192);
	StringBuilder lettersNotOnTheBoard;
	ArrayList<String> wordsAlreadyUsed = new ArrayList<String>();


	public Solution(String[][] x, String dict) throws IOException
	{
		board = x;
		myPath = new Stack();
		generateAlphabet();
		generateDict(dict);
		Point moveableDropPoint;

		for (int r = 0; r < board.length; r++)
			for (int c = 0; c < board.length; c++)
			{
				myPath = new Stack();
				moveableDropPoint = new Point(r, c);
				myPath.push(moveableDropPoint);
				solve(moveableDropPoint);
			}
		BufferedWriter outputWriter = new BufferedWriter(new FileWriter("out.txt"));

		for (String q : wordsAlreadyUsed)
		{
			outputWriter.write(q);
			outputWriter.newLine();
			outputWriter.flush();
		}

		System.out.println(wordsAlreadyUsed.size() + " found. Check output.txt for the complete list of words.");
	}

	public void generateAlphabet()
	{ // Generates a string of all the letters which aren't in the board, and therefore any words with these letters would be redundant to search for
		StringBuilder b = new StringBuilder();
		for (String[] aBoard : board)
		{
			for (int j = 0; j < aBoard.length; j++)
				b.append((aBoard[j]));
		}

		lettersNotOnTheBoard = new StringBuilder("abcdefghijklmnopqrstuvwxyz");
		char x;
		int y;

		while (b.length() > 0)
		{
			x = b.charAt(b.length() - 1);
			y = lettersNotOnTheBoard.indexOf(x + "");
			if (y > -1) lettersNotOnTheBoard.deleteCharAt(y);
			b.deleteCharAt(b.lastIndexOf(x + ""));
		}
	}

	public void generateDict(String dict) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(dict));
		String sCurrentLine;
		boolean thereIsNoPointInGoingFurther = false;
		int numberOfLettersNotOnTheBoard = lettersNotOnTheBoard.length();

		while ((sCurrentLine = br.readLine()) != null)
		{
			if (sCurrentLine.length() < board.length * board.length)
			{ // If the string isn't too large
				for (int i = 0; i < numberOfLettersNotOnTheBoard; i++)
				{
					if (sCurrentLine.indexOf(lettersNotOnTheBoard.charAt(i)) != -1)
					{ // Make sure it doesn't contain any impossible characters
						thereIsNoPointInGoingFurther = true;
						break;
					} else thereIsNoPointInGoingFurther = false;
				}
				if (!thereIsNoPointInGoingFurther)
				{ // And if it doesn't, add the word to the dictionary.
					dictHash.put(sCurrentLine, true);
					while (sCurrentLine.length() > 0)
					{ // And all the words substrings
						sCurrentLine = sCurrentLine.substring(0, sCurrentLine.length() - 1);
						if (dictHash.containsKey(sCurrentLine) == false)
							dictHash.put(sCurrentLine, false); // Just so long as you haven't already added
					}
				}
			}
		}
	}


	private StringBuilder getPathWord()
	{
		StringBuilder q = new StringBuilder();
		Node head = myPath.getHead();

		while (head != null)
		{
			if (mazeGet(head.getData()).length() == 2)
				q.append(new StringBuffer(mazeGet(head.getData())).reverse().toString()); // For qu
			else q.append(mazeGet(head.getData()));
			head = head.getNext();
		}

		return q.reverse();
	}


	private boolean check(Point p)
	{ // Check in a direction to see if it is a path
		if (myPath != null && p != null)
		{
			if (myPath.search(p)) return false; // If point already in path


			if (dictHash.containsKey((getPathWord().append(mazeGet(p)).toString()))) return true;
		} else return false;
		return false;
	}


	private String mazeGet(Point p)
	{
		return board[p.getY()][p.getX()];
	}


	public void solve(Point currentPoint)
	{
		for (Point direction : compass) // For each direction on the compass...
		{
			try
			{
				if (check(currentPoint.add(direction))) // See if you can go there
				{ // and if you can...
					String thePath = getPathWord().append(mazeGet(currentPoint.add(direction))).toString();

					if (dictHash.containsKey(thePath) && dictHash.get(thePath) == true && thePath.length() >= 3)
					{
						wordsAlreadyUsed.add(thePath); // If this path is a word or substring thereof and hasn't already been used
						dictHash.put(thePath, false);
					}
					myPath.push(currentPoint.add(direction)); // Add it to your stack
					solve(currentPoint.add(direction)); // Continue on from the new direction
				}
			} catch (Exception e)
			{
			}
		}
		myPath.pop(); // Take yourself off the stack
	}
}

/********************************************************\
 *                                                      *
 *                        STACK                         *
 *                                                      *
 \********************************************************/
class Stack
{
	private Node head;

	public Stack()
	{
		head = null;
	}


	public void push(Point data)
	{
		head = new Node(data, head);
	}


	public String toString()
	{
		return printList(head, "");
	}


	private String printList(Node head, String x)
	{
		if (head == null) return x;
		x = x + head;
		return printList(head.getNext(), x);
	}


	public Point pop()
	{
		if (head == null) return null;
		Point p = head.getData();
		head = head.getNext();
		return p;
	}


	public Node getHead()
	{
		return head;
	}


	public int length()
	{
		return lenR(head);
	}


	private int lenR(Node head)
	{
		if (head == null) return 0;
		return 1 + lenR(head.getNext());
	}


	public boolean search(Point p)
	{
		return searchHelper(head, p);
	}


	private boolean searchHelper(Node head, Point p)
	{
		if (head == null) return false;
		if (head.getNext() == null) return false;
		return head.getNext().getData().equals(p) || searchHelper(head.getNext(), p);
	}
}

/********************************************************\
 *                                                      *
 *                       POINT                          *
 *                                                      *
 \********************************************************/
class Point
{
	private int x;
	private int y;

	public Point(int a, int b)
	{
		x = a;
		y = b;
	}


	public int getX()
	{
		return x;
	}


	public int getY()
	{
		return y;
	}


	public Point add(Point p)
	{
		return new Point(x + p.getX(), y + p.getY());
	}


	public String toString()
	{
		return "[ " + this.getX() + " , " + this.getY() + " ]";
	}


	public boolean equals(Point p)
	{
		return x == p.getX() && y == p.getY();
	}
}

/********************************************************\
 *                                                      *
 *                         NODE                         *
 *                                                      *
 \********************************************************/
class Node
{
	private Point data;
	private Node next;

	public Node(Point data, Node next)
	{
		this.data = data;
		this.next = next;
	}


	public Point getData()
	{
		return data;
	}


	public Node getNext()
	{
		return next;
	}


	public String toString()
	{
		return data + "";
	}


	public void setNext(Node next)
	{
		this.next = next;
	}
}

/********************************************************\
 *                                                      *
 *                      STOPWATCH                       *
 *                                                      *
 \********************************************************/
class StopWatch
{
	private double startTime = 0;
	private double stopTime = 0;
	private boolean flag = false;

	public void start()
	{
		startTime = System.currentTimeMillis();
		flag = true;
	}


	public void stop()
	{
		stopTime = System.currentTimeMillis();
		flag = false;
	}


	public double getTime()
	{
		double elapsedTime;
		if (flag) elapsedTime = (System.currentTimeMillis() - startTime);
		else elapsedTime = (stopTime - startTime);
		return elapsedTime;
	}


	public double getTimeSecs()
	{
		double elapsed;
		if (flag) elapsed = ((System.currentTimeMillis() - startTime) / 1000);
		else elapsed = ((stopTime - startTime) / 1000);
		return elapsed;
	}
}