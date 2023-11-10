package capaModeloINV;

public class Parametro {
	
	private String valorParametro;
	private int valorNumerico;
	private String valorTexto;
	
	public String getValorParametro() {
		return valorParametro;
	}
	public void setValorParametro(String valorParametro) {
		this.valorParametro = valorParametro;
	}
	public int getValorNumerico() {
		return valorNumerico;
	}
	public void setValorNumerico(int valorNumerico) {
		this.valorNumerico = valorNumerico;
	}
	public String getValorTexto() {
		return valorTexto;
	}
	public void setValorTexto(String valorTexto) {
		this.valorTexto = valorTexto;
	}
	public Parametro(String valorParametro, int valorNumerico, String valorTexto) {
		super();
		this.valorParametro = valorParametro;
		this.valorNumerico = valorNumerico;
		this.valorTexto = valorTexto;
	}
	
	

}
