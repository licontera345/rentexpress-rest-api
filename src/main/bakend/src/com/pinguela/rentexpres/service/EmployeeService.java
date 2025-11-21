package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.EmployeeCriteria;
import com.pinguela.rentexpres.model.EmployeeDTO;

/**
 * Interfaz para la capa de servicio de Employee. Define operaciones de negocio
 * para CRUD, autenticación y búsqueda por criterios, lanzando
 * RentexpresException en caso de fallo en la lógica o acceso a datos.
 * 
 * @author Jesús Félix
 * @version 1.0
 */
public interface EmployeeService {

	/**
	 * Recupera un Employee por su identificador.
	 * 
	 * @param id Identificador del Employee a buscar.
	 * @return {@link EmployeeDTO} con los datos del Employee o null si no existe.
	 * @throws RentexpresException Si ocurre un error en la capa de datos o lógica.
	 */
	public EmployeeDTO findById(Integer id) throws RentexpresException;

	/**
	 * Crea un nuevo Employee en el sistema.
	 * 
	 * @param Employee {@link EmployeeDTO} con la información del nuevo Employee.
	 * @return true si la creación fue exitosa; false en caso contrario.
	 * @throws RentexpresException Si ocurre un error en la capa de datos o lógica.
	 */
	public boolean create(EmployeeDTO Employee) throws RentexpresException;

	/**
	 * Actualiza los datos de un Employee existente.
	 * 
	 * @param Employee {@link EmployeeDTO} con la información actualizada (ID no
	 *                 nulo).
	 * @return true si la actualización fue exitosa; false en caso contrario.
	 * @throws RentexpresException Si ocurre un error en la capa de datos o lógica.
	 */
	public boolean update(EmployeeDTO Employee) throws RentexpresException;

	/**
	 * Elimina un Employee del sistema.
	 * 
	 * @param Employee {@link EmployeeDTO} del Employee a eliminar (ID no nulo).
	 * @param id       Identificador del Employee (debe coincidir con
	 *                 Employee.getId()).
	 * @return true si la eliminación fue exitosa; false en caso contrario.
	 * @throws RentexpresException Si ocurre un error en la capa de datos o lógica.
	 */
	public boolean delete(EmployeeDTO Employee, Integer id) throws RentexpresException;

	/**
	 * Autentica a un Employee con nombre de Employee y contraseña en texto claro.
	 * 
	 * @param nombreEmployee    Nombre de Employee para iniciar sesión.
	 * @param contrasenaEnClaro Contraseña en texto claro para comparar.
	 * @return {@link EmployeeDTO} con datos del Employee autenticado o null si
	 *         falla.
	 * @throws RentexpresException Si ocurre un error en la capa de datos o lógica.
	 */
	public EmployeeDTO autenticar(String nombreEmployee, String contrasenaEnClaro) throws RentexpresException;

	/**
	 * Recupera todos los Employees registrados en el sistema.
	 * 
	 * @return Lista de {@link EmployeeDTO} con todos los Employees existentes.
	 * @throws RentexpresException Si ocurre un error en la capa de datos o lógica.
	 */
	public List<EmployeeDTO> findAll() throws RentexpresException;

	/**
	 * Busca Employees según criterios de filtrado y paginación.
	 * 
	 * @param criteria {@link EmployeeCriteria} con filtros y parámetros de página.
	 * @return {@link Results}&lt;{@link EmployeeDTO}&gt; con la lista paginada y el
	 *         total.
	 * @throws RentexpresException Si ocurre un error en la capa de datos o lógica.
	 */
	public Results<EmployeeDTO> findByCriteria(EmployeeCriteria criteria) throws RentexpresException;

	/**
	 * Reactivates a deactivated employee by their ID.
	 *
	 * @param id Employee ID
	 * @return true if activation was successful
	 * @throws RentexpresException On data or logic errors
	 */
	boolean activate(Integer id) throws RentexpresException;

}