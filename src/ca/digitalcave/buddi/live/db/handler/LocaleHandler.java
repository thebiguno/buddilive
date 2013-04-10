package ca.digitalcave.buddi.live.db.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class LocaleHandler implements TypeHandler<Locale> {
	@Override
	public Locale getResult(ResultSet rs, String columnName) throws SQLException {
		final String rawValue = rs.getString(columnName);
		return LocaleUtils.toLocale(rawValue);
	}
	
	@Override
	public Locale getResult(CallableStatement cs, int columnIndex) throws SQLException {
		final String rawValue = cs.getString(columnIndex);
		return LocaleUtils.toLocale(rawValue);
	}
	
	@Override
	public void setParameter(PreparedStatement ps, int i, Locale parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.toString());
	}
	
	@Override
	public Locale getResult(ResultSet rs, int columnIndex) throws SQLException {
		final String rawValue = rs.getString(columnIndex);
		return LocaleUtils.toLocale(rawValue);
	}
}
