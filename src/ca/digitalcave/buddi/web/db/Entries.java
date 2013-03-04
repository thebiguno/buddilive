package ca.digitalcave.buddi.web.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.model.Category;
import ca.digitalcave.buddi.web.model.Entry;
import ca.digitalcave.buddi.web.model.User;


public interface Entries {
	public List<Entry> selectEntries(@Param("user") User user, @Param("category") Category category, @Param("date") Date date);
	
}
