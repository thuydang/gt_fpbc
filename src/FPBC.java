import java.util.Arrays;
import java.util.Random;

import javax.swing.text.Utilities;

/**
 *	Fictious Play by Cases
 *	Game theory algorithm
 */

public class FPBC {
	
	public int numGame;
	public Game[] games;
	public int time;
	// history [time][game, strategy_1, strategy_2]
	public int[][] history;


	public FPBC(int numGame, int numPlayer) {
		this.numGame = numGame;
		history = new int[1100][numPlayer + 1];

		games = new Game[numGame];
		for (int i=0; i<numGame; i++) {
			games[i] = new Game("Game_" + i, numPlayer);
		}
		
	}

	public FPBC(int numGame) {
		this(numGame, 2);
	}

	// step 1:
	public void init() {
		time = 0;

		for (Game g:games) {
			g.init();
		}
	}

	public void main_loop() {
		// step 2. main loop:
		while (true) {
			System.out.println("---------- Time " + time + " -----------");
			// step 2.1
			int current_game = new Random().nextInt(games.length);
			Game g = games[current_game];
			System.out.println(g.toString());
			
			// step 2.2
			for (int player_i=0; player_i < g.players.length; player_i++) {
				double max_utility = -1;
				int chosen_strategy_i = 0;
				double utility_i = 0;

				System.out.print("Player " + g.players[player_i].name + " weights: ");
				System.out.println(Arrays.toString(g.players[player_i].weights));

				g.players[player_i].updateForecasts();

				System.out.print("Player " + player_i + " forecasts: ");
				System.out.println(Arrays.toString(g.players[player_i].forecasts));

				
				for (int strategy_i=0; strategy_i < g.players[player_i].numStrategy; strategy_i++) {
					utility_i = 0;
					for (int strategy_j=0; strategy_j < g.getOpponent(g.players[player_i]).numStrategy; strategy_j++) {
						utility_i += g.payoffs[strategy_i][strategy_j][player_i] * g.players[player_i].forecasts[strategy_j];
					}
	
					System.out.println("Utility " + g.players[player_i].name + " : " + utility_i + 
							" strategy_i " + strategy_i);
							
					if (utility_i > max_utility) {
						max_utility = utility_i;
						chosen_strategy_i = strategy_i;
					}
				}
				System.out.println("Player " + player_i + " takes s" + chosen_strategy_i);
				g.players[player_i].current_strategy = chosen_strategy_i;

				//update history
				history[time][player_i] = g.players[player_i].current_strategy;
			}

			// step 2.3
			// last game
			history[time][g.numPlayer] = current_game;
			System.out.print("History: ");
			System.out.println(Arrays.deepToString(history));
			
			// step 2.4
			// update weight
			for (int game_i=0; game_i < numGame; game_i++) {
				g = games[game_i];
				for (int player_i=0; player_i < g.players.length; player_i++) {
					for (int strategy_j=0; strategy_j < g.players[player_i].weights.length; strategy_j++) {
						if (g.getOpponent(g.players[player_i]).current_strategy == strategy_j)
							g.players[player_i].weights[strategy_j] += 1; 
						else
							g.players[player_i].weights[strategy_j] += 0; 
					}
				}

			}
			
			
			time++;
			if (time == 1000) break;
		}
		//- while		
	}

	public static void main(String[] args) {
		System.out.println("Hello world!");
		FPBC alg = new FPBC(9);
		alg.init();
		alg.main_loop();
	}

	// 
	public class Game {
		public String name;
		public int numPlayer;
		public Player[] players;
		// payoffs[p1 strategies][p2 strategies][payoff 1/2]
		public double[][][] payoffs;
		//Payoffs payoffs_set = new Payoffs(player_numStrategy_map);
		public int[] player_numStrategy_map;
		
		// bootstrap
		public Game(String name, int numPlayer) {
			/* game config */
			this.name = name;
			this.numPlayer = numPlayer;
			player_numStrategy_map = new int[numPlayer];
			
			// each player has 2 strategies.
			//for (int i: player_numStrategy_map) {
			//	i = 2;
			//}
			for (int i=0; i < player_numStrategy_map.length; i++) {
				player_numStrategy_map[i] = 2;
			}
			
			/* game memory */			
			
			/* game setup */
			players = new Player[numPlayer];
			
			for(int i = 0; i < players.length; i++) {
				players[i] = new Player(this, i, player_numStrategy_map[i]);
			}

			// set payoffs
			//payoffs_set.setPayoffs();
			payoffs = new double[players[0].numStrategy][players[1].numStrategy][players.length];
			
			//System.out.println(name + "   C0        C1");
			for (int i=0; i<players[0].numStrategy; i++) {
				//System.out.print("R"+i+" [");
				for (int j=0; j<getOpponent(players[0]).numStrategy; j++) {
					//System.out.print("[");
					for (int k=0; k<numPlayer; k++) {
						payoffs[i][j][k] = new Random().nextInt(1000);
						//System.out.print(payoffs[i][j][k] + ", ");
					}
					//System.out.print("], ");
				}
				//System.out.println("]");
			}
			//System.out.println();
			
			//System.out.println(Arrays.deepToString(payoffs));			
			
		}

		
		public String toString() {
			String ret = "";
			
			ret = name + "   C0        C1 \n";
			for (int i=0; i<players[0].numStrategy; i++) {
				ret += "R"+i+" [";
				for (int j=0; j<getOpponent(players[0]).numStrategy; j++) {
					ret+= "s" + i + "-s"+j + " [";
					for (int k=0; k<numPlayer; k++) {
						
						ret+= payoffs[i][j][k] + ", ";
					}
					ret+=("], ");
				}
				ret+=("]\n");
			}
			
			return ret;
		}
		
		public void init() {
			for (Player p:players) {
				p.init();
			}
		}

		public Player getOpponent(Player player) {			
			
			for(Player p: players) {
				if (!p.name.equals(player.name))
					return p;
			}
			
			return null;
		}
	}
	//- class Game
	
	public class Player {
		public String name;
		public int numStrategy;
		public Game game;
		public double[] weights;
		public double[] forecasts;
		public int current_strategy;

		//bootstrap
		public Player(Game game, int name, int numStrategy) {
			this.name = String.valueOf(name);
			this.game = game;
			this.numStrategy = numStrategy;

		}
		
		public void init(){

			weights = new double[game.getOpponent(this).numStrategy];
			forecasts = new double[game.getOpponent(this).numStrategy];
			boolean exist_gt_zero = false;
			
			for (int strategy_j=0; strategy_j<weights.length; strategy_j++) {
				weights[strategy_j] = new Random().nextInt(5);
				if (weights[strategy_j] > 0) 
					exist_gt_zero = true;
			}
			
			if (!exist_gt_zero) init();
			//updateForecasts();
		}

		public void updateForecasts() {
			double sum_weight = 0;
			for (double i: weights) {
				sum_weight += i;
			}
			
			for (int strategy_j=0; strategy_j<weights.length; strategy_j++) {
				forecasts[strategy_j] = weights[strategy_j]/sum_weight;
			}
		}
		
	}
	//- class Player
}

