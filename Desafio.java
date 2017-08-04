import java.lang.*;
import java.util.*;
import java.awt.Point;


class Enemy{
	protected int p; ///< Power
	protected int b; ///< Bullets

	public Enemy(){
		this.p=-1;
		this.b=-1;
	}

	public Enemy(int p, int b){
		this.setP(p);
		this.setB(b);
		
	}
	/**
	 * Gets the power.
	 *
	 * @return     The p.
	 */
	public int getP(){
		return p;
	}

	/**
	 * Gets the bullets.
	 *
	 * @return     The b.
	 */
	public int getB(){
		return b;
	}

	/**
	 * Sets the power.
	 *
	 * @param      p     { parameter_description }
	 */
	public void setP(int p){
		this.p = p;
	}

	/**
	 * Sets the bullets.
	 *
	 * @param      b     { parameter_description }
	 */
	public void setB(int b){
		this.b = b;
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return     String representation of the object.
	 */
	public String toString(){
		int diff = this.b-this.p;
		return "P = "+this.p+" B = "+this.b + " P-B = "+diff;
	}

	
}

/**
 * Classe para resolver o desafio
 */
public class Desafio{

	public static final int M_SIZE = 500000; ///< Tamanho maximo de M (numero de inimigos)
	public static final int N_SIZE = 100; ///< Numer maximo de niveis
	public static final int B_SIZE = 1000; ///< Numero maximo de municoes que um inimigo pode ter
	private static List<List< Enemy> > enemiesPerLevel= new ArrayList<List<Enemy> >(); ///< Armazena os inimigos de cada nivel
	private static int M,N; ///< M,N para cada caso de teste
	private static int maxBFound=0; ///< Armazena o maior valor de B encontrado em um caso de teste. Utilizado para agilizar a reinicializacao da matriz memo
	
	private static List<List<Integer >> memo = new ArrayList<List<Integer> >(); ///< matriz de memoizacao


	/**
	 * Comparador para ordenar os inimigos em ordem crescente de Power
	 */
	private static	Comparator<Enemy> customComparatorP = new Comparator<Enemy>(){
		public int compare(Enemy o1, Enemy o2){
			// int diff1 = o1.getB() - o1.getP();
			// int diff2 = o2.getB() - o2.getP();

			if(o1.getP() == o2.getP()){
				return 0;
			}else if(o1.getP() <= o2.getP()){
				return -1; 
			}else{
				return 1;
			}
		}
	};


	static{
		Desafio.allocateMemo();
		
	}

	/**
	 * Aloca matriz de memoizacao
	 */
	private static void allocateMemo(){
		//Matriz de memoizacao tem tamanho N_SIZE+1 * B_SIZE+1
		for(int i=0;i<N_SIZE+1;i++){
			List<Integer> aux = new ArrayList<Integer>();
			
			for(int j=0;j<B_SIZE+1;j++){
				aux.add(null);
				
			}
			memo.add(aux);
			
		}
	}

	
	/**
	 * Resets matriz de memoizacao para utilizacao em outro caso de teste
	 *
	 * @param      n1	quantidade de niveis maximo do novo caso de teste
	 */
	private static void resetMemo(int n1){
		
		//notar que, nesta funcao, esta sendo limpado somente as posicoes que serao utilizadas, ou seja, as linhas 0 ate n1, e as colunas
		// 0 ate maxBFound

		for(int n=0;n<n1;n++){ // para os niveis 0 ate n1-1, deve ser atribuido valor nulo
			for(int bacc=0;bacc<maxBFound+1;bacc++){
				Desafio.memo.get(n).set(bacc,null);
			}
		}
		for(int bacc=0;bacc<maxBFound+1;bacc++){ //para a coluna n1, deve ser atribuido valor 0
			Desafio.memo.get(n1).set(bacc,0);
		}

	}


	private static void printMemo(){
		//Somente para debug
		for(int bacc=B_SIZE;bacc>=0;bacc--){
			for(int n=0;n<N+1;n++){
				System.out.print(Desafio.memo.get(n).get(bacc)+"\t");
			}
			System.out.println();
		}
	}

