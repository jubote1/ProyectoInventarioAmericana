package capaModeloINV;

public class LlaveEvento {
	
	private int idEvento;
	private int idLlave;
	private int idUsuarioRegistro;
	private int idUsuarioPrestamo;
	private String observacion;
	private String fecha;
	private String usuarioRegistro;
	private String usuarioPrestamo;
	private String evento;
	
	
	
	public String getEvento() {
		return evento;
	}
	public void setEvento(String evento) {
		this.evento = evento;
	}
	public int getIdEvento() {
		return idEvento;
	}
	public void setIdEvento(int idEvento) {
		this.idEvento = idEvento;
	}
	public int getIdLlave() {
		return idLlave;
	}
	public void setIdLlave(int idLlave) {
		this.idLlave = idLlave;
	}
	public int getIdUsuarioRegistro() {
		return idUsuarioRegistro;
	}
	public void setIdUsuarioRegistro(int idUsuarioRegistro) {
		this.idUsuarioRegistro = idUsuarioRegistro;
	}
	public int getIdUsuarioPrestamo() {
		return idUsuarioPrestamo;
	}
	public void setIdUsuarioPrestamo(int idUsuarioPrestamo) {
		this.idUsuarioPrestamo = idUsuarioPrestamo;
	}
	public String getObservacion() {
		return observacion;
	}
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getUsuarioRegistro() {
		return usuarioRegistro;
	}
	public void setUsuarioRegistro(String usuarioRegistro) {
		this.usuarioRegistro = usuarioRegistro;
	}
	public String getUsuarioPrestamo() {
		return usuarioPrestamo;
	}
	public void setUsuarioPrestamo(String usuarioPrestamo) {
		this.usuarioPrestamo = usuarioPrestamo;
	}
	public LlaveEvento(int idLlave, int idUsuarioRegistro, int idUsuarioPrestamo, String observacion) {
		super();
		this.idLlave = idLlave;
		this.idUsuarioRegistro = idUsuarioRegistro;
		this.idUsuarioPrestamo = idUsuarioPrestamo;
		this.observacion = observacion;
	}
	public LlaveEvento() {
		super();
	}
	
	
	
	

}
