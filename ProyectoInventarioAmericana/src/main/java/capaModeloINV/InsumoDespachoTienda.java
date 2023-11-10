package capaModeloINV;

public class InsumoDespachoTienda {
	
	
	private int idDespacho;
	private int idTienda;
	private String tienda;
	private String fechaDespacho;
	private String fechaReal;
	private String estado;
	private String observacion;
	private String novedadTienda;
	private String diferencia;
	private String marcado;
	
	
	public String getMarcado() {
		return marcado;
	}
	public void setMarcado(String marcado) {
		this.marcado = marcado;
	}
	public String getNovedadTienda() {
		return novedadTienda;
	}
	public void setNovedadTienda(String novedadTienda) {
		this.novedadTienda = novedadTienda;
	}
	public String getDiferencia() {
		return diferencia;
	}
	public void setDiferencia(String diferencia) {
		this.diferencia = diferencia;
	}
	public String getTienda() {
		return tienda;
	}
	public void setTienda(String tienda) {
		this.tienda = tienda;
	}
	public String getObservacion() {
		return observacion;
	}
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	public int getIdDespacho() {
		return idDespacho;
	}
	public void setIdDespacho(int idDespacho) {
		this.idDespacho = idDespacho;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public String getFechaDespacho() {
		return fechaDespacho;
	}
	public void setFechaDespacho(String fechaDespacho) {
		this.fechaDespacho = fechaDespacho;
	}
	public String getFechaReal() {
		return fechaReal;
	}
	public void setFechaReal(String fechaReal) {
		this.fechaReal = fechaReal;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public InsumoDespachoTienda(int idDespacho, int idTienda, String fechaDespacho, String fechaReal, String estado, String observacion) {
		super();
		this.idDespacho = idDespacho;
		this.idTienda = idTienda;
		this.fechaDespacho = fechaDespacho;
		this.fechaReal = fechaReal;
		this.estado = estado;
		this.observacion = observacion;
	}
	
	
	
	

}
