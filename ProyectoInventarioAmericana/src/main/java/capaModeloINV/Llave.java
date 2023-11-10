package capaModeloINV;

public class Llave {
	
		private int idLlave;
		private String nombre;
		private int idTienda;
		private boolean disponible;
		
		public boolean isDisponible() {
			return disponible;
		}
		public void setDisponible(boolean disponible) {
			this.disponible = disponible;
		}
		public int getIdLlave() {
			return idLlave;
		}
		public void setIdLlave(int idLlave) {
			this.idLlave = idLlave;
		}
		public String getNombre() {
			return nombre;
		}
		public void setNombre(String nombre) {
			this.nombre = nombre;
		}
		
		
		public int getIdTienda() {
			return idTienda;
		}
		public void setIdTienda(int idTienda) {
			this.idTienda = idTienda;
		}

		
		public Llave(int idLlave, String nombre, int idTienda) {
			super();
			this.idLlave = idLlave;
			this.nombre = nombre;
			this.idTienda = idTienda;
		}
		@Override
		public String toString() {
			return nombre;
		}
		
		
		
		

}
