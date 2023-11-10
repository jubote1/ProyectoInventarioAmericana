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
import capaModeloINV.DesechoTienda;

/**
 * Servlet implementation class InsertarDespachoTienda
 */
@WebServlet("/ObtenerDesechosTiendaFecha")
/**
 * Servlet que tiene como objetivo la INserción del encabezado de un despacho tienda.
 * @author JuanDavid
 *
 */
public class ObtenerDesechosTiendaFecha extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerDesechosTiendaFecha() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json");
		Logger logger = Logger.getLogger("log_file");
		HttpSession sesion = request.getSession(true);
		String fecha = request.getParameter("fecha");
        int idtienda;
        try
        {
        	idtienda = Integer.parseInt(request.getParameter("idtienda"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idtienda = 0;
        }
        InventarioCtrl inv = new InventarioCtrl();
        String respuesta = inv.obtenerDesechosTiendaFecha(fecha, idtienda);
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
