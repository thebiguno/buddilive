package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.db.dao.DataConstraintException;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.User;


public interface Sources {
	public Source selectSource(@Param("user") User user, @Param("id") int id);
	public Source selectSource(@Param("user") User user, @Param("uuid") String uuid);
	public List<Source> selectSources(@Param("user") User user);
	public Integer insertSource(@Param("user") User user, @Param("source") Source source) throws DataConstraintException;
}
