package capaModeloINV;

public class InsumoConsumidoSemana {
	
	private int idInsumo;
	private String nombre;
	private double lunes;
	private double martes;
	private double miercoles;
	private double jueves;
	private double viernes;
	private double sabado;
	private double domingo;
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
	public double getLunes() {
		return lunes;
	}
	public void setLunes(double lunes) {
		this.lunes = lunes;
	}
	public double getMartes() {
		return martes;
	}
	public void setMartes(double martes) {
		this.martes = martes;
	}
	public double getMiercoles() {
		return miercoles;
	}
	public void setMiercoles(double miercoles) {
		this.miercoles = miercoles;
	}
	public double getJueves() {
		return jueves;
	}
	public void setJueves(double jueves) {
		this.jueves = jueves;
	}
	public double getViernes() {
		return viernes;
	}
	public void setViernes(double viernes) {
		this.viernes = viernes;
	}
	public double getSabado() {
		return sabado;
	}
	public void setSabado(double sabado) {
		this.sabado = sabado;
	}
	public double getDomingo() {
		return domingo;
	}
	public void setDomingo(double domingo) {
		this.domingo = domingo;
	}
	public InsumoConsumidoSemana(int idInsumo, String nombre,double lunes, double martes,
			double miercoles, double jueves, double viernes, double sabado, double domingo) {
		super();
		this.idInsumo = idInsumo;
		this.nombre = nombre;
		this.lunes = lunes;
		this.martes = martes;
		this.miercoles = miercoles;
		this.jueves = jueves;
		this.viernes = viernes;
		this.sabado = sabado;
		this.domingo = domingo;
	}
	
	
	
}
