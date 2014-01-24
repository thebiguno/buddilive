package ca.digitalcave.buddi.live.db;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.User;


public interface Entries {
	public List<Entry> selectEntries(@Param("user") User user);
	
	@MapKey("categoryId")
	public Map<Integer, Entry> selectEntries(@Param("user") User user, @Param("date") Date date);
	
	@MapKey("date")
	public Map<Date, Entry> selectEntries(@Param("user") User user, @Param("categoryId") Integer categoryId);
	
	public Entry selectEntry(@Param("user") User user, @Param("id") Long id);
	public Entry selectEntry(@Param("user") User user, @Param("entry") Entry entry);	//date and category ID must be populated
	public Entry selectEntry(@Param("user") User user, @Param("date") Date date, @Param("categoryId") Integer categoryId);
	
	public Integer insertEntry(@Param("user") User user, @Param("entry") Entry entry);
	
	public Integer updateEntry(@Param("user") User user, @Param("entry") Entry entry);
	
//	public Integer copyFromPreviousPeriod(@Param("user") User user, @Param("previousDate") Date previousDate, @Param("currentDate") Date currentDate);
}
