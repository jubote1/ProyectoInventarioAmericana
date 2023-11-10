package capaModeloINV;

public class CapacitacionEmpleado {

	private int idCapacitacionEmp;
	private int idEmpleado;
	private String fecha;
	private String horaInicio;
	private String horaFinal;
	private String observacion;
	private String nombreLargo;
	
	
	
	public String getNombreLargo() {
		return nombreLargo;
	}
	public void setNombreLargo(String nombreLargo) {
		this.nombreLargo = nombreLargo;
	}
	public int getIdCapacitacionEmp() {
		return idCapacitacionEmp;
	}
	public void setIdCapacitacionEmp(int idCapacitacionEmp) {
		this.idCapacitacionEmp = idCapacitacionEmp;
	}
	public int getIdEmpleado() {
		return idEmpleado;
	}
	public void setIdEmpleado(int idEmpleado) {
		this.idEmpleado = idEmpleado;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getHoraInicio() {
		return horaInicio;
	}
	public void setHoraInicio(String horaInicio) {
		this.horaInicio = horaInicio;
	}
	public String getHoraFinal() {
		return horaFinal;
	}
	public void setHoraFinal(String horaFinal) {
		this.horaFinal = horaFinal;
	}
	public String getObservacion() {
		return observacion;
	}
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	public CapacitacionEmpleado(int idCapacitacionEmp, int idEmpleado, String fecha, String horaInicio,
			String horaFinal, String observacion, String nombreLargo) {
		super();
		this.idCapacitacionEmp = idCapacitacionEmp;
		this.idEmpleado = idEmpleado;
		this.fecha = fecha;
		this.horaInicio = horaInicio;
		this.horaFinal = horaFinal;
		this.observacion = observacion;
		this.nombreLargo = nombreLargo;
	}

	
	
	
	
}
