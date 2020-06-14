package shopee.orderbrushing2;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

/**
 * Hello world!
 *
 */
public class App {
	
	public static final int MAX_READ = Integer.MAX_VALUE;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {
		System.out.println("Hello World!");
		int curline = 0;
		
		CsvToBean<Transaction> cb = new CsvToBeanBuilder(new FileReader("input_brushed3.csv")).withType(Transaction.class).build();
		Iterator<Transaction> it = cb.iterator();
		Map<Integer, List<Integer>> resultMap = new TreeMap<>();
		while(it.hasNext()&& curline++<=MAX_READ) {
			Transaction t = it.next();
//			System.out.println(t);
			List<Integer> userList = null;
			if(!resultMap.containsKey(t.getShopId())) {
				userList = new LinkedList<>();
				resultMap.put(t.getShopId(), userList);
			} else {
				userList = resultMap.get(t.getShopId());
			}
			if(t.isBrush()) {
				userList.add(t.getUserId());
			}
		}
		System.out.println(resultMap);
		Map<Integer, String> resultMap2 = new TreeMap<>();
		resultMap.entrySet().forEach(me -> {
			resultMap2.put(me.getKey(), findHighestUserId(me.getValue()));
		});
		System.out.println(resultMap2);
		
		CSVWriter cw = new CSVWriter(new FileWriter("java_output3.csv"));
		cw.writeNext(new String[] {"shopid","userid"}, false);
		resultMap2.forEach((k,v)->{
			String[] r = new String[2];
			r[0] = String.valueOf(k);
			r[1] = v;
			cw.writeNext(r, false);
		});
		cw.close();
	}
	
	private static String findHighestUserId(List<Integer> userList) {
		if(userList==null || userList.isEmpty())
			return "0";
//		System.out.println(userList);
		Map<Integer, Integer> userCount = new HashMap<>();
		userList.forEach(u -> {
			if(!userCount.containsKey(u))
				userCount.put(u, 1);
			else
				userCount.put(u, userCount.get(u)+1);
			
		});
//		System.out.println(userCount);
		int max = 0;
		List<Integer> highestUser = new LinkedList<>();
		for(Map.Entry<Integer, Integer> me: userCount.entrySet()) {
			if(me.getValue()>max) { // higher
				highestUser.clear();
				highestUser.add(me.getKey());
				max=me.getValue();
			} else if(me.getValue()==max) {
				highestUser.add(me.getKey());
			}
		}
		Collections.sort(highestUser);
		StringBuilder sb= new StringBuilder();
		highestUser.forEach(u -> {
			if(sb.length()>0) {
				sb.append('&');
			}
			sb.append(u);
		});
		return sb.toString();
	}
}
