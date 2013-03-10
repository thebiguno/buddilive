package ca.digitalcave.buddi.live.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.User;


public interface Entries {
	public List<Entry> selectEntries(@Param("user") User user);
	
	public Entry selectEntry(@Param("user") User user, @Param("id") Long id);
	public Entry selectEntry(@Param("user") User user, @Param("entry") Entry entry);	//date and category ID must be populated
	public Entry selectEntry(@Param("user") User user, @Param("date") Date date, @Param("categoryId") Integer categoryId);
	
	public Integer insertEntry(@Param("user") User user, @Param("entry") Entry entry);
	
	public Integer updateEntry(@Param("user") User user, @Param("entry") Entry entry);
}
