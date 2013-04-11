package ca.digitalcave.buddi.live.db.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class CurrencyHandler extends BaseTypeHandler<Currency> {
	@Override
	public Currency getNullableResult(ResultSet rs, String columnName) throws SQLException {
		final String rawValue = rs.getString(columnName);
		return Currency.getInstance(rawValue);
	}
	
	@Override
	public Currency getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		final String rawValue = cs.getString(columnIndex);
		return Currency.getInstance(rawValue);
	}
	
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Currency parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.getCurrencyCode());
	}
	
	@Override
	public Currency getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		final String rawValue = rs.getString(columnIndex);
		return Currency.getInstance(rawValue);
	}
}
