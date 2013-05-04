package ca.digitalcave.buddi.live.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.model.report.Pie;
import ca.digitalcave.buddi.live.model.report.Summary;


public interface Reports {
	public List<Pie> selectPieIncomeOrExpensesByCategory(@Param("user") User user, @Param("type") String type, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
	public List<Summary> selectActualIncomeAndExpensesByCategory(@Param("user") User user, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
