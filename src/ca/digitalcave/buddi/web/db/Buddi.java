package ca.digitalcave.buddi.web.db;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface Buddi {
	public List<Map<String, Object>> selectAccounts(@Param("id") Integer id);
}
