package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import capaControladorINV.InventarioCtrl;
import capaModeloINV.Insumo;
/**
 * Servlet implementation class CRUDEspecialidad
 * Servicio que encarga de implementar los servicios CRUD para la entidad Especialidad.
 */
@WebServlet("/CRUDInsumo")
public class CRUDInsumo extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CRUDInsumo() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * Este servicio recibe un idoperaci�n que puede ser 1 insertar 2 editar 3 Eliminar  4 Consultar
	 * dependiendo el valor de idoperacion, se recibir�n los diferentes par�metros de la entidad especialidad.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			//Operaci�n idoperacion 1 insertar 2 editar 3 Eliminar  4 Consultar
		response.addHeader("Access-Control-Allow-Origin", "*");	
		Logger logger = Logger.getLogger("log_file");
			HttpSession sesion = request.getSession();
			response.addHeader("Access-Control-Allow-Origin", "*");
			String idoperacion = request.getParameter("idoperacion");
			InventarioCtrl invCtrl = new InventarioCtrl();
			int operacion;
			String respuesta="";
			try
			{
				operacion = Integer.parseInt(idoperacion);
			}catch(Exception e){
				operacion = 0;
			}
			if (operacion ==1)
			{
				String nombre = request.getParameter("nombreinsumo");
				String unidadMedida = request.getParameter("unidadmedida");
				double precioUnidad = 0;
				try
				{
					precioUnidad = Double.parseDouble(request.getParameter("preciounidad"));
				}catch(Exception e)
				{
					precioUnidad = 0;
				}
				String manejaCanasta = request.getParameter("manejacanasta");
				int cantidadCanasta = 0;
				try
				{
					cantidadCanasta = Integer.parseInt(request.getParameter("cantidadcanasta"));
				}catch(Exception e)
				{
					cantidadCanasta = 0;
				}
				String nombreContenedor = request.getParameter("nombrecontenedor");
				String categoria = request.getParameter("categoria");
				boolean controlCantidad = false;
				String strControlCantidad = request.getParameter("controlcantidad");
				if(strControlCantidad.equals(new String("S")))
				{
					controlCantidad = true;
				}else
				{
					controlCantidad = false;
				}
				//En el alta se permite dejar el costo vacio: crear el insumo y
				//cargarle el costo despues es una forma de trabajar que se usa
				//-hay insumos con retiros en cero de sus primeras semanas, justo
				//por eso-. Lo que no se permite es un costo escrito que no se
				//entienda, que es otra cosa.
				Double costoLeido = leerCosto(request.getParameter("costounidad"), true);
				if(costoLeido == null)
				{
					PrintWriter outMal = response.getWriter();
					outMal.write("{\"resultado\":\"COSTOMALO\"}");
					return;
				}
				double costoUnidad = costoLeido.doubleValue();
				String controlTienda = request.getParameter("controltienda");
				Insumo insumo = new Insumo(0,nombre, unidadMedida,precioUnidad,manejaCanasta,cantidadCanasta,nombreContenedor,categoria, controlCantidad,costoUnidad, controlTienda);
				respuesta = invCtrl.insertarInsumo(insumo);
			}else if (operacion ==2)
			{
				int idInsumoEdit = Integer.parseInt(request.getParameter("idinsumo"));
				String nombre = request.getParameter("nombreinsumo");
				String unidadMedida = request.getParameter("unidadmedida");
				double precioUnidad = 0;
				try
				{
					precioUnidad = Double.parseDouble(request.getParameter("preciounidad"));
				}catch(Exception e)
				{
					precioUnidad = 0;
				}
				String manejaCanasta = request.getParameter("manejacanastas");
				int cantidadCanasta = 0;
				try
				{
					cantidadCanasta = Integer.parseInt(request.getParameter("cantidadcanasta"));
				}catch(Exception e)
				{
					cantidadCanasta = 0;
				}
				String nombreContenedor = request.getParameter("nombrecontenedor");
				if(nombreContenedor.equals(new String("null")))
				{
					nombreContenedor = "";
				}
				String categoria = request.getParameter("categoria");
				boolean controlCantidad = false;
				String strControlCantidad = request.getParameter("controlcantidad");
				if(strControlCantidad.equals(new String("1")))
				{
					controlCantidad = true;
				}else
				{
					controlCantidad = false;
				}
				//Aca es donde mas dolia: editarInsumo, ademas de guardar el
				//maestro, reescribe retiro_inventario_detalle.precio de TODA la
				//semana en curso. Un costo que quedara en cero se llevaba por
				//delante los retiros de la semana, sin decir nada.
				//Aca el vacio NO se acepta: borrar el campo sin querer no puede
				//terminar en un costo cero que se lleve por delante la semana.
				Double costoLeidoEdit = leerCosto(request.getParameter("costounidad"), false);
				if(costoLeidoEdit == null)
				{
					PrintWriter outMal = response.getWriter();
					outMal.write("{\"resultado\":\"COSTOMALO\"}");
					return;
				}
				double costoUnidad = costoLeidoEdit.doubleValue();
				String controlTienda = request.getParameter("controltienda");
				Insumo insumoEdit = new Insumo(idInsumoEdit,nombre, unidadMedida,precioUnidad,manejaCanasta,cantidadCanasta,nombreContenedor,categoria, controlCantidad,costoUnidad, controlTienda);
				respuesta = invCtrl.editarInsumo(insumoEdit);
			}else if (operacion ==3 )
			{
				//No existe acci�n para la eliminaci�n
			}else if (operacion == 4)
			{
				int idInsumoCon = Integer.parseInt(request.getParameter("idinsumo"));
				respuesta = invCtrl.retornarInsumo(idInsumoCon);
			}else if (operacion == 5)
			{
				//Consultar todos los insumos
				respuesta = invCtrl.retornarInsumos();
			}else if (operacion == 6)
			{
				//Consultar todos los insumos Grid
				respuesta = invCtrl.retornarInsumosGrid();
			}
			System.out.println(respuesta);
			PrintWriter out = response.getWriter();
			out.write(respuesta);
			
		
	}

	/**
	 * Lee el costo que manda la pantalla.
	 *
	 * Antes esto era Integer.parseInt dentro de un try que en el catch dejaba
	 * el costo en CERO. O sea que escribir "1377,5" o "20.200" no daba error:
	 * guardaba el insumo con costo cero y nadie se enteraba. Y no era solo el
	 * maestro: editarInsumo tambien reescribe retiro_inventario_detalle.precio
	 * de toda la semana en curso para ese insumo, asi que los retiros de la
	 * semana quedaban en cero. Un insumo en cero desaparece de todo informe
	 * costeado -varianza, desechos, venta integral- sin dejar rastro.
	 *
	 * Ahora devuelve null cuando no se entiende, y el que llama no guarda
	 * nada. Es preferible que la pantalla diga "revise el costo" a que el dato
	 * se pierda en silencio.
	 *
	 * Sobre los separadores: se usa la convencion de aca, punto para los miles
	 * y coma para los decimales, que es la misma que ya se uso en el central
	 * para los valores de conciliacion. Asi "20.200" son veinte mil doscientos
	 * y "1377,5" son mil trescientos setenta y siete con cinco. Escribir
	 * "1377.5" con punto decimal daria 13.775, pero es una forma que aca no se
	 * usa y que el codigo anterior tampoco aceptaba.
	 *
	 * @param permitirVacio true en el alta, donde dejar el costo en blanco y
	 *        cargarlo despues es valido; false en la edicion, donde borrar el
	 *        campo sin querer no puede terminar en un costo cero
	 * @return el costo, o null si no se puede leer
	 */
	private Double leerCosto(String valor, boolean permitirVacio)
	{
		if(valor == null || valor.trim().length() == 0)
		{
			return(permitirVacio ? Double.valueOf(0) : null);
		}
		//Un signo de pesos o un espacio pegado no deberian tumbar el guardado.
		String limpio = valor.trim().replace("$", "").replace(" ", "");

		if(limpio.indexOf(',') >= 0)
		{
			//Hay coma: entonces la coma es el decimal y los puntos son miles.
			limpio = limpio.replace(".", "").replace(',', '.');
		}
		else
		{
			//No hay coma. Un punto solo es ambiguo: "20.200" son veinte mil
			//doscientos y "1377.5" son mil trescientos setenta y siete con
			//cinco. Se decide por cuantos digitos quedan despues del punto,
			//que es como se escribe de verdad: tres digitos son miles, uno o
			//dos son decimales.
			//
			//Importa afinar esto y no barrer el punto siempre: con la regla
			//simple, escribir "1377.5" guardaba 13.775, diez veces el costo
			//real. Un cero se ve; un costo diez veces mayor pasa de largo y
			//desajusta el costeo de la varianza sin que nadie lo note.
			int primerPunto = limpio.indexOf('.');
			int ultimoPunto = limpio.lastIndexOf('.');
			int digitosFinales = limpio.length() - ultimoPunto - 1;
			if(primerPunto >= 0 && primerPunto == ultimoPunto && digitosFinales > 0 && digitosFinales <= 2)
			{
				//Un solo punto con uno o dos digitos detras: es decimal, se deja.
			}
			else
			{
				//Varios puntos, o tres digitos detras: son separadores de miles.
				limpio = limpio.replace(".", "");
			}
		}
		try
		{
			double leido = Double.parseDouble(limpio);
			//Un costo negativo no existe y romperia el costeo de la varianza,
			//que quedaria con las perdidas del lado de las ganancias.
			if(leido < 0)
			{
				return(null);
			}
			return(Double.valueOf(leido));
		}catch(Exception e)
		{
			return(null);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
