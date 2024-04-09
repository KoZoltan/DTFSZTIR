package beadando;

import java.util.*;

public class Simulator {



	class T_JOB {
	    int id;
	    long[] ProcT;
	    long[] StartT;
	    long[] EndT;
	    long d;
	}

	class T_TIMEWINDOW {
	    long ST;
	    long ET;
	}

	class T_RES {
	    int id;
	    long[] TransT;
	    long[][] SetT;
	    int NCal;
	    T_TIMEWINDOW[] Cal;
	}

	public class Main {
	    static final int NOBJF = 4;

	    public static void main(String[] args) {
	        double[] f = new double[NOBJF];
	        double[] w = new double[NOBJF];

	        int NJ, NR;
	        Scanner scanner = new Scanner(System.in);
	        Random random = new Random();

	        System.out.println("\nFlow shop modell");
	        System.out.print("Munkak szama=");
	        NJ = scanner.nextInt();
	        System.out.print("Munkahelyek szama=");
	        NR = scanner.nextInt();

	        System.out.println("Celfuggvenyek prioritasa");
	        for (int k = 0; k < NOBJF; k++) {
	            System.out.print("w[" + k + "] = ");
	            w[k] = scanner.nextDouble();
	        }

	        System.out.println("Az operaciok megszakithatok (i/n) ?");
	        char respons = scanner.next().charAt(0);
	        int cut_mode = (respons == 'i' || respons == 'I') ? 1 : 0;

	        T_RES[] res = new T_RES[NR];
	        for (int r = 0; r < NR; r++) {
	            res[r] = new T_RES();
	            res[r].id = r;
	            res[r].TransT = new long[NR];
	            for (int k = 0; k < NR; k++)
	                res[r].TransT[k] = 10 + random.nextInt(20);

	            res[r].SetT = new long[NJ][NJ];
	            for (int i = 0; i < NJ; i++) {
	                for (int j = 0; j < NJ; j++) {
	                    if (i == j)
	                        res[r].SetT[i][j] = 0;
	                    else
	                        res[r].SetT[i][j] = 10 + random.nextInt(100);
	                }
	            }

	            res[r].NCal = 2 + random.nextInt(10);
	            res[r].Cal = new T_TIMEWINDOW[res[r].NCal];
	            for (int c = 0; c < res[r].NCal; c++) {
	                if (c == 0)
	                    res[r].Cal[c].ST = 10 + random.nextInt(30);
	                else
	                    res[r].Cal[c].ST = res[r].Cal[c - 1].ET + random.nextInt(30);

	                res[r].Cal[c].ET = res[r].Cal[c].ST + random.nextInt(100);
	            }
	        }

	        int[] s = new int[NJ];
	        T_JOB[] job = new T_JOB[NJ];
	        for (int i = 0; i < NJ; i++) {
	            job[i] = new T_JOB();
	            job[i].id = i;
	            job[i].ProcT = new long[NR];
	            for (int r = 0; r < NR; r++)
	                job[i].ProcT[r] = 1 + random.nextInt(100);

	            job[i].StartT = new long[NR];
	            job[i].EndT = new long[NR];
	            job[i].d = 100 + random.nextInt(5000);
	            s[i] = NJ - i - 1;
	        }

	        System.out.println("\nAd-hoc sorrend");
	        Simulation_FS(job, NJ, res, NR, s, 0, cut_mode);
	        System.out.println("Cmax = " + Evaluate(job, NJ, NR, s));

	        if (NR == 2) {
	            System.out.println("\nJohnson sorrend");
	            Johnson_alg(job, NJ, 0, s);
	            Simulation_FS(job, NJ, res, NR, s, 0, cut_mode);
	            System.out.println("Cmax = " + Evaluate(job, NJ, NR, s));
	        }

	        if (NR == 3) {
	            System.out.println("\nKiterjesztett Johnson sorrend");
	            int r_value = F3_Johnson_alg_ext(job, NJ, 0, s);
	            Simulation_FS(job, NJ, res, NR, s, 0, cut_mode);
	            System.out.println("Cmax = " + Evaluate(job, NJ, NR, s));
	            if (r_value == 1)
	                System.out.println("A megoldás optimális!");
	            else
	                System.out.println("Nem garantálható az optimum elérése.");


	        }
	    }

