package ca.digitalcave.buddi.web.db;

import org.apache.ibatis.annotations.Param;


public interface Users {
	public String selectHashedPassword(@Param("email") String email);
}
