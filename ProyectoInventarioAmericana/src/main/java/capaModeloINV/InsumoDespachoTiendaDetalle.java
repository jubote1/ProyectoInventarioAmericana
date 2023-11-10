package capaModeloINV;

public class InsumoDespachoTiendaDetalle {
	
	private int idDespachoDetalle;
	private int idDespacho;
	private int idInsumo;
	private String nombreInsumo;
	private double cantidad;
	private String contenedor;
	private String lote;
	private int estado;
	private String color;
	
	
	
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getLote() {
		return lote;
	}
	public void setLote(String lote) {
		this.lote = lote;
	}
	public int getEstado() {
		return estado;
	}
	public void setEstado(int estado) {
		this.estado = estado;
	}
	public String getNombreInsumo() {
		return nombreInsumo;
	}
	public void setNombreInsumo(String nombreInsumo) {
		this.nombreInsumo = nombreInsumo;
	}
	public String getContenedor() {
		return contenedor;
	}
	public void setContenedor(String contenedor) {
		this.contenedor = contenedor;
	}
	public int getIdDespachoDetalle() {
		return idDespachoDetalle;
	}
	public void setIdDespachoDetalle(int idDespachoDetalle) {
		this.idDespachoDetalle = idDespachoDetalle;
	}
	public int getIdDespacho() {
		return idDespacho;
	}
	public void setIdDespacho(int idDespacho) {
		this.idDespacho = idDespacho;
	}
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
	public InsumoDespachoTiendaDetalle(int idDespachoDetalle, int idDespacho, int idInsumo, double cantidad, String contenedor) {
		super();
		this.idDespachoDetalle = idDespachoDetalle;
		this.idDespacho = idDespacho;
		this.idInsumo = idInsumo;
		this.cantidad = cantidad;
		this.contenedor = contenedor;
	}
	
	

}
