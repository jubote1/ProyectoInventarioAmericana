package capaModeloINV;

public class InsumoConsumido {
	
	private int idInsumo;
	private String nombre;
	private double cantidad;
	private String fecha;
	private String dia;
	
	public int getIdInsumo() {
		return idInsumo;
	}
	public void setIdInsumo(int idInsumo) {
		this.idInsumo = idInsumo;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getDia() {
		return dia;
	}
	public void setDia(String dia) {
		this.dia = dia;
	}
	public InsumoConsumido(int idInsumo, String nombre, double cantidad, String fecha, String dia) {
		super();
		this.idInsumo = idInsumo;
		this.nombre = nombre;
		this.cantidad = cantidad;
		this.fecha = fecha;
		this.dia = dia;
	}

}
