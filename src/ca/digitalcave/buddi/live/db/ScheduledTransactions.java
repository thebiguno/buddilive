package ca.digitalcave.buddi.live.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.ScheduledTransaction;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.User;


public interface ScheduledTransactions {

	/**
	 * Returns all scheduled transactions who meet the following criteria: 
	 * 
	 * 1) Start date is before today
	 * 2) Last date created is before today (or is not yet set).
	 * 3) End date is after today (or is not defined)
	 * 
	 * This is used at when determining which scheduled transactions we need to check and (possibly) add.
	 */
	public List<ScheduledTransaction> selectOustandingScheduledTransactions(@Param("user") User user);
	
	public List<ScheduledTransaction> selectScheduledTransactions(@Param("user") User user);
	
	public int selectScheduledTransactionCount(@Param("user") User user, @Param("uuid") String uuid);
	
	public Integer insertScheduledTransaction(@Param("user") User user, @Param("transaction") ScheduledTransaction transaction);
	public Integer insertScheduledSplit(@Param("user") User user, @Param("split") Split split);
	
	public Integer updateScheduledTransaction(@Param("user") User user, @Param("transaction") ScheduledTransaction transaction);
	public Integer updateScheduledSplit(@Param("user") User user, @Param("split") Split split);
	
	public Integer deleteScheduledTransaction(@Param("user") User user, @Param("transaction") ScheduledTransaction transaction);
	public Integer deleteScheduledSplit(@Param("user") User user, @Param("split") Split split);
}
