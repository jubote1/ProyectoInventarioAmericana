package capaModeloINV;

public class ControlPorcionEmpleado {

	private int idUsuario;
	private int idTienda;
	private int idPedido;
	private int idDetallePedido;
	private String fecha;
	private String nombreEmpleado;
	
	public String getNombreEmpleado() {
		return nombreEmpleado;
	}
	public void setNombreEmpleado(String nombreEmpleado) {
		this.nombreEmpleado = nombreEmpleado;
	}
	public int getIdUsuario() {
		return idUsuario;
	}
	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public int getIdPedido() {
		return idPedido;
	}
	public void setIdPedido(int idPedido) {
		this.idPedido = idPedido;
	}
	public int getIdDetallePedido() {
		return idDetallePedido;
	}
	public void setIdDetallePedido(int idDetallePedido) {
		this.idDetallePedido = idDetallePedido;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public ControlPorcionEmpleado(int idUsuario, int idTienda, int idPedido, int idDetallePedido, String fecha) {
		super();
		this.idUsuario = idUsuario;
		this.idTienda = idTienda;
		this.idPedido = idPedido;
		this.idDetallePedido = idDetallePedido;
		this.fecha = fecha;
	}
	
}
