package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.model.Source;


public interface Sources {
	/**
	 * Select the source by source id, for the specified user id.  Set id to 0 or null to show all.
	 * @param id
	 * @param user
	 * @return
	 */
	public List<Source> selectSource(@Param("user") int user, @Param("id") Long id);
	
	/**
	 * Select the source by uuid, for the specified user id
	 * @param user
	 * @param id
	 * @return
	 */
	public List<Source> selectSource(@Param("user") int user, @Param("id") String uuid);
	
	/**
	 * Inserts the source
	 * @param source
	 * @return
	 */
	public Integer insertSource(Source source);
}
