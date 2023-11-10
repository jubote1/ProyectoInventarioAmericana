package capaModeloINV;

public class Desecho {
	
	private int idDesecho;
	private String descripcion;
	private String tipo;
	private double costo;
	
	public double getCosto() {
		return costo;
	}
	public void setCosto(double costo) {
		this.costo = costo;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public int getIdDesecho() {
		return idDesecho;
	}
	public void setIdDesecho(int idDesecho) {
		this.idDesecho = idDesecho;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public Desecho(int idDesecho, String descripcion, String tipo, double costo) {
		super();
		this.idDesecho = idDesecho;
		this.descripcion = descripcion;
		this.tipo = tipo;
		this.costo = costo;
	}
}
