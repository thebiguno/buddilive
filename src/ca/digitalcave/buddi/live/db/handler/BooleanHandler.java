package ca.digitalcave.buddi.live.db.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class BooleanHandler extends BaseTypeHandler<Boolean> {
	@Override
	public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException {
		final String rawValue = rs.getString(columnName);
		return "Y".equals(rawValue);
	}
	
	@Override
	public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		final String rawValue = cs.getString(columnIndex);
		return "Y".equals(rawValue);
	}
	
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter ? "Y" : "N");
	}
	
	@Override
	public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		final String rawValue = rs.getString(columnIndex);
		return "Y".equals(rawValue);
	}
}