	    static void Simulation_FS(T_JOB[] job, int NJ, T_RES[] res, int NR, int[] s, int swtch, int cut_mode) {
	        long[][] ReleaseT = new long[NJ][NR];
	        long[][] OrderT = new long[NJ][NR];
	        long[] res_end = new long[NR];
	        for (int r = 0; r < NR; r++)
	            res_end[r] = 0;

	        for (int j = 0; j < NJ; j++) {
	            for (int r = 0; r < NR; r++) {
	                if (j == 0)
	                    ReleaseT[j][r] = 0;
	                else
	                    ReleaseT[j][r] = Math.max(ReleaseT[j - 1][r] + job[s[j - 1]].ProcT[r], res_end[r]);

	                OrderT[j][r] = ReleaseT[j][r] + job[s[j]].ProcT[r];
	                job[s[j]].StartT[r] = ReleaseT[j][r];
	                job[s[j]].EndT[r] = OrderT[j][r];
	                res_end[r] = OrderT[j][r];
	            }
	        }

	        if (cut_mode == 1) {
	            for (int r = 0; r < NR; r++) {
	                for (int j = 1; j < NJ; j++) {
	                    if (job[s[j]].StartT[r] < job[s[j - 1]].EndT[r])
	                        job[s[j]].StartT[r] = job[s[j - 1]].EndT[r];
	                    job[s[j]].EndT[r] = job[s[j]].StartT[r] + job[s[j]].ProcT[r];
	                }
	            }
	        }
	    }

	    static long Evaluate(T_JOB[] job, int NJ, int NR, int[] s) {
	        long cmax = 0;
	        for (int j = 0; j < NJ; j++) {
	            long sum = 0;
	            for (int r = 0; r < NR; r++) {
	                if (j == 0)
	                    sum += job[s[j]].ProcT[r];
	                else
	                    sum += job[s[j]].ProcT[r] + job[s[j - 1]].EndT[r];
	            }
	            if (sum > cmax)
	                cmax = sum;
	        }
	        return cmax;
	    }

	    static void Johnson_alg(T_JOB[] job, int NJ, int swtch, int[] s) {
	        int l = 0, k, min, min_type;
	        int[] S = new int[NJ];
	        for (int i = 0; i < NJ; i++)
	            S[i] = 0;

	        if (swtch == 0) {
	            for (int j = 0; j < NJ; j++) {
	                min = Integer.MAX_VALUE;
	                min_type = 0;
	                for (int i = 0; i < NJ; i++) {
	                    if (S[i] == 0) {
	                        if (min > job[i].ProcT[1]) {
	                            min = (int) job[i].ProcT[1];
	                            l = i;
	                            min_type = 1;
	                        }
	                        if (min > job[i].ProcT[0]) {
	                            min = (int) job[i].ProcT[0];
	                            l = i;
	                            min_type = 0;
	                        }
	                    }
	                }
	                S[l] = 1;
	                s[j] = l;
	            }
	        } else {
	            for (int j = 0; j < NJ; j++) {
	                min = Integer.MAX_VALUE;
	                min_type = 0;
	                for (int i = 0; i < NJ; i++) {
	                    if (S[i] == 0) {
	                        if (min > job[i].ProcT[0]) {
	                            min = (int) job[i].ProcT[0];
	                            l = i;
	                            min_type = 0;
	                        }
	                        if (min > job[i].ProcT[1]) {
	                            min = (int) job[i].ProcT[1];
	                            l = i;
	                            min_type = 1;
	                        }
	                    }
	                }
	                S[l] = 1;
	                s[j] = l;
	            }
	        }
	    }

	    static int F3_Johnson_alg_ext(T_JOB[] job, int NJ, int swtch, int[] s) {
	        int r, l = 0, k, min, min_type;
	        int[] S = new int[NJ];
	        for (int i = 0; i < NJ; i++)
	            S[i] = 0;

	        int m = NJ / 3;
	        int n = NJ - 2 * m;

	        for (r = 0; r < n; r++) {
	            min = Integer.MAX_VALUE;
	            min_type = 0;
	            for (int i = 0; i < NJ; i++) {
	                if (S[i] == 0) {
	                    if (min > job[i].ProcT[0]) {
	                        min = (int) job[i].ProcT[0];
	                        l = i;
	                        min_type = 0;
	                    }
	                    if (min > job[i].ProcT[1]) {
	                        min = (int) job[i].ProcT[1];
	                        l = i;
	                        min_type = 1;
	                    }
	                }
	            }
	            S[l] = 1;
	            s[r] = l;
	        }

	        for (r = n; r < n + m; r++) {
	            min = Integer.MAX_VALUE;
	            min_type = 0;
	            for (int i = 0; i < NJ; i++) {
	                if (S[i] == 0) {
	                    if (min > job[i].ProcT[0]) {
	                        min = (int) job[i].ProcT[0];
	                        l = i;
	                        min_type = 0;
	                    }
	                }
	            }
	            S[l] = 1;
	            s[r] = l;
	        }

	        for (r = n + m; r < NJ; r++) {
	            min = Integer.MAX_VALUE;
	            min_type = 0;
	            for (int i = 0; i < NJ; i++) {
	                if (S[i] == 0) {
	                    if (min > job[i].ProcT[1]) {
	                        min = (int) job[i].ProcT[1];
	                        l = i;
	                        min_type = 1;
	                    }
	                }
	            }
	            S[l] = 1;
	            s[r] = l;
	        }

	        return 1;
	    }
	}
	
	
}
