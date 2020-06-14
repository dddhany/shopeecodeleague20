package shopee.orderbrushing3;

import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

/**
 * Hello world!
 *
 */
public class App {
	public static final int MAX_READ = Integer.MAX_VALUE;

	public static void main(String[] args) throws Exception {
		System.out.println("Hello World!");
		int curline = 0;

		CSVWriter cw = new CSVWriter(new FileWriter("java_output3.csv"));
		cw.writeNext(new String[] { "shopid", "userid", "event_time", "orderid", "is_order_brushing" }, false);

		CsvToBean<Transaction> cb = new CsvToBeanBuilder(new FileReader("sorted_input.csv")).withType(Transaction.class)
				.build();
		Iterator<Transaction> it = cb.iterator();
		List<Transaction> sameShopTransList = new LinkedList<>();
		int lastshop = 0;
		while (it.hasNext() && curline++ <= MAX_READ) {
			Transaction t = it.next();
//			System.out.println(t);
			if (lastshop == 0 || t.getShopId() == lastshop) { // collect all same
				sameShopTransList.add(t);
				lastshop = t.getShopId();
			} else if (t.getShopId() != lastshop) { // process when shop are diff
				processSingleShop(sameShopTransList, cw);
				sameShopTransList.clear();

				lastshop = t.getShopId();
				sameShopTransList.add(t);
			}

		}
		processSingleShop(sameShopTransList, cw); // process last set of record
		cw.close();
	}

	public static void processSingleShop(List<Transaction> transList, CSVWriter cw) throws Exception {
		System.out.println(
				String.format("Processsing shopid [%d] count [%d]", transList.get(0).getShopId(), transList.size()));
		if (transList == null || transList.size() < 3) { // ignore less than 3 char{
			writeToCW(transList, cw);
			return;
		}
		List<Transaction> subTransList = new LinkedList<>(), reverseTL = new LinkedList<>();
		for (int i = 0; i < transList.size(); i++) {
			Transaction t = transList.get(i);
			LocalDateTime ldtStart = t.getEventTime();
			subTransList.clear();
			subTransList.add(t);
			for (int j = i + 1; j < transList.size(); j++) {
				if (ldtStart.until(transList.get(j).getEventTime(), ChronoUnit.MINUTES) < 60) { // within 1h
					subTransList.add(transList.get(j));
				} else {
					break;
				}
			}
			checkAndMarkBrushBasedOnUniqueBuyer(subTransList);
			
			//forward, exclude current
			subTransList.clear();
			for (int j = i + 1; j < transList.size(); j++) {
				if (ldtStart.plusSeconds(1).until(transList.get(j).getEventTime(), ChronoUnit.MINUTES) < 60) { // within 1h
					subTransList.add(transList.get(j));
				} else {
					break;
				}
			}
			checkAndMarkBrushBasedOnUniqueBuyer(subTransList);

			// reverse look
			reverseTL.clear();
			reverseTL.add(t);
			for (int j = i - 1; j>=0; j--) {
				if (transList.get(j).getEventTime().until(ldtStart, ChronoUnit.MINUTES) < 60) { // within 1h
					reverseTL.add(transList.get(j));
				} else {
					break;
				}
			}
			checkAndMarkBrushBasedOnUniqueBuyer(reverseTL);
			
			//reverse, exclude current
			reverseTL.clear();
			for (int j = i - 1; j>=0; j--) {
				if (transList.get(j).getEventTime().until(ldtStart.minusSeconds(1), ChronoUnit.MINUTES) < 60) { // within 1h
					reverseTL.add(transList.get(j));
				} else {
					break;
				}
			}
			checkAndMarkBrushBasedOnUniqueBuyer(reverseTL);
		}
		writeToCW(transList, cw);
	}

	public static void writeToCW(List<Transaction> transList, CSVWriter cw) throws Exception {
		transList.forEach(t -> {
			cw.writeNext(new String[] { "" + t.getShopId(), "" + t.getUserId(),
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(t.getEventTime()), "" + t.getOrderId(),
					"" + (t.isBrush() ? "Y" : "N") }, false);
		});
	}

	public static void checkAndMarkBrushBasedOnUniqueBuyer(List<Transaction> transList) {
		// skip all small trans and all marked trans
		if (transList.size() < 3 || isAllMarked(transList)) {
			return;
		}
		int uniqueBuyerCount = countUniqueBuyer(transList);
		double mark = transList.size() * 1.0 / uniqueBuyerCount;
//		System.out.println(String.format("checknmark shopid=%d from ts=%s size=%d uniquebuyer=%d score=%.1f", transList.get(0).getShopId(), transList.get(0).getEventTime(),
//				transList.size(), uniqueBuyerCount, mark));
		if (mark >= 3.0) {
			transList.forEach(t -> t.setBrush(true));
			System.out.println(String.format("marked trans size=%d shopid=%d startdt=%s", transList.size(),
					transList.get(0).getShopId(), transList.get(0).getEventTime()));
		}
	}

	public static boolean isAllMarked(List<Transaction> transList) {
		for (Transaction t : transList) {
			if (!t.isBrush())
				return false;
		}
		return true;
	}

	public static int countUniqueBuyer(List<Transaction> transList) {
		final Set<Integer> count1 = new HashSet<>();
		transList.forEach(t -> {
			count1.add(t.getUserId());
		});
		return count1.size();
	}
}
