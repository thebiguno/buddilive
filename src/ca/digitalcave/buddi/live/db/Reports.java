package ca.digitalcave.buddi.live.db;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.model.report.Pie;


public interface Reports {
	public List<Pie> selectPieIncomeOrExpensesByCategory(@Param("user") User user, @Param("type") String type, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@MapKey("id")
	public Map<Integer, Map<String, Object>> selectActualByCategory(@Param("user") User user, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
