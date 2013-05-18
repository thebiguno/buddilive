package ca.digitalcave.buddi.live.model.report;

import java.math.BigDecimal;

import ca.digitalcave.buddi.live.model.Category;

public class ActualByCategory {
	private Category category;
	private BigDecimal actual;

	public Category getCategory() {
		return category;
	}
	public void setCategoty(Category category) {
		this.category = category;
	}
	public BigDecimal getActual() {
		return actual;
	}
	public void setActual(BigDecimal actual) {
		this.actual = actual;
	}
}
