package ca.digitalcave.buddi.live.db.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTimeZone;

public class DateTimeZoneHandler extends BaseTypeHandler<DateTimeZone> {
	@Override
	public DateTimeZone getNullableResult(ResultSet rs, String columnName) throws SQLException {
		final String rawValue = rs.getString(columnName);
		return DateTimeZone.forID(rawValue);
	}
	
	@Override
	public DateTimeZone getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		final String rawValue = cs.getString(columnIndex);
		return DateTimeZone.forID(rawValue);
	}
	
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, DateTimeZone parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.getID());
	}
	
	@Override
	public DateTimeZone getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		final String rawValue = rs.getString(columnIndex);
		return DateTimeZone.forID(rawValue);
	}
}