	public static void printInitialState(){
		//somente para debug
		for(int n=0;n<N;n++){
			System.out.println("Nivel "+n);
			System.out.println("P\tB\tP-B");
			for(int m=0;m<M;m++){
				int p,b,aux;
				p= enemiesPerLevel.get(n).get(m).getP();
				b= enemiesPerLevel.get(n).get(m).getB();
				
				aux=b-p;
				System.out.println(p+"\t"+b+"\t"+aux);
			}
			System.out.println();
		}
	}

	
	/**
	 * RElacao de Recorrencia utilizada:
	 *  
	 *  Q(n,bacc) = Min{m pertence a M}{max(0,P_n_m-bacc) + Q(n+1,B_n_m)}
	 *  Q(n,bacc)= 0 para todo n>N
	 *  
	 *  Onde Q(n,bacc) eh a quantidade de municao inicial necessaria  para sair no nivel n e completar o jogo, tendo bacc municoes adquiridas no nivel anterior.
	 *	Onde P_n_m eh o valor power do inimigo m do nivel n
	 *	Onde B_n_m eh a quantidade de municao que o inimigo m do nivel n possui
	 * @param      n     The level
	 * @param      bacc  The amount of bullets acquired in the immediate previous level
	 *
	 * @return     The value of memoization matrix.
	 */
	private static int getValueOfMemo(int n, int bacc){
	
		if(Desafio.memo.get(n).get(bacc) == null){
			
			Integer value=Integer.MAX_VALUE;

			for(int m=0;m<M;m++){
				// caso o inimigo m seja escolhido para ser abatido
				Enemy enemy = enemiesPerLevel.get(n).get(m);

				// A quantidade de municao necessaria para abater m eh igual a P_n_m. Para abate-lo
				// eh possivel utilizar tanto municoes iniciais, quanto municoes. Como deseja-se minimizar o uso de municoes iniciais,
				// assume-se que serao utilizadas quantas municoes acumuladas do nivel anterior quanto mossivel. Logo, utilizando todas 
				// as acumuladas, ainda seria preciso P_n_m - bacc de municoes iniciais para abate-lo. Note que esse valor pode ser negativo,
				// indicand que nao eh necessario utilizar municoes iniciais. Por este motivo, utiliza-se o max.
				int extraBulletsNeedes = Math.max(0,enemy.getP()-bacc); 

				

				// Ao escolher o inimigo m, se o valor de municao inicial for maior do que um "value" ja conhecido, entao nao precisa considerar
				if(extraBulletsNeedes>=value){
					continue;
				}
				value = Math.min(value,extraBulletsNeedes + Desafio.getValueOfMemo(n+1,enemy.getB()));

				if(value ==0){ //como zero eh o menor valor possivel, ao descobrir uma solucao com zero, nao precisa avaliar outras mais
					break;
				}
			}
			Desafio.memo.get(n).set(bacc,value);
		}

		return Desafio.memo.get(n).get(bacc);
	}

	

	
	public static int solve(){
		int res;
		
		Desafio.resetMemo(N); //reinitialize memoization matrix

		res =  Desafio.getValueOfMemo(0,0); // a solucao desejada se encontra na posicao 0,0 da matriz
		

		return res;
	}

	public static void main(String[] args){
		int T;
		
		
		Scanner sc = new Scanner(System.in);
		
		T = sc.nextInt(); //le quantidade de casos de teste
		for(int t = 0;t<T;t++){
			N = sc.nextInt(); //le numero de niveis
			M = sc.nextInt(); //le numero de inimigos

			enemiesPerLevel.clear(); //limpa lista de inimigos
			maxBFound=0;

			
			for(int n=0;n<N;n++){
				List< Enemy> aux = new ArrayList<Enemy>();				
				for(int m=0;m<M;m++){
					Enemy auxEnemy = new Enemy(); //cria novo inimigo
					auxEnemy.setP(sc.nextInt()); //seta somente Power
					aux.add(auxEnemy);
				}
				enemiesPerLevel.add(aux);
				
			}
		
			for(int n=0;n<N;n++){
				for(int m=0;m<M;m++){
					
					int b =sc.nextInt();
					enemiesPerLevel.get(n).get(m).setB(b); //seta Bullet para  o inimigo ja criado
					maxBFound = Math.max(maxBFound,b); //armazena qual o maior valor de Bllet lido
					
				}
				
			}

			
			
			int res = Desafio.solve();
			System.out.println(res);


		}
		
	}
}
