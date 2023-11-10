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
@WebServlet("/InsertarDesechoTienda")
/**
 * Servlet que tiene como objetivo la INserción del encabezado de un despacho tienda.
 * @author JuanDavid
 *
 */
public class InsertarDesechoTienda extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertarDesechoTienda() {
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
		String motivo = request.getParameter("motivo");
		String descripcion = request.getParameter("descripcion");
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
        int numeroDesecho;
        try
        {
        	numeroDesecho = Integer.parseInt(request.getParameter("numerodesecho"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	numeroDesecho = 0;
        }
        int idDesecho;
        try
        {
        	idDesecho = Integer.parseInt(request.getParameter("iddesecho"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idDesecho = 0;
        }
        double gramos;
        try
        {
        	gramos = Double.parseDouble(request.getParameter("gramos"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	gramos = 0;
        }
        double cantidad = 0;
        try
        {
        	cantidad = Double.parseDouble(request.getParameter("cantidad"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	gramos = 0;
        }
        String usuario = request.getParameter("usuario");
        InventarioCtrl inv = new InventarioCtrl();
        DesechoTienda des = new DesechoTienda(0,numeroDesecho, idtienda, fecha, descripcion, motivo, idDesecho, gramos, cantidad, usuario);
        String respuesta = inv.insertarDesechoTienda(des);
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
