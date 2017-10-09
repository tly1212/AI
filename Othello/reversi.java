package lab2;

/**
 * @author Liang-yu
 * The implementation of game reversi is based on the code of khzaw (https://github.com/khzaw/reversi-othello)
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class reversi {

	private final static int ROWS = 8;
	private final static int COLS = 8;

	// logical board
	static Move[][] board = new Move[ROWS][COLS];
	static Move[][] initialState = new Move[ROWS][COLS];
	static Move[][] currentState = new Move[ROWS][COLS];
	private static Move move_piece;
	private static Move Current_move_piece;
	static int darkScore = 0;
	static int lightScore = 0;
	static int depth = 1;// search depth, default value is 1;
	int[][] weight = { { 8, 1, 4, 4, 4, 4, 1, 8 }, //
			{ 1, 1, 4, 4, 4, 4, 1, 1 }, //
			{ 1, 4, 4, 4, 4, 4, 4, 1 }, //
			{ 1, 4, 4, 4, 4, 4, 4, 1 }, //
			{ 1, 4, 4, 4, 4, 4, 4, 1 }, //
			{ 1, 4, 4, 4, 4, 4, 4, 1 }, //
			{ 1, 1, 2, 2, 2, 2, 1, 1 }, //
			{ 8, 1, 2, 2, 2, 2, 1, 8 } };

	/**
	 * start a new game
	 */
	public reversi() {
		newGame();
	}

	/**
	 * implement the new game
	 */
	public void newGame() {
		// create a empty board
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				board[row][col] = Move.EMPTY;
			}
		}
		move_piece = Move.DARK; // starting from a black player
		Current_move_piece = Move.DARK;// current player
		placePiece(3, 3, Move.LIGHT);
		updateScore();
		placePiece(3, 4, Move.DARK);
		updateScore();
		placePiece(4, 4, Move.LIGHT);
		updateScore();
		placePiece(4, 3, Move.DARK);
		updateScore();
		Scanner scan = new Scanner(System.in);
		System.out.println("Input depth");
		System.out.print("depth =  ");
		if (scan.hasNextInt())// input depth
			depth = scan.nextInt();
		initialState = board;

		// print the initial satate
		printBoard(initialState);

		// start playing
		while (true) {

			if (Current_move_piece == Move.DARK) {// DARK's turn
				move_piece = Move.DARK;
				Scanner scanner = new Scanner(System.in);
				System.out.println("DARK's turn");
				System.out.println("Enter row: ");
				int row = 0, col = 0;
				// input coordinate from console
				if (scanner.hasNextInt())
					row = scanner.nextInt();
				System.out.println("Enter column: ");
				if (scanner.hasNextInt())
					col = scanner.nextInt();

				while (!doFlip(row, col, Current_move_piece, false, board)) {
					System.out.println("Invalid input!");
					System.out.println("Enter row: ");
					if (scanner.hasNextInt())
						row = scanner.nextInt();
					System.out.println("Enter column: ");
					if (scanner.hasNextInt())
						col = scanner.nextInt();
				}
				doFlip(row, col, Current_move_piece, true, board);// flip the
																	// cells
				placePiece(row, col, Current_move_piece);// place the piece from
															// input
				swapTurns2(Current_move_piece);// swap players
				printBoard(board);
				updateScore();// update scores
				if (isTerminal(board)) { // check if it is terminal state
					checkWin(darkScore, lightScore);
					System.exit(0);
				}
			} else {
				System.out.println("LIGHT's turn");// LIGHT's turn
				move_piece = Move.LIGHT;
				copyState(board, currentState);
				int[] ac = makeDecision(board, depth); // choose the best action
				System.out.println("action is:(" + ac[0] + "," + ac[1] + ")");
				board = currentState;
				doFlip(ac[0], ac[1], Current_move_piece, true, board);
				placePiece(ac[0], ac[1], Current_move_piece);
				swapTurns2(Current_move_piece);
				updateScore();
				printBoard(board);
				if (isTerminal(board)) {
					checkWin(darkScore, lightScore);
					System.exit(0);
				}
			}

		} // end of while

	}

	/**
	 * place the piece on given coordinate
	 * 
	 * @param row
	 * @param col
	 * @param color
	 */
	public void placePiece(int row, int col, Move color) {
		if (board[row][col] == Move.EMPTY)
			board[row][col] = color;
		swapTurns(move_piece);
	}

	/**
	 * print the state "board"
	 * 
	 * @param aboard
	 */
	public void printBoard(Move[][] aboard) {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				if (aboard[r][c] == Move.DARK)
					System.out.print("O" + " "); // DARD player use "O"
				else if (aboard[r][c] == Move.LIGHT)
					System.out.print("X" + " "); // LIGHT player use "X"
				else
					System.out.print("_" + " ");// empty cell used "_"
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * update the scores
	 */
	public void updateScore() {
		darkScore = 0;
		lightScore = 0;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (board[row][col] == Move.DARK) {
					darkScore++;
				}
				if (board[row][col] == Move.LIGHT) {
					lightScore++;
				}
			}
		}
	}

	/**
	 * Check if the action is valid or not.If putDown if true, do the flip based
	 * on an action
	 * 
	 * @param row,
	 *            action's row coordinate
	 * @param col,
	 *            action's column coordinate
	 * @param piece,
	 *            current player
	 * @param putDown,
	 *            If putDown if true, do the flip based on an action, otherwise
	 *            return the status.
	 * @param state,
	 *            given state to do the flip
	 * @return
	 */
	public boolean doFlip(int row, int col, Move piece, boolean putDown, Move[][] state) {
		boolean isValid = false;
		for (int dX = -1; dX < 2; dX++) {
			for (int dY = -1; dY < 2; dY++) {
				if (dX == 0 && dY == 0) {
					continue;// do not check itself
				}
				// check the surrounding pieces
				int checkRow = row + dX;
				int checkCol = col + dY;
				// make sure the coordiantes are in the board
				if (checkRow >= 0 && checkCol >= 0 && checkRow < ROWS && checkCol < COLS) {
					// if your piece is white, check for black and vice versa
					if (state[checkRow][checkCol] == (piece == Move.DARK ? Move.LIGHT : Move.DARK)) {
						// keep track of the distance
						for (int distance = 0; distance < 8; distance++) {
							int minorCheckRow = row + distance * dX;
							int minorCheckCol = col + distance * dY;
							if (minorCheckRow < 0 || minorCheckCol < 0 || minorCheckRow > 7 || minorCheckCol > 7)
								continue; // if going out of the borad, ignore
							if (state[minorCheckRow][minorCheckCol] == piece) {
								if (putDown) {
									for (int distance2 = 1; distance2 < distance; distance2++) {
										int flipRow = row + distance2 * dX;
										int flipCol = col + distance2 * dY;
										if (state[flipRow][flipCol] != Move.EMPTY)
											state[flipRow][flipCol] = piece;
									}
								}
								isValid = true;
								break;
							}
						}
					}
				}
			}
		}
		return isValid;
	}

	/**
	 * swape the player of move_piece
	 * 
	 * @param piece
	 */
	private void swapTurns(Move piece) {
		// System.out.println(piece);
		move_piece = (piece == Move.DARK ? Move.LIGHT : Move.DARK);
		// System.out.println(move_piece);
	}

	/**
	 * swape the player of Current_move_piece
	 * 
	 * @param piece
	 */
	private void swapTurns2(Move piece) {
		// System.out.println(piece);
		Current_move_piece = (piece == Move.DARK ? Move.LIGHT : Move.DARK);
		// System.out.println(Current_move_piece);
	}

	/**
	 * check who wins
	 * 
	 * @param totalDark,
	 *            dark player's score
	 * @param totalLight,
	 *            light player's score
	 */
	private void checkWin(int totalDark, int totalLight) {
		if (totalDark > totalLight) {
			System.out.println("DARK Player Wins!");
		} else if (totalLight > totalDark) {
			System.out.println("LIGHT Player Wins!");
		} else if (totalLight == totalDark) {
			System.out.println("It's a Tie!");
		}
	}

	/**
	 * get the current player
	 * 
	 * @param state
	 * @return
	 */
	public Move getPlayer(Move[][] state) {
		return move_piece;
	}

	/**
	 * get all the legal movements
	 * 
	 * @param state,
	 *            given the state @return, the list of legal actions
	 */
	public List<int[]> getActions(Move[][] state) {
		ArrayList<int[]> ret = new ArrayList<>();
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				if (state[r][c] == Move.EMPTY)
					if (doFlip(r, c, getPlayer(state), false, state)) {
						int[] coor = new int[2];
						coor[0] = r;
						coor[1] = c;
						ret.add(coor);
					}
			}
		}
		return ret;
	}

	/**
	 * return the result of state take a given action
	 * 
	 * @param state,
	 *            given state
	 * @param action,
	 *            given action
	 * @return updated state
	 */
	public Move[][] getResult(Move[][] state, int[] action) {
		board = state;
		doFlip(action[0], action[1], move_piece, true, state);
		placePiece(action[0], action[1], move_piece);
		Move[][] updatedState = board;
		return updatedState;
	}

	/**
	 * check if it is a termimal state
	 * 
	 * @param state,
	 *            given state
	 * @return a boolean value
	 */
	public boolean isTerminal(Move[][] state) {
		boolean moreValidPosition = false;
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				if (doFlip(r, c, Move.DARK, false, state) || doFlip(r, c, Move.LIGHT, false, state))
					moreValidPosition = true;
			}
		}
		return !moreValidPosition;
	}

	/**
	 * get the corresponding scores
	 * 
	 * @param state,
	 *            given state
	 * @param player,given
	 *            player
	 * @return the score of the player on given state
	 */
	public double getUtility(Move[][] state, Move player) {
		int dark = 0, light = 0;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (state[row][col] == Move.DARK) {
					dark += weight[row][col];
				}
				if (state[row][col] == Move.LIGHT) {
					light += weight[row][col];
				}
			}
		}
		if (player == Move.DARK)
			return dark;
		else if (player == Move.LIGHT)
			return light;
		else {
			System.out.println("Check the getUtility method!");
			return 0;
		}
	}

	/**
	 * find the best action
	 * 
	 * @param state,
	 *            given state
	 * @param depth,
	 *            search depth
	 * @return the best action
	 */
	public int[] makeDecision(Move[][] state, int depth) {
		int[] result = null;
		double resultValue = Double.NEGATIVE_INFINITY;
		Move player = getPlayer(state);
		// System.out.println(player);
		List<int[]> acList = getActions(state);
		// for (int[] e : acList) {
		// System.out.println(e[0] + " : " + e[1]);
		// }
		for (int[] action : acList) {
			Move[][] temp = new Move[ROWS][COLS];
			Move tempPlayer = (player == Move.DARK ? Move.DARK : Move.LIGHT);
			copyState(state, temp);
			Move[][] tempState = getResult(temp, action);
			move_piece = (player == Move.DARK ? Move.DARK : Move.LIGHT);
			// printBoard(tempState);
			double value = minValue(tempState, tempPlayer, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, depth);
			if (value > resultValue) {
				result = action;
				resultValue = value;
			}
		}
		return result;
	}

	/**
	 * find out the max scores of next movement based on given information
	 * 
	 * @param state,
	 *            given state
	 * @param player,
	 *            current player
	 * @param alpha
	 * @param beta
	 * @param depth,
	 *            search depth
	 * @return
	 */
	public double maxValue(Move[][] state, Move player, double alpha, double beta, int depth) {
		depth -= 1;
		if (isTerminal(state) || depth == 0)
			return getUtility(state, player);// ????
		double value = Double.NEGATIVE_INFINITY;
		List<int[]> acList = getActions(state);
		// for (int[] e : acList) {
		// System.out.println(e[0] + " : " + e[1]);
		// }
		for (int[] action : acList) {
			Move[][] temp = new Move[ROWS][COLS];
			Move tempPlayer = (player == Move.DARK ? Move.DARK : Move.LIGHT);
			copyState(state, temp);
			value = Math.max(value, minValue(getResult(temp, action), tempPlayer, alpha, beta, depth));
			move_piece = (player == Move.DARK ? Move.DARK : Move.LIGHT);
			if (value >= beta)
				return value;
			alpha = Math.max(alpha, value);
		}
		return value;
	}

	/**
	 * find out the min scores of next movement based on given information
	 * 
	 * @param state,
	 *            given state
	 * @param player,
	 *            current player
	 * @param alpha
	 * @param beta
	 * @param depth,
	 *            search depth
	 * @return
	 */
	public double minValue(Move[][] state, Move player, double alpha, double beta, int depth) {
		depth -= 1;
		if (isTerminal(state) || depth == 0)
			return getUtility(state, player);// ?????
		double value = Double.POSITIVE_INFINITY;
		List<int[]> acList = getActions(state);
		// for (int[] e : acList) {
		// System.out.println(e[0] + " : " + e[1]);
		// }
		for (int[] action : acList) {
			Move[][] temp = new Move[ROWS][COLS];
			Move tempPlayer = (player == Move.DARK ? Move.DARK : Move.LIGHT);
			copyState(state, temp);
			value = Math.min(value, maxValue(getResult(temp, action), tempPlayer, alpha, beta, depth));
			move_piece = (player == Move.DARK ? Move.DARK : Move.LIGHT);
			if (value <= alpha)
				return value;
			beta = Math.min(beta, value);
		}
		return value;
	}

	/**
	 * copy the state
	 * 
	 * @param s,
	 *            source state
	 * @param t,
	 *            destination of copy
	 */
	public void copyState(Move[][] s, Move[][] t) {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				t[r][c] = s[r][c];
			}
		}
	}
}
