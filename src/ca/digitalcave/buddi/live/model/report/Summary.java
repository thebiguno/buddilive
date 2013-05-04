package ca.digitalcave.buddi.live.model.report;

import java.math.BigDecimal;

import ca.digitalcave.buddi.live.model.Source;

public class Summary {
	private Source source;
	private BigDecimal actual;
	private BigDecimal budgeted;

	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
	}
	public BigDecimal getActual() {
		return actual;
	}
	public void setActual(BigDecimal actual) {
		this.actual = actual;
	}
	public BigDecimal getBudgeted() {
		return budgeted;
	}
	public void setBudgeted(BigDecimal budgeted) {
		this.budgeted = budgeted;
	}
	public BigDecimal getDifference() {
		return budgeted.subtract(actual);
	}
}
