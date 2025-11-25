package com.pinguela.rentexpres.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Clase de utilidades para manejo de conexiones JDBC y transacciones.
 */
public class JDBCUtils {

	private static final Logger logger = LogManager.getLogger(JDBCUtils.class);
	private static DataSource ds = null;

	static {
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/rentexpresDS");
        } catch (Exception e) {
            throw new RuntimeException("No se pudo inicializar el DataSource jdbc/rentexpresDS", e);
        }
    }

	public static final Connection getConnection() throws SQLException {
	    return ds.getConnection();
	}

	/**
	 * Inicia una transacción poniendo auto-commit a false.
	 */
	public static void beginTransaction(Connection connection) throws SQLException {
		if (connection != null) {
			connection.setAutoCommit(false);
			logger.debug("Transacción iniciada.");
		}
	}

	/**
	 * Hace commit de la transacción si la conexión no es nula.
	 */
	public static void commitTransaction(Connection connection) {
		if (connection != null) {
			try {
				connection.commit();
				logger.debug("Transacción confirmada (commit).");
			} catch (SQLException e) {
				logger.error("Error al confirmar la transacción.", e);
			}
		}
	}

	/**
	 * Hace rollback de la transacción si la conexión no es nula.
	 */
	public static void rollbackTransaction(Connection connection) {
		if (connection != null) {
			try {
				// Verifica si la conexión no está cerrada antes de hacer rollback
				if (!connection.isClosed()) {
					connection.rollback();
					logger.debug("Transacción revertida.");
				} else {
					logger.warn("No se pudo revertir la transacción porque la conexión ya está cerrada.");
				}
			} catch (SQLException e) {
				logger.error("Error al revertir la transacción.", e);
			}
		}
	}

	/**
	 * Cierra el PreparedStatement y el ResultSet si no son nulos.
	 */
	public static void close(PreparedStatement ps, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		} catch (SQLException e) {
			logger.error("Error cerrando ResultSet o PreparedStatement.", e);
		}
	}

	/**
	 * Cierra la conexión (devuelve al pool).
	 */
	public static void close(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
				logger.debug("Conexión cerrada (devuelta al pool).");
			}
		} catch (SQLException e) {
			logger.error("Error cerrando la conexión.", e);
		}
	}
}
