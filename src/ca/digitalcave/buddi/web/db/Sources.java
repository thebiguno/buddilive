package ca.digitalcave.buddi.web.db;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;


public interface Sources {
	/**
	 * Select the source by source id, for the specified user id.  Set id to 0 or null to show all.
	 * @param id
	 * @param user
	 * @return
	 */
	public List<Map<String, Object>> selectSource(@Param("user") int user, @Param("id") Long id);
	
	/**
	 * Select the source by uuid, for the specified user id
	 * @param user
	 * @param uuid
	 * @return
	 */
	public List<Map<String, Object>> selectSource(@Param("user") int user, @Param("uuid") String uuid);
	
	/**
	 * Inserts the source
	 * @param source
	 * @return
	 */
	public Long insertSource(Map<String, Object> source);
}
