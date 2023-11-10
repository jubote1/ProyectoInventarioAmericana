package capaModeloINV;

public class DesechoTienda {
	
	private int idDesechoTienda;
	private int numeroDesecho;
	private int idTienda;
	private String fecha;
	private String descripcion;
	private String motivo;
	private int idDesecho;
	private String descripcionDesecho;
	private double gramos;
	private double costo;
	private double cantidad;
	private String usuario;
	
	
	
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public double getCosto() {
		return costo;
	}
	public void setCosto(double costo) {
		this.costo = costo;
	}
	public String getDescripcionDesecho() {
		return descripcionDesecho;
	}
	public void setDescripcionDesecho(String descripcionDesecho) {
		this.descripcionDesecho = descripcionDesecho;
	}
	public int getIdDesechoTienda() {
		return idDesechoTienda;
	}
	public void setIdDesechoTienda(int idDesechoTienda) {
		this.idDesechoTienda = idDesechoTienda;
	}
	public int getNumeroDesecho() {
		return numeroDesecho;
	}
	public void setNumeroDesecho(int numeroDesecho) {
		this.numeroDesecho = numeroDesecho;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getMotivo() {
		return motivo;
	}
	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
	public int getIdDesecho() {
		return idDesecho;
	}
	public void setIdDesecho(int idDesecho) {
		this.idDesecho = idDesecho;
	}
	public double getGramos() {
		return gramos;
	}
	public void setGramos(double gramos) {
		this.gramos = gramos;
	}
	public DesechoTienda(int idDesechoTienda, int numeroDesecho, int idTienda, String fecha, String descripcion,
			String motivo, int idDesecho, double gramos, double cantidad, String usuario) {
		super();
		this.idDesechoTienda = idDesechoTienda;
		this.numeroDesecho = numeroDesecho;
		this.idTienda = idTienda;
		this.fecha = fecha;
		this.descripcion = descripcion;
		this.motivo = motivo;
		this.idDesecho = idDesecho;
		this.gramos = gramos;
		this.cantidad = cantidad;
		this.usuario = usuario;
	}
	
	

}
