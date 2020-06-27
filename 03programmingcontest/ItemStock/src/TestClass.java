/* IMPORTANT: Multiple classes and nested static classes are supported */

//imports for BufferedReader
import java.io.File;
import java.util.Arrays;
//import for Scanner and other utility classes
import java.util.Scanner;
import java.util.stream.Stream;

// Warning: Printing unwanted or ill-formatted data to output will cause the test cases to fail

class TestClass {

	static int[] parent, stock, quantity;
	static boolean[] fixed;
	static int numLine;

	public static void main(String args[]) throws Exception {

		// Scanner
//        Scanner s = new Scanner(System.in);
		Scanner s = new Scanner(new File("input.txt"));
		int[] firstLineArr = parseLineToIntArray(s.nextLine());
//		System.out.println(Arrays.toString(firstLineArr));

		numLine = firstLineArr[0];
		parent = new int[numLine];
		stock = new int[numLine];
		quantity = new int[numLine];
		fixed = new boolean[numLine];

		parent[0] = 0;
		quantity[0] = 0;
		stock[0] = firstLineArr[1];
		fixed[0] = true;

		for (int i = 1; i < numLine; i++) {
			int[] lineInt = parseLineToIntArray(s.nextLine());
			parent[i] = lineInt[1] - 1; // 0-based index
			fixed[i] = lineInt[0] == 2;
			quantity[i] = lineInt[2];
			if (fixed[i]) {
				stock[i] = lineInt[3];
//				stock[parent[i]] -= stock[i] * mult[i];
				deductStockTillTop(i);
//				updateSubTreeStock(0);
				// deduct all to top
			} else {
				// calc dynamic based on top
				calcDynamicStock(i);
			}

//			System.out.println(String.format("after adding %d. stock: %s", i, Arrays.toString(stock)));
		}
		updateSubTreeStock(0);

//		System.out.println(String.format("parent: %s", Arrays.toString(parent)));
//		System.out.println(String.format("mult: %s", Arrays.toString(quantity)));
//		System.out.println(String.format("fixed: %s", Arrays.toString(fixed)));

		// print result
		Arrays.stream(stock).forEach(stk -> System.out.println(stk));
	}

	private static void calcDynamicStock(int index) {
		stock[index] = stock[parent[index]] / quantity[index];
//		System.out.println("calculating "+index+" stock become"+stock[index]);
	}

	private static int[] parseLineToIntArray(String line) {
		return Stream.of(line.split(" ")).mapToInt(Integer::parseInt).toArray();
	}
	
	private static void deductStockTillTop(int i) {
		int mult = quantity[i];
		int node = i;
		while(true) {
			node = parent[node];
			//exit loop when reach top or reach first fixed node
			if(node==0||fixed[node])
				break;
			//otherwise, continue multiplying
			mult*=quantity[node];
		}
		stock[node]-=stock[i]*mult;
//		System.out.println("deductStockTillTop node="+node+" mult="+mult);
	}

	private static void updateSubTreeStock(int index) {
		// TODO: add line to avoid for loop
		for (int i = 0; i < numLine; i++) {
			// 0 stock - not passed
			if (stock[i] == 0)
				break;
			// skip own and not match parent or
			if (i == index || index != parent[i])
				continue;
			if (!fixed[i])
				calcDynamicStock(i);
			updateSubTreeStock(i); // recursive
		}
	}

}
