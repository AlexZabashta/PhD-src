package tasks.linear;

public interface LRSolution {
	public double[] solve(int n, int m, double[][] x, double[] y);

	public default double[] solve(int n, int m, int[][] xy) {
		double[] mu = new double[m + 1];
		double[] sigma = new double[m + 1];
		double[][] data = Solution.normalize(n, m, xy, mu, sigma);
		double[] y = Solution.replaceTarget(n, m, data);
		double[] a = solve(n, m + 1, data, y);
		Solution.denormalize(n, m, mu, sigma, a);
		return a;
	}
}
