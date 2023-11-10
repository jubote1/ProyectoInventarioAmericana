package capaModeloINV;

public class VarianzaDiferencia {
	
	private int idInsumo;
	private double cantidad;
	public int getIdInsumo() {
		return idInsumo;
	}
	public void setIdInsumo(int idInsumo) {
		this.idInsumo = idInsumo;
	}
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	public VarianzaDiferencia(int idInsumo, double cantidad) {
		super();
		this.idInsumo = idInsumo;
		this.cantidad = cantidad;
	}
	
	

}
