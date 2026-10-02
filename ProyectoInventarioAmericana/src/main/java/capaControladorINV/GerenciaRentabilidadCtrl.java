package capaControladorINV;

import java.util.ArrayList;

import capaDAOINV.GerenciaRentabilidadDAO;

/**
 * El JSON del tablero de rentabilidad.
 *
 * Arma el texto a mano, igual que el resto del menu Gerencia, para no meterle
 * una libreria de JSON a un proyecto que no la tiene. Todo lo que sale de la
 * base pasa por {@link #esc} antes de entrar al texto.
 *
 * LAS CIFRAS VIAJAN CRUDAS
 *
 * Nada se redondea aqui ni se convierte a texto con puntos de miles. El
 * formato es de la pantalla; si se decidiera aca, el Excel saldria con los
 * numeros como texto y no se podria sumar una columna.
 */
public class GerenciaRentabilidadCtrl {

	public String consultarEscalera(final int anio, final int mes, final int idSemana) {
		final GerenciaRentabilidadDAO.Escalera e =
				GerenciaRentabilidadDAO.escalera(anio, mes, idSemana);
		if (e.error.length() > 0) {
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"" + esc(e.error) + "\"}");
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("{\"respuesta\":\"OK\"");
		sb.append(",\"anio\":").append(e.anio);
		sb.append(",\"mes\":").append(e.mes);
		sb.append(",\"semanas\":").append(e.semanas);
		sb.append(",\"idsemana\":").append(idSemana);
		sb.append(",\"desde\":\"").append(esc(e.desde)).append('"');
		sb.append(",\"hasta\":\"").append(esc(e.hasta)).append('"');
		sb.append(",\"simulado\":").append(e.simulado);
		//Cuando se mira una semana sola, los costos mensuales van repartidos. La
		//pantalla tiene que decirlo: si no, alguien compara dos semanas del mismo
		//mes y concluye que la nomina no se movio, cuando lo que pasa es que se
		//dividio en partes iguales.
		sb.append(",\"prorrateado\":").append(idSemana > 0);

		sb.append(",\"faltantes\":[");
		for (int i = 0; i < e.faltantes.size(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append('"').append(esc(e.faltantes.get(i))).append('"');
		}
		sb.append(']');

		sb.append(",\"tiendas\":[");
		for (int i = 0; i < e.tiendas.size(); i++) {
			final GerenciaRentabilidadDAO.Tienda t = e.tiendas.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"idtienda\":").append(t.idTienda);
			sb.append(",\"nombre\":\"").append(esc(t.nombre)).append('"');
			sb.append(",\"venta\":").append(num(t.venta));
			sb.append(",\"meta\":").append(num(t.meta));
			sb.append(",\"resultado\":").append(num(t.resultado));
			sb.append(",\"resultado_pct\":").append(num(t.resultadoPct));
			sb.append(",\"lineas\":[");
			for (int k = 0; k < t.lineas.size(); k++) {
				final GerenciaRentabilidadDAO.Linea l = t.lineas.get(k);
				if (k > 0) {
					sb.append(',');
				}
				sb.append("{\"codigo\":\"").append(esc(l.codigo)).append('"');
				sb.append(",\"nombre\":\"").append(esc(l.nombre)).append('"');
				sb.append(",\"valor\":").append(num(l.valor));
				sb.append(",\"pct\":").append(num(l.pct));
				sb.append(",\"hay_dato\":").append(l.hayDato);
				sb.append(",\"subtotal\":").append(l.subtotal);
				sb.append(",\"origen\":\"").append(esc(l.origen)).append("\"}");
			}
			sb.append("]}");
		}
		sb.append("]}");
		return (sb.toString());
	}

	/** Los meses del ano que ya tienen calendario, y si tienen venta. */
	public String consultarMeses(final int anio) {
		final ArrayList<int[]> meses = GerenciaRentabilidadDAO.mesesDe(anio);
		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\",\"meses\":[");
		for (int i = 0; i < meses.size(); i++) {
			final int[] m = meses.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"mes\":").append(m[0]).append(",\"semanas\":").append(m[1])
			  .append(",\"con_venta\":").append(m[2]).append('}');
		}
		return (sb.append("]}").toString());
	}

	/** Las semanas de un mes, para el selector de la vista semanal. */
	public String consultarSemanas(final int anio, final int mes) {
		final ArrayList<GerenciaRentabilidadDAO.Semana> lista =
				GerenciaRentabilidadDAO.semanasDe(anio, mes);
		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\",\"semanas\":[");
		for (int i = 0; i < lista.size(); i++) {
			final GerenciaRentabilidadDAO.Semana s = lista.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"idsemana\":").append(s.idSemana);
			sb.append(",\"numero\":").append(s.numero);
			sb.append(",\"desde\":\"").append(esc(s.desde)).append('"');
			sb.append(",\"hasta\":\"").append(esc(s.hasta)).append('"');
			sb.append(",\"con_venta\":").append(s.conVenta).append('}');
		}
		return (sb.append("]}").toString());
	}

	public String consultarTendencia(final int idTienda, final int anio) {
		final ArrayList<double[]> filas = GerenciaRentabilidadDAO.tendencia(idTienda, anio);
		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\",\"semanas\":[");
		for (int i = 0; i < filas.size(); i++) {
			final double[] f = filas.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"numero\":").append((int) f[0]);
			sb.append(",\"mes\":").append((int) f[1]);
			sb.append(",\"venta\":").append(num(f[2]));
			sb.append(",\"resultado\":").append(num(f[3]));
			sb.append(",\"pct\":").append(num(f[4])).append('}');
		}
		return (sb.append("]}").toString());
	}

	public String consultarDetalle(final int idTienda, final int anio, final int mes,
			final String linea) {
		final ArrayList<GerenciaRentabilidadDAO.Detalle> filas =
				GerenciaRentabilidadDAO.detalle(idTienda, anio, mes, linea);
		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\",\"detalle\":[");
		for (int i = 0; i < filas.size(); i++) {
			final GerenciaRentabilidadDAO.Detalle d = filas.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"concepto\":\"").append(esc(d.concepto)).append('"');
			sb.append(",\"valor\":").append(num(d.valor));
			sb.append(",\"nota\":\"").append(esc(d.nota)).append("\"}");
		}
		return (sb.append("]}").toString());
	}

	/**
	 * Un numero para JSON.
	 *
	 * NaN e infinito no son JSON valido y rompen el parseo del navegador entero,
	 * no solo esa celda. Pasan a 0 porque a esta altura ya quedo dicho aparte,
	 * en hay_dato, que el dato no existe.
	 */
	private static String num(final double d) {
		if (Double.isNaN(d) || Double.isInfinite(d)) {
			return ("0");
		}
		return (String.valueOf(Math.round(d * 100) / 100.0));
	}

	/** Comillas, barras y saltos, que son lo que rompe un JSON armado a mano. */
	private static String esc(final String s) {
		if (s == null) {
			return ("");
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (c == '"' || c == '\\') {
				sb.append('\\').append(c);
			} else if (c == '\n' || c == '\r' || c == '\t') {
				sb.append(' ');
			} else if (c < 32) {
				sb.append(' ');
			} else {
				sb.append(c);
			}
		}
		return (sb.toString());
	}
}
