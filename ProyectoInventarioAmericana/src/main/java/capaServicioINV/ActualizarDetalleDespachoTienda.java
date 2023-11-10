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

/**
 * Servlet implementation class InsertarDetalleDespachoTienda
 */
@WebServlet("/ActualizarDetalleDespachoTienda")
public class ActualizarDetalleDespachoTienda extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActualizarDetalleDespachoTienda() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json");
		Logger logger = Logger.getLogger("log_file");
		HttpSession sesion = request.getSession(true);
		double cantidad;
        int iddespacho;
        int idinsumo;
        String contenedor;
        String lote;
        int estado;
        int idDespachoDetalle;
        try
        {
        	idDespachoDetalle = Integer.parseInt(request.getParameter("iddespachodetalle"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idDespachoDetalle = 0;
        }
        try
        {
        	iddespacho = Integer.parseInt(request.getParameter("iddespacho"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	iddespacho = 0;
        }
        try
        {
        	idinsumo = Integer.parseInt(request.getParameter("idinsumo"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idinsumo = 0;
        }
        try
        {
        	cantidad = Double.parseDouble(request.getParameter("cantidad"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	cantidad = 0;
        }
        try
        {
        	contenedor = request.getParameter("contenedor");
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	contenedor = "";
        }
        try
        {
        	lote = request.getParameter("lote");
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	lote = "";
        }
        try
        {
        	estado = Integer.parseInt(request.getParameter("estado"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	estado = 0;
        }
        if(estado != 0 && estado != 1)
        {
        	estado = 0;
        }
        InventarioCtrl inv = new InventarioCtrl();
        String respuesta = inv.ActualizarDetalleInsumoDespachoTienda(iddespacho,idinsumo,cantidad,contenedor,lote,estado,idDespachoDetalle);
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
