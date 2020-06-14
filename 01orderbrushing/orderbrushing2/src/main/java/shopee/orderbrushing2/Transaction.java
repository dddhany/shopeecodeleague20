package shopee.orderbrushing2;

import java.time.LocalDateTime;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import lombok.Data;

@Data
public class Transaction {
	@CsvBindByName
	private int shopId;
	@CsvBindByName
	private int userId;
	@CsvBindByName(column = "event_time")
	@CsvDate(value="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime eventTime;
	@CsvBindByName
	private long orderId;
	@CsvBindByName(column = "is_order_brushing")
	private char isOrderBrushing;
	private boolean brush;
	
	private void setBrush(boolean brush) {
		this.brush = brush;
	}
	
	public boolean isBrush() {
		return isOrderBrushing=='Y';
	}
}
