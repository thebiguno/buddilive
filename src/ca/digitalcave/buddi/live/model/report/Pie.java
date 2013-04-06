package ca.digitalcave.buddi.live.model.report;

import java.math.BigDecimal;

public class Pie {
	private String label;
	private BigDecimal amount;
	
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}
