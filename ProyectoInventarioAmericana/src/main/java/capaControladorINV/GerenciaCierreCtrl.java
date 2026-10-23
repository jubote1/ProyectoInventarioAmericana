package capaControladorINV;

import java.util.ArrayList;

import capaDAOINV.GerenciaCierreDAO;

/**
 * El JSON del cierre por areas.
 *
 * Arma el texto a mano, igual que el resto del menu Gerencia, para no meterle
 * una libreria de JSON a un proyecto que no la tiene. Todo lo que sale de la
 * base pasa por {@link #esc} antes de entrar al texto.
 *
 * Las cifras viajan crudas, sin redondear ni formatear: el formato es de la
 * pantalla. Si se decidiera aqui, el Excel saldria con los numeros como texto y
 * no se podria sumar una columna.
 */
public class GerenciaCierreCtrl {

	public String consultarCierre(final int anio, final int mes,
			final int idSemanaDesde, final int idSemanaHasta) {
		final GerenciaCierreDAO.Cierre c =
				GerenciaCierreDAO.cierre(anio, mes, idSemanaDesde, idSemanaHasta);
		if (c.error.length() > 0) {
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"" + esc(c.error) + "\"}");
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("{\"respuesta\":\"OK\"");
		sb.append(",\"anio\":").append(c.anio);
		sb.append(",\"mes\":").append(c.mes);
		sb.append(",\"semanasdelmes\":").append(c.semanasDelMes);
		sb.append(",\"semanaspedidas\":").append(c.semanasPedidas);
		sb.append(",\"desde\":\"").append(esc(c.desde)).append('"');
		sb.append(",\"hasta\":\"").append(esc(c.hasta)).append('"');
		//Cuando no es el mes entero, los costos mensuales van repartidos. La
		//pantalla tiene que decirlo: un arriendo no se paga por cuartos.
		sb.append(",\"prorrateado\":").append(c.prorrateado);
		sb.append(",\"pctseguridad\":").append(c.porcentajeSeguridadSocial);
		sb.append(",\"pctliquidacion\":").append(c.porcentajeLiquidacion);

		sb.append(",\"areas\":[");
		for (int i = 0; i < c.areas.size(); i++) {
			final GerenciaCierreDAO.Area a = c.areas.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"codigo\":\"").append(esc(a.codigo)).append('"');
			sb.append(",\"nombre\":\"").append(esc(a.nombre)).append('"');
			sb.append(",\"sueldos\":").append(a.sueldos);
			sb.append(",\"seguridadsocial\":").append(a.seguridadSocial);
			sb.append(",\"liquidacion\":").append(a.liquidacion);
			sb.append(",\"nomina\":").append(a.nomina);
			sb.append(",\"totalgastos\":").append(a.totalGastos);
			sb.append(",\"total\":").append(a.total);
			sb.append(",\"sinsalario\":").append(a.sinSalario);

			sb.append(",\"personas\":[");
			for (int j = 0; j < a.personas.size(); j++) {
				final GerenciaCierreDAO.Persona p = a.personas.get(j);
				if (j > 0) {
					sb.append(',');
				}
				sb.append("{\"id\":").append(p.id);
				sb.append(",\"nombre\":\"").append(esc(p.nombre)).append('"');
				sb.append(",\"cargo\":\"").append(esc(p.cargo)).append('"');
				sb.append(",\"basico\":").append(p.basico);
				sb.append(",\"variable\":").append(p.variable);
				sb.append(",\"seguridadsocial\":").append(p.seguridadSocial);
				sb.append(",\"liquidacion\":").append(p.liquidacion);
				sb.append(",\"costo\":").append(p.costo);
				sb.append(",\"aviso\":\"").append(esc(p.aviso)).append("\"}");
			}
			sb.append(']');

			sb.append(",\"gastos\":[");
			for (int j = 0; j < a.gastos.size(); j++) {
				final GerenciaCierreDAO.Gasto g = a.gastos.get(j);
				if (j > 0) {
					sb.append(',');
				}
				sb.append("{\"idconcepto\":").append(g.idConcepto);
				sb.append(",\"nombre\":\"").append(esc(g.nombre)).append('"');
				sb.append(",\"tipo\":\"").append(esc(g.tipo)).append('"');
				sb.append(",\"mensual\":").append(g.mensual);
				sb.append(",\"periodo\":").append(g.periodo).append('}');
			}
			sb.append(']');

			sb.append(",\"avisos\":").append(lista(a.avisos));
			sb.append('}');
		}
		sb.append("]}");
		return (sb.toString());
	}

	private String lista(final ArrayList<String> textos) {
		final StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < textos.size(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append('"').append(esc(textos.get(i))).append('"');
		}
		return (sb.append(']').toString());
	}

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
