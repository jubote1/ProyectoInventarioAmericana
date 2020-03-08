package capaServicio;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import capaModelo.Insumo;
import capaControlador.InventarioCtrl;
import org.apache.log4j.Logger;
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
	 * Este servicio recibe un idoperación que puede ser 1 insertar 2 editar 3 Eliminar  4 Consultar
	 * dependiendo el valor de idoperacion, se recibirán los diferentes parámetros de la entidad especialidad.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			//Operación idoperacion 1 insertar 2 editar 3 Eliminar  4 Consultar
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
				double costoUnidad = 0;
				try
				{
					costoUnidad = Integer.parseInt(request.getParameter("costounidad"));
				}catch(Exception e)
				{
					costoUnidad = 0;
				}
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
				double costoUnidad = 0;
				try
				{
					costoUnidad = Integer.parseInt(request.getParameter("costounidad"));
				}catch(Exception e)
				{
					costoUnidad = 0;
				}
				String controlTienda = request.getParameter("controltienda");
				Insumo insumoEdit = new Insumo(idInsumoEdit,nombre, unidadMedida,precioUnidad,manejaCanasta,cantidadCanasta,nombreContenedor,categoria, controlCantidad,costoUnidad, controlTienda);
				respuesta = invCtrl.editarInsumo(insumoEdit);
			}else if (operacion ==3 )
			{
				//No existe acción para la eliminación
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
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
