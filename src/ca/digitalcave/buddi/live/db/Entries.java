package ca.digitalcave.buddi.live.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.User;


public interface Entries {
	public List<Entry> selectEntries(@Param("user") User user, @Param("category") Category category, @Param("date") Date date);
	
}
