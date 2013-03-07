package ca.digitalcave.buddi.live.db;

import java.util.Date;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.User;


public interface Entries {
	//public List<Entry> selectEntries(@Param("user") User user, @Param("category") Category category, @Param("date") Date date);
	public Entry selectEntry(@Param("user") User user, @Param("id") Long id);
	public Entry selectEntry(@Param("user") User user, @Param("date") Date date, @Param("categoryId") Integer categoryId);
	
	public Integer insertEntry(@Param("entry") Entry entry);
	
	public Integer updateEntry(@Param("entry") Entry entry);
}
